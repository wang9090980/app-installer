package me.panpf.app.install;

import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import java.io.File;

import me.panpf.shell.Cmd;
import me.panpf.shell.CmdResult;
import me.panpf.shell.ResultCallback;
import me.panpf.shell.Sheller;

/**
 * ROOT 安装相关
 */
public class RootInstaller {
    private static final long ROOT_RETRY_TIMES = 10 * 24 * 60 * 60 * 1000;
    private static final int MAX_REMIND_COUNT = 2;    // ROOT 安装最大提醒次数
    private static final int MAX_FAILED_COUNT = 2;    // 最大失败次数，超过这个次数就自动关闭 ROOT 安装

    @NonNull
    private AppInstaller appInstaller;
//    private String installCommandPrefix;

    RootInstaller(@NonNull AppInstaller appInstaller) {
        this.appInstaller = appInstaller;
    }

    public boolean isEnabled() {
        return appInstaller.getPreferences().isEnabledRootInstall();
    }

    public void setEnabled(boolean enabled) {
        appInstaller.getPreferences().setEnabledRootInstall(enabled);

        if (enabled) {
            setFailedCount(0);
        } else {
            resetNextRemindEnableRootInstallTime();
        }
    }

    private int getRemindEnableRootInstallCount() {
        return appInstaller.getPreferences().getRemindEnableRootInstallCount();
    }

    private void setRemindEnableRootInstallCount(int count) {
        if (count > MAX_REMIND_COUNT) {
            count = MAX_REMIND_COUNT;
        }
        appInstaller.getPreferences().setRemindEnableRootInstallCount(count);
    }

    private long getNextRemindEnableRootInstallTime() {
        return appInstaller.getPreferences().getNextRemindEnableRootInstallTime();
    }

    private void resetNextRemindEnableRootInstallTime() {
        appInstaller.getPreferences().setNextRemindEnableRootInstallTime(System.currentTimeMillis() + ROOT_RETRY_TIMES);
    }

    private int getFailedCount() {
        return appInstaller.getPreferences().getRootInstallFailedCount();
    }

    private void setFailedCount(int count) {
        appInstaller.getPreferences().setRootInstallFailedCount(count);
    }

    public boolean canRemindEnableRootInstall() {
        return getRemindEnableRootInstallCount() < MAX_REMIND_COUNT
                && System.currentTimeMillis() >= getNextRemindEnableRootInstallTime();
    }

    public boolean isFailedCountExceedLimit() {
        return getFailedCount() >= MAX_FAILED_COUNT;
    }

    public void recordRemindEnableRootInstall() {
        setRemindEnableRootInstallCount(getRemindEnableRootInstallCount() + 1);
        resetNextRemindEnableRootInstallTime();
    }

    private void preExecute() {
//        // 兼容 MIUI 双开，在 MIUI 上开启双开以后，通过 pm 安装会自动生成双开应用
//        if (RomManager.getInstance().romType == RomManager.RomType.MIUI) {
//            try {
//                ShellUtils.execCommand("setprop debug.adb.default_USER_ALL false", true);
//            } catch (Exception ignored) {
//
//            }
//        }

//        // 根据 cpu 类型构建环境变量，在 4.0 以后某些版本 ROOT 后丢失 LD_LIBRARY_PATH
//        if (installCommandPrefix == null) {
//            if (System.getProperty("os.arch", "").contains("64")) {
//                installCommandPrefix = "LD_LIBRARY_PATH=/vendor/lib64:/system/lib64";
//            } else {
//                installCommandPrefix = "LD_LIBRARY_PATH=/vendor/lib:/system/lib";
//            }
//        }
    }

    @NonNull
    @WorkerThread
    public CmdResult syncExecute(File file) {
        preExecute();

        CmdResult result = new Sheller(new Cmd("su" + "\n" + "pm install -r " + file.getPath()).timeout(120 * 1000)).syncExecute();

        if (result.isSuccess()) {
            AILog.i("syncRootInstall. " + result.toString());
        } else {
            AILog.w("syncRootInstall. " + result.toString());
        }
        handleResult(result);

        return result;
    }

    @WorkerThread
    public void asyncExecute(File file, Handler callbackHandler, final ResultCallback callback) {
        preExecute();

        new Sheller(new Cmd("su" + "\n" + "pm install -r " + file.getPath()).timeout(120 * 1000)).asyncExecute(callbackHandler, new ResultCallback() {
            @Override
            public void onCallbackResult(@NonNull CmdResult result) {
                if (result.isSuccess()) {
                    AILog.i("asyncRootInstall. " + result.toString());
                } else {
                    AILog.w("asyncRootInstall. " + result.toString());
                }
                handleResult(result);

                callback.onCallbackResult(result);
            }
        });
    }

    private void handleResult(@NonNull CmdResult result) {
        if (result.isSuccess()) {
            // 安装成功了，失败计数清零
            setFailedCount(0);
        } else {
            // 安装失败了，超过超过最大次数限制就就关闭 root 安装
            setFailedCount(getFailedCount() + 1);
            if (isFailedCountExceedLimit()) {
                setEnabled(false);
                AILog.w("RootInstaller", "auto closed root install");
            }
        }
    }
}
