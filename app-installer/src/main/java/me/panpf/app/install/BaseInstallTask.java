package me.panpf.app.install;

import android.app.Application;
import androidx.annotation.NonNull;

@SuppressWarnings("WeakerAccess")
public abstract class BaseInstallTask implements Runnable {
    private static final String NAME = "BaseInstallTask";

    @NonNull
    protected Application appContext;
    @NonNull
    protected Preferences preferences;
    @NonNull
    protected EventMonitor eventMonitor;
    @NonNull
    protected TaskManager taskManager;
    @NonNull
    protected PackageSource packageSource;

    BaseInstallTask(@NonNull Application application, @NonNull Preferences preferences, @NonNull EventMonitor eventMonitor, @NonNull TaskManager taskManager, @NonNull PackageSource packageSource) {
        this.appContext = application;
        this.preferences = preferences;
        this.eventMonitor = eventMonitor;
        this.taskManager = taskManager;
        this.packageSource = packageSource;
    }

    @Override
    public final void run() {
        if (AILog.isLoggable(AILog.DEBUG)) {
            AILog.d(NAME, "start. " + packageSource.getLogInfo());
        }

        eventMonitor.onTaskStart(packageSource);

        taskManager.setStatus(packageSource, InstallStatus.INSTALLING);

        boolean success = dispatchInstall();

        taskManager.delete(packageSource);

        if (success) {
            eventMonitor.onTaskSuccess(packageSource);
        } else {
            eventMonitor.onTaskError(packageSource);
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