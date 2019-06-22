package me.panpf.app.install;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class EventMonitorImpl implements EventMonitor {

    @Override
    public void onTaskStart(@NonNull PackageSource packageSource) {

    }

    @Override
    public void onTaskSuccess(@NonNull PackageSource packageSource) {

    }

    @Override
    public void onTaskError(@NonNull PackageSource packageSource) {

    }

    @Override
    public void onNoMatchedInstaller(@NonNull PackageSource packageSource) {
    }

    @Override
    public void onRepeatInstall(@NonNull PackageSource packageSource, @InstallStatus int oldInstallStatus) {
    }

    @Override
    public void onFileLost(@NonNull PackageSource packageSource) {
    }

    @Override
    public void onApkZipInvalid(@NonNull PackageSource packageSource, @NonNull String packageMD5, @Nullable Exception e) {

    }

    @Override
    public void onApkPackageInvalid(@NonNull PackageSource packageSource, @NonNull String packageMD5) {
    }

    @Override
    public void onGetInstalledApkSignatureException(@NonNull PackageSource packageSource, @NonNull Exception e) {
    }

    @Override
    public void onGetNewApkSignatureException(@NonNull PackageSource packageSource, @Nullable String packageMd5, @NonNull Exception e) {

    }

    @Override
    public void onApkSignatureNotMatched(@NonNull PackageSource packageSource, @NonNull String installedAppSignature, @NonNull String newAppSignature) {
    }

    @Override
    public void onUnableStartPackageInstaller(@NonNull PackageSource packageSource) {
    }

    @Override
    public void onRemindEnableRootInstall(@NonNull PackageSource packageSource) {

    }

    @Override
    public void onRemindRootInstallSuccess(@NonNull PackageSource packageSource) {

    }

    @Override
    public void onRemindEnableAutoInstall(@NonNull PackageSource packageSource) {

    }
}
