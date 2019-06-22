package me.panpf.app.install.auto;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import me.panpf.app.install.AILog;
import me.panpf.app.packages.PackageChangedListener;
import me.panpf.app.packages.PackageMonitor;
import me.panpf.app.packages.PackageUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 绑定管理器，用来维护各种绑定关系
 */
public class BindManager {
    private static final String NAME = "BindManager";

    /**
     * 等待安装的App的名称集合，自动安装服务会从这里面检查来过滤不是从应用汇启动的安装
     */
    private final Map<String, String> bindingAppNameMap = new HashMap<>();

    /**
     * 安装完毕等待点击完成按钮的App名称集合，安装完毕后APP名称会从bindingAppNameMap集合中移到这个集合中来，完成页面只会从这个集合里面检查APP名称过滤不是从应用汇启动的安装
     */
    private final Map<String, Long> waitingClickDoneButtonMap = new HashMap<>();

    /**
     * 当前正在安装的APP的名称，用来区分是否是一个新的页面，当新的AppName同当前的AppName不一样的时候就会认为是一个新的页面
     */
    private String currentAppName;

    /**
     * 当前等待点击完成按钮的APP的名称
     */
    private String currentWaitingClickDoneAppName;

    /**
     * 用来保存当前等待安装的任务数，当任务数为0的时候AutoInstallAccessibilityService将不会再处理，
     * <br>这样可以防止在没有安装任务的时候AutoInstallAccessibilityService彻底停止工作，不会再自动点击一些AlertDialog
     */
    private int waitingInstallTaskCount;

    BindManager(@NonNull Context context, @NonNull PackageMonitor packageMonitor, @NonNull final HandlerThread handlerThread) {
        final Context appContext = context.getApplicationContext();

        final Handler workHandler = new Handler(handlerThread.getLooper());

        packageMonitor.getListeners().register(new PackageChangedListener() {
            @Override
            public void onPackageChanged(boolean added, @NonNull String packageName, @Nullable PackageUtils.AppPackage appPackage) {
                if (added && appPackage != null) {
                    workHandler.post(new UnbindTask(appContext, appPackage, BindManager.this));
                }
            }
        });
    }

    public void bindApp(String realAppName) {
        if (realAppName != null) {
            synchronized (bindingAppNameMap) {
                bindingAppNameMap.put(realAppName, "binding");
                waitingInstallTaskCount++;
            }

            AILog.d(NAME, "bind. The binding 【" + realAppName + "】");
        }
    }

    public boolean isBinding(String realAppName) {
        if (realAppName != null) {
            boolean binding;
            synchronized (bindingAppNameMap) {
                binding = bindingAppNameMap.containsKey(realAppName);
            }
            if (binding) {
                AILog.d(NAME, "isBinding. The binding of 【" + realAppName + "】");
            } else {
                AILog.d(NAME, "isBinding. unbounded 【" + realAppName + "】");
            }
            return binding;
        } else {
            AILog.d(NAME, "isBinding. appName is null");
            return false;
        }
    }

    public void unbindApp(String realAppName) {
        if (realAppName != null) {
            synchronized (bindingAppNameMap) {
                if (bindingAppNameMap.containsKey(realAppName)) {
                    bindingAppNameMap.remove(realAppName);
                    if (waitingInstallTaskCount > 0) {
                        waitingInstallTaskCount--;
                    }
                    if (realAppName.equals(currentAppName)) {
                        currentAppName = null;
                    }

                    synchronized (waitingClickDoneButtonMap) {
                        waitingClickDoneButtonMap.put(realAppName, System.currentTimeMillis());
                    }
                    AILog.d(NAME, "unbind. Has cancelled the binding 【" + realAppName + "】");
                }
            }
        }
    }

    public boolean isWaitingClickDoneButton(String realAppName) {
        if (realAppName != null) {
            boolean binding;
            synchronized (waitingClickDoneButtonMap) {
                binding = waitingClickDoneButtonMap.containsKey(realAppName);
            }
            if (binding) {
                AILog.d(NAME, "isWaitingClickDoneButton. The waitingClickDoneButton of 【" + realAppName + "】");
            } else {
                AILog.d(NAME, "isWaitingClickDoneButton. no 【" + realAppName + "】");
            }
            return binding;
        } else {
            AILog.d(NAME, "isWaitingClickDoneButton. appName is null");
            return false;
        }
    }

    public void clickDoneButton(String appName) {
        synchronized (waitingClickDoneButtonMap) {
            if (appName != null && waitingClickDoneButtonMap.containsKey(appName)) {
                waitingClickDoneButtonMap.remove(appName);
                AILog.d(NAME, "clickDoneButton. 【" + appName + "】");
            }
        }
    }

    /**
     * 自动安装服务关闭
     */
    public void autoInstallAccessibilityServiceUnbid() {
        synchronized (bindingAppNameMap) {
            bindingAppNameMap.clear();
        }
        synchronized (waitingClickDoneButtonMap) {
            waitingClickDoneButtonMap.clear();
        }
        currentAppName = null;
        currentWaitingClickDoneAppName = null;
        waitingInstallTaskCount = 0;
        AILog.d(NAME, "autoInstallAccessibilityServiceUnbid");
    }

    public int getWaitingInstallTaskCount() {
        return waitingInstallTaskCount;
    }

    public String getCurrentAppName() {
        return currentAppName;
    }

    public void setCurrentAppName(String currentAppName) {
        this.currentAppName = currentAppName;
    }

    public String getCurrentWaitingClickDoneAppName() {
        return currentWaitingClickDoneAppName;
    }

    public void setCurrentWaitingClickDoneAppName(String currentWaitingClickDoneAppName) {
        this.currentWaitingClickDoneAppName = currentWaitingClickDoneAppName;
    }
}