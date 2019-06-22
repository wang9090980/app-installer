package me.panpf.app.install.auto;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.HandlerThread;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import me.panpf.app.install.AppInstaller;
import me.panpf.app.packages.PackageMonitor;

import me.panpf.androidx.os.Romx;

/**
 * 自动安装管理器
 */
public class AutoInstaller {
    public static final int AVOID_ROOT_AUTO_INSTALL_MAX_ALERT_COUNT = 2;    // 自动安装最大提醒次数
    private static final long AVOID_ROOT_AUTO_INSTALL_ALERT_DELAY_TIME = 1000 * 60 * 60 * 24 * 2;    // 自动安装延迟提醒间隔时间

    @NonNull
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private Context appContext;
    @NonNull
    private AppInstaller appInstaller;
    @NonNull
    private BindManager bindManager;
    @Nullable
    private AutoInstallAlert autoInstallAlert;
    @NonNull
    private Class<? extends BaseAutoInstallAccessibilityService> serviceClass;

    public AutoInstaller(@NonNull Context context, @NonNull AppInstaller appInstaller,
                         @NonNull PackageMonitor packageMonitor, HandlerThread handlerThread,
                         @NonNull Class<? extends BaseAutoInstallAccessibilityService> serviceClass) {
        this.appContext = context.getApplicationContext();
        this.appInstaller = appInstaller;
        this.serviceClass = serviceClass;
        this.bindManager = new BindManager(context, packageMonitor, handlerThread);
    }

    @NonNull
    public BindManager getBindManager() {
        return bindManager;
    }

    /**
     * 是否满足使用条件
     *
     * @param excludeNotSupportRom 排除不支持的 rom
     */
    public boolean isUsable(boolean excludeNotSupportRom) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && !(excludeNotSupportRom && Romx.isMIUI());
    }

    public boolean isServiceRunningByActivityManager() {
        return AutoInstallUtils.isServiceRunning(appContext, appContext.getPackageName(), serviceClass.getName());
    }

    public void postCheckServiceStatus(Activity activity) {
        appInstaller.postWorkTask(new CheckServiceStatusTask(activity, appInstaller));
    }

    public boolean openAccessibilityPage(@NonNull final Activity activity) {
        boolean success = false;
        try {
            activity.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            success = true;
        } catch (ActivityNotFoundException | NullPointerException e) {
            /* 通过崩溃日志发现有一些机型在执行这段代码的时候会抛出 NullPointerException 异常 */
            e.printStackTrace();
            try {
                activity.startActivity(new Intent(Settings.ACTION_SETTINGS));
                success = true;
            } catch (ActivityNotFoundException | NullPointerException e1) {
                e1.printStackTrace();
            }
        }

        if (success) {
            if (!isServiceRunningByActivityManager() && !isServiceRunning()) {
                appInstaller.getEventMonitor().onRemindAndGuideEnableAutoInstall(activity);
            }
        } else {
            appInstaller.getEventMonitor().onOpenAccessibilityPageFailed(activity);
        }
        return success;
    }


    public boolean isOpenedAutoInstallService() {
        return appInstaller.getPreferences().isOpenedAutoInstallService();
    }

    @SuppressWarnings("WeakerAccess")
    public void setOpenedAutoInstallService(@SuppressWarnings("SameParameterValue") boolean opened) {
        appInstaller.getPreferences().setOpenedAutoInstallService(opened);
    }

    public boolean isNoLongerRemind() {
        return appInstaller.getPreferences().isNoLongerRemindAutoInstall();
    }

    public void setNoLongerRemind(@SuppressWarnings("SameParameterValue") boolean noLonger) {
        appInstaller.getPreferences().setNoLongerRemindAutoInstall(noLonger);
    }

    public int getRemindCount() {
        return appInstaller.getPreferences().getRemindAutoInstallCount();
    }

    public void setRemindCount(int count) {
        appInstaller.getPreferences().setRemindAutoInstallCount(count);
    }

    public long getNextRemindTime() {
        return appInstaller.getPreferences().getNextRemindAutoInstallTime();
    }

    public void resetNextRemindTime() {
        appInstaller.getPreferences().setNextRemindAutoInstallTime(System.currentTimeMillis() + AutoInstaller.AVOID_ROOT_AUTO_INSTALL_ALERT_DELAY_TIME);
    }

    @SuppressWarnings("WeakerAccess")
    public boolean isServiceRunning() {
        return appInstaller.getPreferences().isAutoInstallServiceRunning();
    }

    @SuppressWarnings("WeakerAccess")
    public void setServiceRunning(boolean running) {
        appInstaller.getPreferences().setAutoInstallServiceRunning(running);
    }

    public int getAutoInstallAppCount() {
        return appInstaller.getPreferences().getAutoInstallAppCount();
    }

    public void setAutoInstallAppCount(int count) {
        appInstaller.getPreferences().setAutoInstallAppCount(count);
    }

    @SuppressWarnings("WeakerAccess")
    public int getClosedUnexpectedlyRemindCount() {
        return appInstaller.getPreferences().getClosedUnexpectedlyRemindCount();
    }

    @SuppressWarnings("WeakerAccess")
    public void setClosedUnexpectedlyRemindCount(int count) {
        appInstaller.getPreferences().setClosedUnexpectedlyRemindCount(count);
    }

    @Nullable
    AutoInstallAlert getAutoInstallAlert() {
        return autoInstallAlert;
    }

    public void setAutoInstallAlert(@Nullable AutoInstallAlert autoInstallAlert) {
        this.autoInstallAlert = autoInstallAlert;
    }
}
