package me.panpf.app.install;

import android.app.Application;
import android.os.HandlerThread;
import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.LinkedList;
import java.util.List;

/**
 * App 安装器，App 安装的所有有关操作都从这里开始
 */
public class AppInstaller {

    @NonNull
    private TaskManager taskManager;
    @NonNull
    private ListenerManager listenerManager;
    @NonNull
    private List<PackageChecker> packageCheckers = new LinkedList<>();
    @NonNull
    private List<InstallInterceptor> installInterceptors = new LinkedList<>();

    public AppInstaller(@NonNull Application application, @Nullable Preferences userPreferences, @Nullable EventMonitor userEventMonitor, @NonNull HandlerThread handlerThread) {

        Preferences preferences = userPreferences != null ? userPreferences : new PreferencesImpl(application);
        EventMonitor eventMonitor = userEventMonitor != null ? userEventMonitor : new EventMonitorImpl();
        this.listenerManager = new ListenerManager(handlerThread);
        this.taskManager = new TaskManager(application, preferences, eventMonitor, listenerManager);

        packageCheckers.add(new ApkValidityChecker());
        packageCheckers.add(new ApkSignatureChecker());

        // todo 实现 auto，root，xpk
    }

    @NonNull
    public AppInstaller addPackageChecker(@NonNull PackageChecker packageChecker) {
        //noinspection ConstantConditions
        if (packageChecker != null) {
            packageCheckers.add(packageChecker);
        }
        return this;
    }

    @NonNull
    public AppInstaller addInstallInterceptor(@NonNull InstallInterceptor installInterceptor) {
        //noinspection ConstantConditions
        if (installInterceptor != null) {
            installInterceptors.add(installInterceptor);
        }
        return this;
    }

    @NonNull
    public List<PackageChecker> getPackageCheckers() {
        return packageCheckers;
    }

    @NonNull
    public List<InstallInterceptor> getInstallInterceptors() {
        return installInterceptors;
    }

    @AnyThread
    public void addInstallMatcher(@NonNull TaskMatcher taskMatcher) {
        taskManager.addInstallMatcher(taskMatcher);
    }

    /**
     * 提交安装任务
     *
     * @return false：重复任务
     */
    public boolean postInstall(@NonNull PackageSource packageSource) {
        return taskManager.post(packageSource);
    }


    /* *************************************************** 状态相关 ************************************************** */


    /**
     * 查询状态
     */
    @InstallStatus
    public int queryStatus(@NonNull String key) {
        return taskManager.getStatus(key);
    }

    /**
     * 查询进度
     */
    @Nullable
    public Progress queryProgress(@NonNull String key) {
        return taskManager.getProgress(key);
    }

    /**
     * 注册状态监听器
     *
     * @param key      key
     * @param listener 状态监听器
     */
    @AnyThread
    public void registerStatusListener(@NonNull String key, @NonNull final StatusChangedListener listener) {
        listenerManager.registerStatusListener(key, listener);
    }

    /**
     * 注册状态监听器，不关心某一个 app 而是关心所有的应用
     *
     * @param listener 状态监听器
     */
    @AnyThread
    public void registerStatusListener(@NonNull final StatusChangedListener listener) {
        listenerManager.registerStatusListener(listener);
    }

    /**
     * 删除状态监听器
     *
     * @param key      key
     * @param listener 状态监听器
     * @return true：删除成功；false：未注册或 listener 为 null
     */
    @AnyThread
    public boolean unregisterStatusListener(@NonNull String key, @NonNull final StatusChangedListener listener) {
        return listenerManager.unregisterStatusListener(key, listener);
    }

    /**
     * 删除状态监听器，不关心某一个 app 而是关心所有的应用
     *
     * @param listener 状态监听器
     * @return true：删除成功；false：未注册或 listener 为 null
     */
    @AnyThread
    public boolean unregisterStatusListener(@NonNull final StatusChangedListener listener) {
        return listenerManager.unregisterStatusListener(listener);
    }

    /**
     * 注册进度监听器
     *
     * @param key      key
     * @param listener 进度监听器
     */
    @AnyThread
    public void registerProgressListener(@NonNull String key, @NonNull final DecompressProgressChangedListener listener) {
        listenerManager.registerProgressListener(key, listener);
    }

    /**
     * 注册进度监听器，不关心某一个 app 而是关心所有的应用
     *
     * @param listener 进度监听器
     */
    @AnyThread
    public void registerProgressListener(@NonNull final DecompressProgressChangedListener listener) {
        listenerManager.registerProgressListener(listener);
    }

    /**
     * 删除进度监听器
     *
     * @param key      key
     * @param listener 进度监听器
     */
    @AnyThread
    public void unregisterProgressListener(@NonNull String key, @NonNull final DecompressProgressChangedListener listener) {
        listenerManager.unregisterProgressListener(key, listener);
    }

    /**
     * 删除进度监听器，不关心某一个 app 而是关心所有的应用
     *
     * @param listener 进度监听器
     */
    @AnyThread
    public void unregisterProgressListener(@NonNull final DecompressProgressChangedListener listener) {
        listenerManager.unregisterProgressListener(listener);
    }
}