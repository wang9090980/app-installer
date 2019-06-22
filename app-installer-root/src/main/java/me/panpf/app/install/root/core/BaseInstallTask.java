package me.panpf.app.install.core;

import android.content.Context;

import me.panpf.app.install.AILog;
import me.panpf.app.install.AppInstaller;
import me.panpf.app.install.InstallStatus;

import androidx.annotation.NonNull;

public abstract class BaseInstallTask implements Runnable {
    private static final String NAME = "BaseInstallTask";

    @NonNull
    protected Context appContext;
    @NonNull
    @SuppressWarnings("WeakerAccess")
    protected AppInstaller appInstaller;
    @NonNull
    @SuppressWarnings("WeakerAccess")
    protected PackageSource packageSource;

    BaseInstallTask(@NonNull Context context, @NonNull AppInstaller appInstaller, @NonNull PackageSource packageSource) {
        this.appContext = context.getApplicationContext();
        this.appInstaller = appInstaller;
        this.packageSource = packageSource;
    }

    @Override
    public final void run() {
        if (AILog.isLoggable(AILog.DEBUG)) {
            AILog.d(NAME, "start. " + packageSource.getLogInfo());
        }

        appInstaller.getEventMonitor().onTaskStart(packageSource);

        appInstaller.getStatusManager().setStatus(packageSource, InstallStatus.INSTALLING);

        boolean success = dispatchInstall();

        appInstaller.getStatusManager().delete(packageSource);

        if (success) {
            appInstaller.getEventMonitor().onTaskSuccess(packageSource);
        } else {
            appInstaller.getEventMonitor().onTaskError(packageSource);
        }
        if (AILog.isLoggable(AILog.DEBUG)) {
            AILog.d(NAME, "finished. " + packageSource.getLogInfo());
        }
    }

    /**
     * 分发安装流程
     */
    protected abstract boolean dispatchInstall();
}