package me.panpf.app.install.core;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.text.format.Formatter;

import me.panpf.app.install.AILog;
import me.panpf.app.install.AppInstaller;
import me.panpf.app.install.auto.AutoInstaller;
import me.panpf.app.packages.PackageUtils;
import me.panpf.utils.ApkInspector;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.zip.ZipFile;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import me.panpf.androidx.Androidx;
import me.panpf.androidx.os.storage.Storagex;
import me.panpf.javax.security.Digestx;
import me.panpf.shell.CmdResult;

/**
 * APK 安装器
 */
public class ApkInstallTask extends BaseInstallTask {
    private static final String NAME = "ApkInstallTask";

    private File apkFile;

    public ApkInstallTask(@NonNull Context context, @NonNull AppInstaller appInstaller, @NonNull PackageSource packageSource) {
        super(context, appInstaller, packageSource);
    }

    protected File getApkFile() {
        return packageSource.getFile();
    }

    /**
     * 分发安装流程
     */
    @Override
    protected boolean dispatchInstall() {
        if (!prepare()) {
            return false;
        }

        if (!checkPackage()) {
            return false;
        }

        if (!preCheckApk()) {
            return false;
        }

        File apkFile = getApkFile();
        if (apkFile == null) {
            throw new IllegalStateException("Apk file is null");
        }
        this.apkFile = apkFile;

        if (!validApk()) {
            return false;
        }

        if (!checkApkInfo()) {
            return false;
        }

        if (!checkApkSignature()) {
            return false;
        }

        if (!preInstall()) {
            return false;
        }

        if (!rootInstall()) {
            return true;    // root 安装返回 false，代表 root 安装成功，无需走普通安装
        }

        if (!openAutoInstall()) {
            return false;
        }

        if (!startPackageInstaller()) {
            return false;
        }

        onInstallSuccess();
        return true;
    }

    /**
     * 准备工作
     *
     * @return false：结束安装
     */
    private boolean prepare() {
        return true;
    }

    /**
     * 检查安装包
     *
     * @return false：结束安装
     */
    protected boolean checkPackage() {
        if (!packageSource.getFile().exists()) {
            appInstaller.getEventMonitor().onFileLost(packageSource);
            return false;
        }

        return true;
    }

    /**
     * 检查 APK 之前的准备工作
     *
     * @return false：结束安装
     */
    protected boolean preCheckApk() {
        return true;
    }

