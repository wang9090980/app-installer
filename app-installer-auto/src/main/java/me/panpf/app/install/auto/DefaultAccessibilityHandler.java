package me.panpf.app.install.auto;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.accessibility.AccessibilityNodeInfo;

import me.panpf.app.install.AILog;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class DefaultAccessibilityHandler extends AbstractAccessibilityHandler {
    private static final String NAME = "DefaultAccessibilityHandler";

    protected List<TargetPage> targetPageList;

    public DefaultAccessibilityHandler(AccessibilityHandlerListener accessibilityHandlerListener, List<TargetPage> targetPageList) {
        super(accessibilityHandlerListener);
        this.targetPageList = targetPageList;
    }

    @Override
    public List<TargetPage> getTargetPageList() {
        return targetPageList;
    }

    @Override
    protected TargetPage findTargetPage(String className) {
        if (targetPageList != null && targetPageList.size() > 0) {
            for (TargetPage targetPage : targetPageList) {
                if (targetPage.className.equalsIgnoreCase(className)) {
                    AILog.d(NAME, "findTargetPage. " + targetPage.getInfo());
                    if(targetPage.donePage){
                        // 完成页面不需要检查任务数
                        return targetPage;
                    }else{
                        if(accessibilityHandlerListener.getBindManager().getWaitingInstallTaskCount() > 0){
                            return targetPage;
                        }else {
                            AILog.w(NAME, "findTargetPage. waitingInstallTaskCount is 0");
                            return null;
                        }
                    }
                }
            }
        }

        return null;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    protected ButtonBindHolder findTargetButton(TargetPage currentTargetPage, AccessibilityNodeInfo root) {
        Queue<AccessibilityNodeInfo> nodeInfoQueue = new LinkedList<>();
        nodeInfoQueue.add(root);
        while (!nodeInfoQueue.isEmpty()) {
            AccessibilityNodeInfo nodeInfo = nodeInfoQueue.poll();
            if (nodeInfo == null) {
                break;
            }
            String nodePackageName = nodeInfo.getPackageName().toString();
            if (!accessibilityHandlerListener.isAcceptPackageName(nodePackageName)) {
                AILog.w(NAME, "findTargetButton. Don't accept the package name：" + nodePackageName);
                continue;
            }
            AILog.d(NAME, "findTargetButton. nodeInfo: " + nodeInfo.getPackageName() + ", " + nodeInfo.getClassName() + ", " + nodeInfo.getText());
            if (nodeInfo.getChildCount() > 0) {
                for (int w = 0, size = nodeInfo.getChildCount(); w < size; w++) {
                    nodeInfoQueue.add(nodeInfo.getChild(w));
                }
            } else {
                if (nodeInfo.isEnabled() && nodeInfo.isClickable()) {
                    TargetButton targetButton = matching(nodeInfo, currentTargetPage.targetButtonList);
                    if (targetButton != null) {
                        ButtonBindHolder bindHolder = new ButtonBindHolder();
                        bindHolder.targetButton = targetButton;
                        bindHolder.nodeInfo = nodeInfo;
                        return bindHolder;
                    }
                }
            }
        }

        return null;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private TargetButton matching(AccessibilityNodeInfo nodeInfo, List<TargetButton> targetButtonList) {
        if (nodeInfo != null && nodeInfo.getText() != null && targetButtonList != null && targetButtonList.size() > 0) {
            String nodeClassName = nodeInfo.getClassName().toString();
            String nodeText = nodeInfo.getText().toString();
            for (TargetButton targetButton : targetButtonList) {
                if (targetButton.className != null && targetButton.nameList != null && targetButton.className.equals(nodeClassName)) {
                    for(String name : targetButton.nameList){
                        if(name.equalsIgnoreCase(nodeText)){
                            return targetButton;
                        }
                    }
                }
            }
        }
        return null;
    }

    public void setTargetPageList(List<TargetPage> targetPageList) {
        this.targetPageList = targetPageList;
    }
}
