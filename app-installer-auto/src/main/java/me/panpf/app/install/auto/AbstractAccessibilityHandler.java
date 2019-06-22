package me.panpf.app.install.auto;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import me.panpf.app.install.AILog;

import java.util.LinkedList;
import java.util.Queue;

public abstract class AbstractAccessibilityHandler implements AccessibilityHandler, FindTargetButtonTask.FindTargetButtonListener, ClickTargetButtonTask.ClickTargetButtonListener {
    private static final String NAME = "AbstractAccessibilityHandler";
    protected AccessibilityHandlerListener accessibilityHandlerListener;
    private Handler handler;
    private ClickTargetButtonTask waitClickTargetButtonTask;
    private FindTargetButtonTask waitFindTargetButtonTask;
    private TargetPage currentTargetPage;
    /*
        在联想的VIBEUI上，安装器只有一个页面，既是起始页又是完成页；
        安装完成后会先收到安装成功的广播，同时清除InstallBindManager中的CurrentAppName；
        然后在onWindowContentChanged中试图查找“完成”按钮的时候会因为CurrentAppName为null，且又是起始页导致无法进入查找的流程
        因此专门为这种情况增加一个tempIgnoreCheckAppName的参数，执行完Lenovo_VIBEUI的“安装”按钮后会设置此参数为true；
        然后再onWindowContentChanged的时候就会跳过检查appName，直接进入查找“完成”按钮的流程
     */
    private boolean tempIgnoreCheckAppName;

    public AbstractAccessibilityHandler(AccessibilityHandlerListener accessibilityHandlerListener) {
        this.accessibilityHandlerListener = accessibilityHandlerListener;
        this.handler = new Handler(Looper.getMainLooper());
    }

    protected abstract TargetPage findTargetPage(String className);

    protected abstract ButtonBindHolder findTargetButton(TargetPage currentTargetPage, AccessibilityNodeInfo root);

