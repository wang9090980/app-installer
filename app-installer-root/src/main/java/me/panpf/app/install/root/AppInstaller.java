package me.panpf.app.install;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import me.panpf.app.install.auto.AutoInstaller;
import me.panpf.app.install.auto.BaseAutoInstallAccessibilityService;
import me.panpf.app.install.auto.BindManager;
import me.panpf.app.install.core.NotificationFactory;
import me.panpf.app.install.core.PackageSource;
import me.panpf.app.install.core.TaskManager;
import me.panpf.app.install.core.TaskMatcher;
import me.panpf.app.packages.PackageMonitor;

/**
 * App 安装器，App 安装的所有有关操作都从这里开始
 */
public class AppInstaller {

    @NonNull
    private Handler workHandler;

    @NonNull
    private TaskManager taskManager;
    @NonNull
    private ListenerManager listeners;
    @NonNull
    private StatusManager statusManager;
    @NonNull
    private RootInstaller rootInstaller;
    @NonNull
    private AutoInstaller autoInstaller;
    @NonNull
    private Preferences preferences;
    @NonNull
    private EventMonitor eventMonitor;
    @Nullable
    private NotificationFactory notificationFactory;

    public AppInstaller(@NonNull Context context, @NonNull PackageMonitor packageMonitor,
                        @NonNull HandlerThread handlerThread, @NonNull Class<? extends BaseAutoInstallAccessibilityService> serviceClass) {
        final Context appContext = context.getApplicationContext();

        this.workHandler = new Handler(handlerThread.getLooper());

        this.preferences = new PreferencesImpl(appContext);
        this.eventMonitor = new EventMonitorImpl();

        this.listeners = new ListenerManager(this, handlerThread);
        this.statusManager = new StatusManager(this);

        this.rootInstaller = new RootInstaller(this);
        this.autoInstaller = new AutoInstaller(appContext, this, packageMonitor, handlerThread, serviceClass);
        this.taskManager = new TaskManager(appContext, this);
    }

    /**
     * 提交安装任务
     *
     * @return false：重复任务
     */
    public boolean postInstall(PackageSource packageSource) {
        return taskManager.post(packageSource);
    }

    /**
     * 往下载工作线程提交一个任务
     *
     * @param runnable 任务
     */
    @AnyThread
    public void postWorkTask(Runnable runnable) {
        if (runnable != null) {
            workHandler.post(runnable);
        }
    }

    /**
     * 查询状态
     *
     * @param appPackageName app 包名
     * @param appVersionCode app 版本号
     */
    @InstallStatus
    @SuppressWarnings("unused")
    public int queryStatus(@NonNull String appPackageName, int appVersionCode) {
        return statusManager.query(appPackageName, appVersionCode);
    }

    /**
     * 查询状态
     *
     * @param filePath 安装包路径
     */
    @InstallStatus
    @SuppressWarnings("unused")
    public int queryStatus(@NonNull String filePath) {
        return statusManager.query(filePath);
    }

    @Nullable
    @SuppressWarnings("unused")
    public PackageSource getCurrentPackageSource(@NonNull String id) {
        PackageSource packageSource = taskManager.getCurrentPackageSource();
        if (packageSource != null && packageSource.getId().equals(id)) {
            return packageSource;
        }
        return null;
    }

    @AnyThread
    @SuppressWarnings("WeakerAccess")
    public void addInstallMatcher(@NonNull TaskMatcher taskMatcher) {
        taskManager.addInstallMatcher(taskMatcher);
    }

    @NonNull
    public ListenerManager getListeners() {
        return listeners;
    }

    @NonNull
    public StatusManager getStatusManager() {
        return statusManager;
    }

    @NonNull
    public RootInstaller getRootInstaller() {
        return rootInstaller;
    }

    @SuppressWarnings("unused")
    @NonNull
    public AutoInstaller getAutoInstaller() {
        return autoInstaller;
    }

    @NonNull
    public Preferences getPreferences() {
        return preferences;
    }

    public void setPreferences(@NonNull Preferences preferences) {
        //noinspection ConstantConditions
        if (preferences != null) {
            this.preferences = preferences;
        }
    }

    @NonNull
    public EventMonitor getEventMonitor() {
        return eventMonitor;
    }

    @SuppressWarnings("WeakerAccess")
    public void setEventMonitor(@NonNull EventMonitor eventMonitor) {
        this.eventMonitor = eventMonitor;
    }

    @Nullable
    public NotificationFactory getNotificationFactory() {
        return notificationFactory;
    }

    @SuppressWarnings("WeakerAccess")
    public void setNotificationFactory(@Nullable NotificationFactory notificationFactory) {
        this.notificationFactory = notificationFactory;
    }
}