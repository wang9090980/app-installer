package me.panpf.app.install;

import android.app.Application;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.text.format.Formatter;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import me.panpf.javax.security.Digestx;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

/**
 * APK 安装器
 */
public class ApkInstallTask extends BaseInstallTask {
    private static final String NAME = "ApkInstallTask";

    private File apkFile;

    public ApkInstallTask(@NonNull Application application, @NonNull Preferences preferences, @NonNull EventMonitor eventMonitor,
                          @NonNull TaskManager taskManager, @NonNull PackageSource packageSource) {
        super(application, preferences, eventMonitor, taskManager, packageSource);
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

        if (!checkApkInfo()) {
            return false;
        }

        if (!preInstall()) {
            return false;
        }

        if (!startPackageInstaller()) {
            return false;
        }

        // todo root 签名校验

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
            eventMonitor.onFileLost(packageSource);
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
            eventMonitor.onApkPackageInvalid(packageSource, packageMd5);
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
     * 安装之前的准备工作
     */
    protected boolean preInstall() {
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
            eventMonitor.onUnableStartPackageInstaller(packageSource);
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