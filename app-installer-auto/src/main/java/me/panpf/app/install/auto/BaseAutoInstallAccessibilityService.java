package me.panpf.app.install.auto;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import me.panpf.app.install.AILog;
import me.panpf.app.install.AppInstaller;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public abstract class BaseAutoInstallAccessibilityService extends AccessibilityService implements AccessibilityHandlerListener {
    public static final String NAME = "AutoInstallAccessibilityService";
    public static final String ACTION_SERVICE_CONNECTED = "ACTION_SERVICE_CONNECTED";

    private AccessibilityHandler accessibilityHandler;
    private List<String> acceptPackageNameList;
    @Nullable
    private AutoInstallAlert autoInstallAlert;
    private Configuration configuration;

    @NonNull
    protected abstract AppInstaller getAppInstaller(Context context);

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        AppInstaller appInstaller = getAppInstaller(getBaseContext());
        this.autoInstallAlert = appInstaller.getAutoInstaller().getAutoInstallAlert();

        if (!appInstaller.getAutoInstaller().isUsable(false)) {
            return;
        }

        configuration = ConfigurationManager.getInstance(getBaseContext()).getConfiguration();
        if (configuration != null) {
            accessibilityHandler = configuration.getAccessibilityHandler(this);
        }

        if (configuration != null && accessibilityHandler != null) {
            accessibilityHandler.onServiceConnected();
            AILog.d(NAME, "serviceConnected" + ". " + configuration.getName());
        } else {
            AILog.w(NAME, "serviceConnected. not support");
            return;
        }
        boolean opened = appInstaller.getAutoInstaller().isOpenedAutoInstallService();
        appInstaller.getAutoInstaller().setOpenedAutoInstallService(true);

        appInstaller.getAutoInstaller().setServiceRunning(true);

        String[] packageNames = getServiceInfo().packageNames;
        if (packageNames != null && packageNames.length > 0) {
            this.acceptPackageNameList = new LinkedList<>();
            Collections.addAll(acceptPackageNameList, packageNames);
        } else {
            AILog.e(NAME, "serviceConnected. 没有配置packageNames");
        }

        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(new Intent(ACTION_SERVICE_CONNECTED));

        appInstaller.getEventMonitor().onAutoInstallServiceConnected(opened);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!getAppInstaller(getBaseContext()).getAutoInstaller().isUsable(false)) {
            AILog.w(NAME, "onAccessibilityEvent" + ". no support");
            return;
        }

        if (accessibilityHandler == null) {
            AILog.w(NAME, "onAccessibilityEvent. not support");
            return;
        }

        String eventPackageName = event.getPackageName().toString();
        if (!isAcceptPackageName(eventPackageName)) {
            AILog.w(NAME, "onAccessibilityEvent. Don't accept the package name：" + eventPackageName);
            return;
        }

        AILog.d(NAME, "onAccessibilityEvent. accessibilityEvent: packageName=" + event.getPackageName() + "; eventType=" + AutoInstallUtils.getEventName(event) + "; className=" + event.getClassName() + "; text=" + event.getBeforeText());

        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                if (accessibilityHandler != null) {
                    accessibilityHandler.onWindowStateChanged(event);
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                if (accessibilityHandler != null) {
                    accessibilityHandler.onWindowContentChanged(event);
                }
                break;
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                if (accessibilityHandler != null) {
                    accessibilityHandler.onViewScrolled(event);
                }
                break;
        }
    }

    @Override
    public void onInterrupt() {
        AILog.d(NAME, "onInterrupt. interrupt");
        if (accessibilityHandler != null) {
            accessibilityHandler.onInterrupt();
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        AILog.d(NAME, "onUnbind. unbind");
        if (accessibilityHandler != null) {
            accessibilityHandler.onUnbind();
        }

        AppInstaller appInstaller = getAppInstaller(getBaseContext());

        appInstaller.getAutoInstaller().getBindManager().autoInstallAccessibilityServiceUnbid();
        appInstaller.getAutoInstaller().setServiceRunning(false);

        appInstaller.getEventMonitor().onAutoInstallServiceUnbind();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        AILog.d(NAME, "onDestroy. destroy");
        if (accessibilityHandler != null) {
            accessibilityHandler.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    public boolean isAcceptPackageName(String packageName) {
        return acceptPackageNameList == null || acceptPackageNameList.contains(packageName);
    }

    @Override
    public Context getContext() {
        return getBaseContext();
    }

    @Override
    public BindManager getBindManager() {
        return getAppInstaller(getBaseContext()).getAutoInstaller().getBindManager();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public AccessibilityNodeInfo getRootNodeInActiveWindow() {
        return super.getRootInActiveWindow();
    }

    @Override
    public void onNewApp(String newAppName) {

    }

    @Override
    public void onClickButton(TargetButton button, AccessibilityNodeInfo nodeInfo) {
        if (button.installButton) {
            if (autoInstallAlert != null) {
                autoInstallAlert.showAlert();
            }

            AppInstaller appInstaller = getAppInstaller(getBaseContext());
            appInstaller.getEventMonitor().onClickAutoInstallTargetButton(configuration, button, nodeInfo);
        }
    }
}