    @Override
    public void onWindowStateChanged(AccessibilityEvent event) {
        cancelOldClickTargetButtonTask();
        cancelOldFindTargetButtonTask();
        tempIgnoreCheckAppName = false;

        // 查找页面
        String className = event.getClassName().toString();
        TargetPage newTargetPage = findTargetPage(className);
        if (newTargetPage != currentTargetPage) {
            // 切换页面了
            // 需要重置一些数据
            accessibilityHandlerListener.getBindManager().setCurrentAppName(null);
            accessibilityHandlerListener.getBindManager().setCurrentWaitingClickDoneAppName(null);
            currentTargetPage = newTargetPage;

            // 验证页面来源
            if (currentTargetPage != null) {
                if (AILog.isLoggable(AILog.VERBOSE)) {
                    AILog.v(NAME, "onWindowStateChanged. NewCurrentTargetPage: " + currentTargetPage.className);
                }
                if (currentTargetPage.startPage) {
                    // 新的页面是起始页，要检查来源
                    String bindingAppName = checkBindingAppName();
                    if (bindingAppName != null) {
                        if (!bindingAppName.equals(accessibilityHandlerListener.getBindManager().getCurrentAppName())) {
                            // 新页面
                            accessibilityHandlerListener.getBindManager().setCurrentAppName(bindingAppName);
                            accessibilityHandlerListener.onNewApp(bindingAppName);
                            if (AILog.isLoggable(AILog.VERBOSE)) {
                                AILog.v(NAME, "onWindowStateChanged. startPage: New applications are being installed 【" + bindingAppName + "】");
                            }
                        } else {
                            if (AILog.isLoggable(AILog.VERBOSE)) {
                                AILog.v(NAME, "onWindowStateChanged. startPage: The same application without prompt again 【" + bindingAppName + "】");
                            }
                        }
                    } else {
                        AILog.w(NAME, "onWindowStateChanged. startPage: unknown source");
                    }
                } else if (currentTargetPage.donePage) {
                    // 新的页面是完成页，也要检查来源
                    String waitingClickDoneAppName = checkWaitingClickDoneAppName();
                    if (waitingClickDoneAppName != null) {
                        accessibilityHandlerListener.getBindManager().setCurrentWaitingClickDoneAppName(waitingClickDoneAppName);
                        if (AILog.isLoggable(AILog.VERBOSE)) {
                            AILog.v(NAME, "onWindowStateChanged. donePage: New applications are being installed 【" + waitingClickDoneAppName + "】");
                        }
                    } else {
                        if (AILog.isLoggable(AILog.DEBUG)) {
                            AILog.d(NAME, "onWindowStateChanged. donePage: unknown source");
                        }
                    }
                } else {
                    AILog.w(NAME, "onWindowStateChanged. otherPage");
                }
            } else {
                AILog.w(NAME, "onWindowStateChanged. unknownPage: " + className);
            }
        } else {
            if (newTargetPage == null) {
                AILog.w(NAME, "onWindowStateChanged. unknownPage: " + className);
            }
        }

        tryDelayedFindTargetButton(false);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    protected String checkBindingAppName() {
        String newAppName = null;
        Queue<AccessibilityNodeInfo> nodeInfoQueue = new LinkedList<>();
        nodeInfoQueue.add(accessibilityHandlerListener.getRootNodeInActiveWindow());
        while (!nodeInfoQueue.isEmpty()) {
            AccessibilityNodeInfo nodeInfo = nodeInfoQueue.poll();
            if (nodeInfo == null) {
                break;
            }
            String nodePackageName = nodeInfo.getPackageName().toString();
            if (!accessibilityHandlerListener.isAcceptPackageName(nodePackageName)) {
                AILog.w(NAME, "checkBindingAppName. Don't accept the node package name：" + nodePackageName);
                continue;
            }
            if (nodeInfo.getChildCount() > 0) {
                for (int w = 0, size = nodeInfo.getChildCount(); w < size; w++) {
                    nodeInfoQueue.add(nodeInfo.getChild(w));
                }
            } else {
                String className = nodeInfo.getClassName().toString();
                if ("android.widget.TextView".equals(className)) {
                    CharSequence nodeTextCharSequence = nodeInfo.getText();
                    if (nodeTextCharSequence != null) {
                        String nodeText = nodeTextCharSequence.toString();
                        if (accessibilityHandlerListener.getBindManager().isBinding(nodeText)) {
                            newAppName = nodeText;
                            break;
                        }
                    }
                }
            }
        }
        return newAppName;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    protected String checkWaitingClickDoneAppName() {
        String newAppName = null;
        Queue<AccessibilityNodeInfo> nodeInfoQueue = new LinkedList<>();
        nodeInfoQueue.add(accessibilityHandlerListener.getRootNodeInActiveWindow());
        while (!nodeInfoQueue.isEmpty()) {
            AccessibilityNodeInfo nodeInfo = nodeInfoQueue.poll();
            if (nodeInfo == null) {
                break;
            }
            String nodePackageName = nodeInfo.getPackageName().toString();
            if (!accessibilityHandlerListener.isAcceptPackageName(nodePackageName)) {
                AILog.w(NAME, "checkWaitingClickDoneAppName. Don't accept the node package name：" + nodePackageName);
                continue;
            }
            if (nodeInfo.getChildCount() > 0) {
                for (int w = 0, size = nodeInfo.getChildCount(); w < size; w++) {
                    nodeInfoQueue.add(nodeInfo.getChild(w));
                }
            } else {
                String className = nodeInfo.getClassName().toString();
                if ("android.widget.TextView".equals(className)) {
                    CharSequence nodeTextCharSequence = nodeInfo.getText();
                    if (nodeTextCharSequence != null) {
                        String nodeText = nodeTextCharSequence.toString();
                        if (accessibilityHandlerListener.getBindManager().isWaitingClickDoneButton(nodeText)) {
                            newAppName = nodeText;
                            break;
                        }
                    }
                }
            }
        }
        return newAppName;
    }

    protected void tryDelayedFindTargetButton(boolean checkAppName) {
        if (currentTargetPage == null) {
            AILog.w(NAME, "tryDelayedFindTargetButton. currentTargetPage is null");
            return;
        }

        if (tempIgnoreCheckAppName) {
            AILog.w(NAME, "tryDelayedFindTargetButton. ignore check app name！");
            delayedFindTargetButton();
        } else if (currentTargetPage.startPage) {
            String currentAppName = accessibilityHandlerListener.getBindManager().getCurrentAppName();
            if (currentAppName != null) {
                delayedFindTargetButton();
            } else if (checkAppName) {
                String newAppName = checkBindingAppName();
                if (newAppName != null) {
                    if (!newAppName.equals(currentAppName)) {
                        accessibilityHandlerListener.getBindManager().setCurrentAppName(newAppName);
                        accessibilityHandlerListener.onNewApp(newAppName);
                        if (AILog.isLoggable(AILog.VERBOSE)) {
                            AILog.v(NAME, "tryDelayedFindTargetButton. startPage: New applications are being installed 【" + newAppName + "】");
                        }
                        delayedFindTargetButton();
                    } else {
                        AILog.w(NAME, "tryDelayedFindTargetButton. startPage: The same application without prompt again 【" + newAppName + "】");
                    }
                } else {
                    AILog.w(NAME, "tryDelayedFindTargetButton. startPage: unknown source");
                }
            } else {
                AILog.w(NAME, "tryDelayedFindTargetButton. startPage: WuWu！");
            }
        } else if (currentTargetPage.donePage) {
            String currentWaitingClickDoneAppName = accessibilityHandlerListener.getBindManager().getCurrentWaitingClickDoneAppName();
            if (currentWaitingClickDoneAppName != null) {
                delayedFindTargetButton();
            } else if (checkAppName) {
                // 新的页面是完成页，也要检查来源
                String newWaitingClickDoneAppName = checkWaitingClickDoneAppName();
                if (newWaitingClickDoneAppName != null) {
                    if (AILog.isLoggable(AILog.VERBOSE)) {
                        AILog.v(NAME, "tryDelayedFindTargetButton. donePage: New applications are being installed 【" + newWaitingClickDoneAppName + "】");
                    }
                    accessibilityHandlerListener.getBindManager().setCurrentWaitingClickDoneAppName(newWaitingClickDoneAppName);
                    delayedFindTargetButton();
                } else {
                    AILog.w(NAME, "tryDelayedFindTargetButton. donePage: unknown source");
                }
            } else {
                AILog.w(NAME, "tryDelayedFindTargetButton. donePage: WuWu！");
            }
        } else {
            AILog.w(NAME, "tryDelayedFindTargetButton. other page");
            delayedFindTargetButton();
        }
    }

    @Override
    public void onWindowContentChanged(AccessibilityEvent event) {
        tryDelayedFindTargetButton(true);
    }

    @Override
    public void onViewScrolled(AccessibilityEvent event) {
        tryDelayedFindTargetButton(true);
    }

    private void cancelOldClickTargetButtonTask() {
        if (waitClickTargetButtonTask != null && !waitClickTargetButtonTask.isFinished()) {
            waitClickTargetButtonTask.cancel();
            waitClickTargetButtonTask = null;
            AILog.w(NAME, "cancelOldClickTargetButtonTask. has cancelled the click task to be performed");
        }
    }

    private void cancelOldFindTargetButtonTask() {
        if (waitFindTargetButtonTask != null && !waitFindTargetButtonTask.isFinished()) {
            waitFindTargetButtonTask.cancel();
            waitFindTargetButtonTask = null;
            AILog.w(NAME, "cancelOldClickTargetButtonTask. has cancelled the find task to be performed");
        }
    }

    private void delayedFindTargetButton() {
        cancelOldClickTargetButtonTask();
        cancelOldFindTargetButtonTask();

        waitFindTargetButtonTask = new FindTargetButtonTask(this, handler);
        waitFindTargetButtonTask.postDelayed();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onFindTargetButton() {
        if (currentTargetPage != null) {
            ButtonBindHolder buttonBindHolder = findTargetButton(currentTargetPage, accessibilityHandlerListener.getRootNodeInActiveWindow());
            if (buttonBindHolder != null) {
                waitClickTargetButtonTask = new ClickTargetButtonTask(this, handler, buttonBindHolder.targetButton, buttonBindHolder.nodeInfo);
                waitClickTargetButtonTask.postDelayed();
                if (AILog.isLoggable(AILog.VERBOSE)) {
                    AILog.v(NAME, "onFindTargetButton. to find the 【" + buttonBindHolder.nodeInfo.getText() + "】 button, " + "100 milliseconds click execution");
                }
            } else {
                AILog.w(NAME, "onFindTargetButton. didn't find anything");
            }
        }
    }

    @SuppressLint("InlinedApi")
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onClickTargetButton(TargetButton targetButton, AccessibilityNodeInfo nodeInfo) {
        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        if (targetButton.installButton) {
            accessibilityHandlerListener.getBindManager().unbindApp(accessibilityHandlerListener.getBindManager().getCurrentAppName());
            if (currentTargetPage.startPage && currentTargetPage.donePage) {
                tempIgnoreCheckAppName = true;
                AILog.d(NAME, "onClickTargetButton. set temp ignore check app name");
            }
        } else if (targetButton.doneButton) {
            accessibilityHandlerListener.getBindManager().clickDoneButton(accessibilityHandlerListener.getBindManager().getCurrentWaitingClickDoneAppName());
        }

        accessibilityHandlerListener.onClickButton(targetButton, nodeInfo);
        if (AILog.isLoggable(AILog.DEBUG)) {
            AILog.d(NAME, "onClickTargetButton. Click on the " + nodeInfo.getText() + " button, class name is " + nodeInfo.getClassName());
        }
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public void onServiceConnected() {
    }

    @Override
    public void onUnbind() {
        cancelOldClickTargetButtonTask();
        cancelOldFindTargetButtonTask();
    }

    @Override
    public void onDestroy() {
    }
}
