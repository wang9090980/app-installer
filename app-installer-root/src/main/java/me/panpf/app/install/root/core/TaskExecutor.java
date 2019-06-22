package me.panpf.app.install.core;

import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import me.panpf.app.install.AILog;
import me.panpf.app.install.EventMonitor;

import androidx.annotation.NonNull;

class TaskExecutor {
    @NonNull
    private TaskManager taskManager;
    @NonNull
    private EventMonitor eventMonitor;
    @NonNull
    private Listener listener;

    private Handler handler;
    private HandlerThread handlerThread;

    TaskExecutor(@NonNull TaskManager taskManager, @NonNull EventMonitor eventMonitor, @NonNull Listener listener) {
        this.taskManager = taskManager;
        this.eventMonitor = eventMonitor;
        this.listener = listener;

        this.handlerThread = new HandlerThread("AppInstaller");
        this.handlerThread.start();

        this.handler = new Handler(handlerThread.getLooper(), new WorkHandler());

        // 一分钟后自毁
        handler.sendMessageDelayed(handler.obtainMessage(WorkHandler.STOP_SELF), 1000 * 60);
    }

    void postTask(@NonNull PackageSource packageSource) {
        // 取消自毁定时
        handler.removeMessages(WorkHandler.STOP_SELF);

        handler.obtainMessage(WorkHandler.INSTALL, packageSource).sendToTarget();
    }

    public interface Listener {
        void onStopSelf();

        void onNoMatchedInstaller(@NonNull PackageSource packageSource);
    }

    private class WorkHandler implements Handler.Callback {
        private static final int INSTALL = 4242;
        private static final int STOP_SELF = 4243;

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case INSTALL:
                    // 取消自毁定时
                    handler.removeMessages(WorkHandler.STOP_SELF);

                    // 匹配任务并执行
                    PackageSource packageSource = (PackageSource) msg.obj;
                    BaseInstallTask installTask = taskManager.matchInstaller(packageSource);
                    if (installTask != null) {
                        taskManager.setCurrentPackageSource(packageSource);
                        installTask.run();
                        taskManager.setCurrentPackageSource(null);
                    } else {
                        AILog.e("No matched installer. " + packageSource.getLogInfo());
                        eventMonitor.onNoMatchedInstaller(packageSource);
                        listener.onNoMatchedInstaller(packageSource);
                    }

                    // 一分钟后自毁
                    handler.sendMessageDelayed(handler.obtainMessage(STOP_SELF), 1000 * 60);
                    break;
                case STOP_SELF:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        handlerThread.quitSafely();
                    } else {
                        handlerThread.quit();
                    }
                    listener.onStopSelf();
                    break;
            }
            return false;
        }
    }
}
