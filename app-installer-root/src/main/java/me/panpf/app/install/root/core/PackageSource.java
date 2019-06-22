package me.panpf.app.install.core;

import android.content.Context;

import me.panpf.app.install.InstallStatus;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface PackageSource {
    @NonNull
    File getFile();

    @NonNull
    String getId();

    @NonNull
    String getShowAppName();

    @Nullable
    String getPackageName();

    @Nullable
    String getVersionName();

    int getVersionCode();

    @Nullable
    String getAppIconUrl();

    @Nullable
    String getRealAppName();

    @Nullable
    ApkInfo getApkInfo();

    void setApkInfo(@NonNull ApkInfo apkInfo);

    @NonNull
    String getLogInfo();

    long getStartTime();

    @InstallStatus
    int getStatus();

    void setStatus(@InstallStatus int status);

    long getTotalLength();

    long getCompletedLength();

    void setTotalLength(long totalLength);

    void setCompletedLength(long completedLength);

    void onPost(@NonNull Context context);

    @Nullable
    String getRequests();
}
