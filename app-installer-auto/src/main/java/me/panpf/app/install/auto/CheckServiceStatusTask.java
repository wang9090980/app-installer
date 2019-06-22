package me.panpf.app.install.auto;

import android.app.Activity;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;

import me.panpf.app.install.AppInstaller;
import me.panpf.app.install.AILog;

import java.lang.ref.WeakReference;

/**
 * 检查服务状态，如果异常关闭就提醒用户
 */
public class CheckServiceStatusTask implements Runnable {
    @NonNull
    private WeakReference<Activity> activityWeakReference;
    @NonNull
    private AppInstaller appInstaller;

    CheckServiceStatusTask(@NonNull Activity activity, @NonNull AppInstaller appInstaller) {
        this.activityWeakReference = new WeakReference<>(activity);
        this.appInstaller = appInstaller;
    }

    @Override
    public void run() {
        Activity activity = activityWeakReference.get();
        if (activity == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && appInstaller.getAutoInstaller().isServiceRunning()
                && !appInstaller.getAutoInstaller().isServiceRunningByActivityManager()) {

            appInstaller.getAutoInstaller().setServiceRunning(false);

            int abnormalShutdownAlertCount = appInstaller.getAutoInstaller().getClosedUnexpectedlyRemindCount();
            if (abnormalShutdownAlertCount < 3) {
                appInstaller.getAutoInstaller().setClosedUnexpectedlyRemindCount(abnormalShutdownAlertCount + 1);

                new Handler(Looper.getMainLooper()).post(new RemindTask(activity, appInstaller));
            } else {
                AILog.w("Auto install function was shut down abnormally, but the number of prompts has been more than 3 times, it is no longer reminded");
            }
        }
    }

    public static class RemindTask implements Runnable {
        @NonNull
        private WeakReference<Activity> activityWeakReference;
        @NonNull
        private AppInstaller appInstaller;

        RemindTask(@NonNull Activity activity, @NonNull AppInstaller appInstaller) {
            this.activityWeakReference = new WeakReference<>(activity);
            this.appInstaller = appInstaller;
        }

        @Override
        public void run() {
            Activity activity = activityWeakReference.get();
            if (activity == null) {
                return;
            }

            appInstaller.getEventMonitor().onAutoInstallServiceClosedUnexpectedly(activity);
        }
    }
}
