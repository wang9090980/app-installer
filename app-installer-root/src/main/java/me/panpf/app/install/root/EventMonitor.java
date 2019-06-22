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

    void onRootInstallFailed(@NonNull PackageSource packageSource, @NonNull CmdResult cmdResult, boolean rootInstallFailedCountLimit, boolean permissionDenied);

    void onRootInstallFailedNotInstalled(@NonNull PackageSource packageSource, @NonNull File apkFile, long apkSize, long freeSize, boolean noSpace);

    void onUnableStartPackageInstaller(@NonNull PackageSource packageSource);

    void onRemindAndGuideEnableAutoInstall(@NonNull Activity activity);

    void onOpenAccessibilityPageFailed(@NonNull Activity activity);

    void onAutoInstallServiceClosedUnexpectedly(@NonNull Activity activity);

    void onRemindEnableRootInstall(@NonNull PackageSource packageSource);

    void onRemindRootInstallSuccess(@NonNull PackageSource packageSource);

    void onRemindEnableAutoInstall(@NonNull PackageSource packageSource);

    void onAutoInstallServiceConnected(boolean opened);

    void onAutoInstallServiceUnbind();

    void onClickAutoInstallTargetButton(@NonNull Configuration configuration, @NonNull TargetButton button, @NonNull AccessibilityNodeInfo nodeInfo);
}
