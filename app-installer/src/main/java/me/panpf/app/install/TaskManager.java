package me.panpf.app.install;

import android.annotation.SuppressLint;
import android.app.Application;
import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;

import java.util.ArrayList;
import java.util.List;

/**
 * 安装任务执行器
 */
public class TaskManager {

    @NonNull
    private Application application;
    @NonNull
    private Preferences preferences;
    @NonNull
    private EventMonitor eventMonitor;
    @NonNull
    private List<TaskMatcher> taskMatcherList;

    @Nullable
    private PackageSource currentPackageSource;
    @Nullable
    private TaskExecutor taskExecutor;
    @NonNull
    private final ArrayMap<String, Integer> statusMap = new ArrayMap<>();
    @NonNull
    private ListenerManager listenerManager;

    public TaskManager(@NonNull Application application, @NonNull Preferences preferences,
                       @NonNull EventMonitor eventMonitor, @NonNull ListenerManager listenerManager) {
        this.application = application;
        this.preferences = preferences;
        this.eventMonitor = eventMonitor;
        this.listenerManager = listenerManager;

        this.taskMatcherList = new ArrayList<>();
        this.taskMatcherList.add(new ApkTaskMatcher());
    }

    @AnyThread
    public void addInstallMatcher(@NonNull TaskMatcher taskMatcher) {
        this.taskMatcherList.add(taskMatcher);
    }

    @SuppressLint("WrongConstant")
    public synchronized boolean post(@NonNull PackageSource packageSource) {
        int currentStatus = getStatus(packageSource.getId());
        if (currentStatus != -1) {
            AILog.w("Repeat submit install task. " + packageSource.getId() + ":" + currentStatus);
            eventMonitor.onRepeatInstall(packageSource, currentStatus);
            return false;
        }

        if (taskExecutor == null) {
            taskExecutor = new TaskExecutor(this, eventMonitor, new ExecutorListener());
        }

        setStatus(packageSource, InstallStatus.WAITING_QUEUE);

        taskExecutor.postTask(packageSource);

        packageSource.onPost(application);

        return true;
    }

    @Nullable
    BaseInstallTask matchInstaller(@NonNull PackageSource packageSource) {
        for (TaskMatcher matcher : taskMatcherList) {
            if (matcher.match(packageSource)) {
                return matcher.createInstallTask(application, preferences, eventMonitor, this, packageSource);
            }
        }
        return null;
    }

    @Nullable
    public Progress getProgress(@NonNull String key){
        PackageSource packageSource = currentPackageSource;
        if (packageSource != null && packageSource.getId().equals(key)) {
            return new Progress(packageSource.getDecompressTotalLength(), packageSource.getDecompressCompletedLength());
        } else {
            return null;
        }
    }

    void setCurrentPackageSource(@Nullable PackageSource currentPackageSource) {
        this.currentPackageSource = currentPackageSource;
    }

    @InstallStatus
    int getStatus(@NonNull String key) {
        Integer status;
        synchronized (statusMap) {
            status = statusMap.get(key);
        }
        return status != null ? status : -1;
    }

    @AnyThread
    void setStatus(@NonNull PackageSource packageSource, @InstallStatus int newStatus) {
        packageSource.setStatus(newStatus);

        synchronized (statusMap) {
            statusMap.put(packageSource.getId(), newStatus);
        }
        listenerManager.postCallbackStatus(packageSource.getId(), newStatus);
    }

    @AnyThread
    @SuppressLint("WrongConstant")
    void delete(@NonNull PackageSource packageSource) {
        packageSource.setStatus(-1);

        synchronized (statusMap) {
            statusMap.remove(packageSource.getId());
        }
        listenerManager.postCallbackStatus(packageSource.getId(), -1);
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
            delete(packageSource);
        }
    }
}
