package me.panpf.app.install;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 安装过程中所有的事件回调
 */
public interface EventMonitor {
    void onTaskStart(@NonNull PackageSource packageSource);

    void onTaskSuccess(@NonNull PackageSource packageSource);

    void onTaskError(@NonNull PackageSource packageSource);

    void onNoMatchedInstaller(@NonNull PackageSource packageSource);

    void onRepeatInstall(@NonNull PackageSource packageSource, @InstallStatus int oldInstallStatus);

    void onFileLost(@NonNull PackageSource packageSource);

    void onApkZipInvalid(@NonNull PackageSource packageSource, @NonNull String packageMD5, @Nullable Exception e);

    void onApkPackageInvalid(@NonNull PackageSource packageSource, @NonNull String packageMD5);

    void onGetInstalledApkSignatureException(@NonNull PackageSource packageSource, @NonNull Exception e);

    void onGetNewApkSignatureException(@NonNull PackageSource packageSource, @NonNull String packageMd5, @NonNull Exception e);

    void onApkSignatureNotMatched(@NonNull PackageSource packageSource, @NonNull String installedAppSignature, @NonNull String newAppSignature);

    void onUnableStartPackageInstaller(@NonNull PackageSource packageSource);

    void onRemindEnableRootInstall(@NonNull PackageSource packageSource);

    void onRemindRootInstallSuccess(@NonNull PackageSource packageSource);

    void onRemindEnableAutoInstall(@NonNull PackageSource packageSource);
}