    /**
     * 验证 apk 是否是一个 zip 文件
     */
    private boolean validApk() {
        /* 这里不能使用 net.lingala.zip4j.core.ZipFile.isValidZipFile() 方法来验证是否是一个有效的 zip 文件，因为 APK 有些特殊 */
        ZipFile zipFile = null;
        try {
            try {
                zipFile = new ZipFile(apkFile);
            } catch (IOException e) {
                e.printStackTrace();

                String packageMd5 = Digestx.getMD5OrEmpty(apkFile);
                AILog.w(NAME, String.format(Locale.US, "Invalid apk. File：%s/%d/%s. %s. %s",
                        packageMd5, apkFile.length(), Formatter.formatFileSize(appContext, apkFile.length()),
                        e.getMessage(), packageSource.getLogInfo()));
                appInstaller.getEventMonitor().onApkZipInvalid(packageSource, packageMd5, e);
                return false;
            }

            if (zipFile.getEntry("AndroidManifest.xml") == null) {
                String packageMd5 = Digestx.getMD5OrEmpty(apkFile);
                AILog.w(NAME, String.format(Locale.US, "Invalid apk. File：%s/%d/%s. %s",
                        packageMd5, apkFile.length(), Formatter.formatFileSize(appContext, apkFile.length()),
                        packageSource.getLogInfo()));
                appInstaller.getEventMonitor().onApkZipInvalid(packageSource, packageMd5, null);
                return false;
            }
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    /**
     * 检查 APK 的信息
     *
     * @return false：结束安装
     */
    private boolean checkApkInfo() {
        PackageManager packageManager = appContext.getPackageManager();
        PackageInfo packageInfo = packageManager.getPackageArchiveInfo(apkFile.getPath(), PackageManager.GET_META_DATA);
        if (packageInfo == null) {
            String packageMd5;
            try {
                packageMd5 = Digestx.getMD5(apkFile);
            } catch (IOException ee) {
                ee.printStackTrace();
                packageMd5 = "";
            }
            AILog.w(NAME, String.format(Locale.US, "Unable to parse apk. File：%s/%d/%s. %s",
                    packageMd5, apkFile.length(), Formatter.formatFileSize(appContext, apkFile.length()),
                    packageSource.getLogInfo()));
            appInstaller.getEventMonitor().onApkPackageInvalid(packageSource, packageMd5);
            return false;
        }

        ApplicationInfo applicationInfo = packageInfo.applicationInfo;
        if (applicationInfo == null) {
            return false;
        }

        applicationInfo.sourceDir = apkFile.getPath();
        applicationInfo.publicSourceDir = apkFile.getPath();
        CharSequence realAppName = applicationInfo.loadLabel(packageManager);

        packageSource.setApkInfo(new ApkInfo(realAppName.toString(), applicationInfo.packageName, packageInfo.versionName, packageInfo.versionCode));
        return true;
    }

    /**
     * 检查 APK 的签名
     *
     * @return false：结束安装
     */
    private boolean checkApkSignature() {
        PackageInfo packageInfo;
        try {
            packageInfo = appContext.getPackageManager().getPackageInfo(packageSource.getPackageName(), PackageManager.GET_UNINSTALLED_PACKAGES);
        } catch (PackageManager.NameNotFoundException e) {
            AILog.d(NAME, "checkApkSignature. uninstalled don't need check signature. " + packageSource.getLogInfo());
            return true;
        }

        String installedAppSignature;
        try {
            installedAppSignature = ApkInspector.getSignatureByFile(packageInfo.applicationInfo.sourceDir);
            if (TextUtils.isEmpty(installedAppSignature)) {
                throw new IllegalArgumentException("installedAppSignature result is null");
            }
        } catch (Exception e) {
            e.printStackTrace();
            AILog.w(NAME, "get signature failed from installed apk. " + packageSource.getLogInfo());
            appInstaller.getEventMonitor().onGetInstalledApkSignatureException(packageSource, e);
            return true;
        }

        String newAppSignature;
        try {
            newAppSignature = ApkInspector.getSignatureByFile(apkFile.getPath());
            if (TextUtils.isEmpty(newAppSignature)) {
                throw new IllegalArgumentException("newAppSignature result is null");
            }
        } catch (Exception e) {
            e.printStackTrace();

            AILog.w(NAME, "get signature failed from new apk. " + packageSource.getLogInfo());
            String packageMd5;
            try {
                packageMd5 = Digestx.getMD5(apkFile);
            } catch (IOException ee) {
                ee.printStackTrace();
                packageMd5 = "";
            }
            appInstaller.getEventMonitor().onGetNewApkSignatureException(packageSource, packageMd5, e);
            return true;
        }

        if (installedAppSignature.equals(newAppSignature)) {
            AILog.d(NAME, "checkApkSignature. signature match. " + packageSource.getLogInfo());
            return true;
        } else {
            AILog.w(NAME, "signature not matched. " + installedAppSignature + ":" + newAppSignature + " " + packageSource.getLogInfo());
            appInstaller.getEventMonitor().onApkSignatureNotMatched(packageSource, installedAppSignature, newAppSignature);

            // 提示签名不匹配后会让用户卸载旧版本，如果旧版本已卸载就继续安装
            try {
                appContext.getPackageManager().getPackageInfo(packageSource.getPackageName(), PackageManager.GET_UNINSTALLED_PACKAGES);
                return false;
            } catch (PackageManager.NameNotFoundException e) {
                AILog.d(NAME, "checkApkSignature. old version uninstalled. " + packageSource.getLogInfo());
                return true;
            }
        }
    }

    /**
     * 安装之前的准备工作
     */
    protected boolean preInstall() {
        return true;
    }

    /**
     * ROOT 安装
     *
     * @return false：结束安装
     */
    private boolean rootInstall() {
        // 自己不能使用 ROOT 安装
        if (appContext.getPackageName().equals(packageSource.getPackageName())) {
            AILog.d(NAME, "rootInstall. self not use root install. " + packageSource.getLogInfo());
            return true;
        }

        // 没有 ROOT 的设备不能使用 ROOT 安装
        if (!Androidx.isRooted()) {
            AILog.d(NAME, "rootInstall. no root. " + packageSource.getLogInfo());
            return true;
        }

        // 提醒用户打开 ROOT 安装
        if (!appInstaller.getRootInstaller().isEnabled()) {
            // 提醒次数已超出最大次数限制，或者下次提醒时间还未到
            if (!appInstaller.getRootInstaller().canRemindEnableRootInstall()) {
                AILog.d(NAME, "rootInstall. next time remind open root install time hasn't arrived. " + packageSource.getLogInfo());
                return true;
            }

            // 提醒用户打开 ROOT 安装
            AILog.d(NAME, "rootInstall. remind open root install. " + packageSource.getLogInfo());
            appInstaller.getRootInstaller().recordRemindEnableRootInstall();
            appInstaller.getEventMonitor().onRemindEnableRootInstall(packageSource);

            // 用户拒绝开启
            if (!appInstaller.getRootInstaller().isEnabled()) {
                AILog.d(NAME, "rootInstall. user refuses to open. " + packageSource.getLogInfo());
                return true;
            }
        }

        NotificationFactory notificationFactory = appInstaller.getNotificationFactory();
        RootInstallingNotification notification = notificationFactory != null ? notificationFactory.createRootInstallingNotification(packageSource) : null;
        if (notification != null) {
            notification.showInstalling();
        }

        // 同步执行 ROOT 安装
        CmdResult result = appInstaller.getRootInstaller().syncExecute(apkFile);

        if (notification != null) {
            notification.close();
        }

        if (result.isSuccess()) {
            //noinspection ConstantConditions
            int installedVersionCode = PackageUtils.getVersionCode(appContext, packageSource.getPackageName());
            if (installedVersionCode != -1 && installedVersionCode == packageSource.getVersionCode()) {
                // ROOT 安装成功
                appInstaller.getEventMonitor().onRemindRootInstallSuccess(packageSource);
                if (notification != null) {
                    notification.showSuccess();
                }

                AILog.v(NAME, "rootInstall. root install success. " + packageSource.getLogInfo());
                return false;
            } else {
                // 命令执行成功，但从系统查询结果看未安装，说明安装包不兼容或者剩余空间不足
                long freeSize = Storagex.getAvailableBytes(new File("/data"), -1);
                long apkSize = apkFile.length();
                long needSize;
                if (apkSize > (100 * 1024 * 1024)) {
                    needSize = (long) (apkSize * 1.5f);
                } else if (apkSize > (50 * 1024 * 1024)) {
                    needSize = (long) (apkSize * 2f);
                } else {
                    needSize = (long) (apkSize * 3f);
                }
                boolean noSpace = freeSize >= 0 && freeSize < needSize;
                AILog.w(NAME, String.format(Locale.US, "root install failed. package incompatible. apkSize=%s, freeSize=%s, noSpace=%s. %s",
                        Formatter.formatFileSize(appContext, apkSize), Formatter.formatFileSize(appContext, freeSize), noSpace, packageSource.getLogInfo()));
                appInstaller.getEventMonitor().onRootInstallFailedNotInstalled(packageSource, apkFile, apkSize, freeSize, noSpace);

                // 返回 true 继续走普通安装
                return true;
            }
        } else if (result.isTimeout()) {
            AILog.w(NAME, "Root install timeout. " + packageSource.getLogInfo());

            boolean rootInstallFailedCountLimit = appInstaller.getRootInstaller().isFailedCountExceedLimit();
            appInstaller.getEventMonitor().onRootInstallFailed(packageSource, result, rootInstallFailedCountLimit, false);

            // 返回 true 继续走普通安装
            return true;
        } else {
            // 错误信息中存在 "permission denied" 或 "unallowed user" 说明是用户拒绝了授权，否则就是 root 功能异常
            boolean permissionDenied = false;
            if (!result.isException() && !TextUtils.isEmpty(result.getErrorText())) {
                String errorTextLowerCase = result.getErrorText().toLowerCase();
                permissionDenied = errorTextLowerCase.contains("permission denied") || errorTextLowerCase.contains("unallowed user");
            }
            AILog.w(NAME, "Root install failed. " + packageSource.getLogInfo());

            boolean rootInstallFailedCountLimit = appInstaller.getRootInstaller().isFailedCountExceedLimit();
            appInstaller.getEventMonitor().onRootInstallFailed(packageSource, result, rootInstallFailedCountLimit, permissionDenied);

            // 返回 true 继续走普通安装
            return true;
        }
    }

    /**
     * 打开自动安装功能
     *
     * @return false：结束安装
     */
    private boolean openAutoInstall() {
        // 无法使用此功能
        if (!appInstaller.getAutoInstaller().isUsable(true)) {
            AILog.d(NAME, "autoInstall. version too low cant use avoid root auto install. " + packageSource.getLogInfo());
            return true;
        }

        // 应用汇自己不能使用自动安装
        if (appContext.getPackageName().equals(packageSource.getPackageName())) {
            AILog.d(NAME, "autoInstall. yyh self not use avoid root auto install. " + packageSource.getLogInfo());
            return true;
        }

        // 服务正在运行
        if (appInstaller.getAutoInstaller().isServiceRunningByActivityManager()) {
            AILog.d(NAME, "autoInstall. avoid root auto install services running. " + packageSource.getLogInfo());
            appInstaller.getAutoInstaller().getBindManager().bindApp(packageSource.getRealAppName());
            return true;
        }

        // 已经开启过服务的话就不再提醒了
        if (appInstaller.getAutoInstaller().isOpenedAutoInstallService()) {
            AILog.d(NAME, "autoInstall. opened avoid root auto install services. " + packageSource.getLogInfo());
            return true;
        }

        // 用户选择了“不再提醒”的话就不再提醒了
        if (appInstaller.getAutoInstaller().isNoLongerRemind()) {
            AILog.d(NAME, "autoInstall. no longer to remind the user to select. " + packageSource.getLogInfo());
            return true;
        }

        // 已提醒次数超过最大限制就不再提醒了
        int alertedCount = appInstaller.getAutoInstaller().getRemindCount();
        if (alertedCount >= AutoInstaller.AVOID_ROOT_AUTO_INSTALL_MAX_ALERT_COUNT) {
            AILog.d(NAME, "autoInstall. remind count beyond the limit. " + packageSource.getLogInfo());
            return true;
        }

        // 下次提醒时间还没到就不提醒
        long nextAlertTime = appInstaller.getAutoInstaller().getNextRemindTime();
        if (System.currentTimeMillis() < nextAlertTime) {
            AILog.d(NAME, "autoInstall. next time remind open avoid root auto install time has arrived. " + packageSource.getLogInfo());
            return true;
        }

        // 已提醒次数加1并更新下次提醒时间
        AILog.d(NAME, "autoInstall. remind open avoid root auto install. " + packageSource.getLogInfo());
        appInstaller.getAutoInstaller().setRemindCount(alertedCount + 1);
        appInstaller.getAutoInstaller().resetNextRemindTime();

        // 提醒用户开启自动安装
        appInstaller.getEventMonitor().onRemindEnableAutoInstall(packageSource);

        // 继续安装
        return true;
    }

    /**
     * 打开系统安装包器安装 APK
     *
     * @return false：结束安装
     */
    private boolean startPackageInstaller() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.setDataAndType(FileProvider.getUriForFile(appContext, appContext.getPackageName() + ".provider", apkFile), "application/vnd.android.package-archive");
            } else {
                intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            appContext.startActivity(intent);

            AILog.d(NAME, "openPackageInstaller. invoke system installer success. " + packageSource.getLogInfo());
            return true;
        } catch (Exception e) {
            e.printStackTrace();

            AILog.w(NAME, "start system installer failed. " + packageSource.getLogInfo());
            appInstaller.getEventMonitor().onUnableStartPackageInstaller(packageSource);
            return false;
        }
    }

    /**
     * 安装成功
     */
    @SuppressWarnings("WeakerAccess")
    protected void onInstallSuccess() {

    }
}