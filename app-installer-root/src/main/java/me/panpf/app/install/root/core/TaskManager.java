package me.panpf.app.install.core;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import me.panpf.app.install.AILog;
import me.panpf.app.install.AppInstaller;
import me.panpf.app.install.InstallStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * 安装任务执行器
 */
public class TaskManager {

    @NonNull
    private Context appContext;
    @NonNull
    private AppInstaller appInstaller;
    @NonNull
    private List<TaskMatcher> taskMatcherList;

    @Nullable
    private PackageSource currentPackageSource;
    @Nullable
    private TaskExecutor taskExecutor;

    public TaskManager(@NonNull Context context, @NonNull AppInstaller appInstaller) {
        this.appContext = context.getApplicationContext();
        this.appInstaller = appInstaller;

        this.taskMatcherList = new ArrayList<>();
        this.taskMatcherList.add(new ApkTaskMatcher(appContext, appInstaller));
    }

    @AnyThread
    public void addInstallMatcher(@NonNull TaskMatcher taskMatcher){
        this.taskMatcherList.add(taskMatcher);
    }

    @SuppressLint("WrongConstant")
    public synchronized boolean post(@NonNull PackageSource packageSource) {
        int currentStatus = appInstaller.queryStatus(packageSource.getId());
        if (currentStatus != -1) {
            AILog.w("Repeat submit install task. " + packageSource.getId() + ":" + currentStatus);
            appInstaller.getEventMonitor().onRepeatInstall(packageSource, currentStatus);
            return false;
        }

        if (taskExecutor == null) {
            taskExecutor = new TaskExecutor(this, appInstaller.getEventMonitor(), new ExecutorListener());
        }

        appInstaller.getStatusManager().setStatus(packageSource, InstallStatus.INSTALL_QUEUEING);

        taskExecutor.postTask(packageSource);

        packageSource.onPost(appContext);

        return true;
    }

    @Nullable
    BaseInstallTask matchInstaller(@NonNull PackageSource packageSource) {
        for (TaskMatcher matcher : taskMatcherList) {
            if (matcher.match(packageSource)) {
                return matcher.createInstallTask(packageSource);
            }
        }
        return null;
    }

    @Nullable
    public PackageSource getCurrentPackageSource() {
        return currentPackageSource;
    }

    void setCurrentPackageSource(@Nullable PackageSource currentPackageSource) {
        this.currentPackageSource = currentPackageSource;
    }

    private class ExecutorListener implements TaskExecutor.Listener {
        @Override
        public void onStopSelf() {
            synchronized (TaskManager.this) {
                taskExecutor = null;
            }
        }

        @Override
        public void onNoMatchedInstaller(@NonNull PackageSource packageSource) {
            appInstaller.getStatusManager().delete(packageSource);
        }
    }
}
