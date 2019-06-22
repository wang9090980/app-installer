package me.panpf.app.install;

import android.app.Activity;
import android.view.accessibility.AccessibilityNodeInfo;

import me.panpf.app.install.auto.Configuration;
import me.panpf.app.install.auto.TargetButton;
import me.panpf.app.install.core.PackageSource;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.panpf.shell.CmdResult;

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
    public void onRootInstallFailed(@NonNull PackageSource packageSource, @NonNull CmdResult cmdResult, boolean rootInstallFailedCountLimit, boolean permissionDenied) {

    }

    @Override
    public void onRootInstallFailedNotInstalled(@NonNull PackageSource packageSource, @NonNull File apkFile, long apkSize, long freeSize, boolean noSpace) {
    }

    @Override
    public void onUnableStartPackageInstaller(@NonNull PackageSource packageSource) {
    }

    @Override
    public void onRemindAndGuideEnableAutoInstall(@NonNull Activity activity) {

    }

    @Override
    public void onOpenAccessibilityPageFailed(@NonNull Activity activity) {

    }

    @Override
    public void onAutoInstallServiceClosedUnexpectedly(@NonNull Activity activity) {

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

    @Override
    public void onAutoInstallServiceConnected(boolean opened) {

    }

    @Override
    public void onAutoInstallServiceUnbind() {

    }

    @Override
    public void onClickAutoInstallTargetButton(@NonNull Configuration configuration, @NonNull TargetButton button, @NonNull AccessibilityNodeInfo nodeInfo) {

    }
}
