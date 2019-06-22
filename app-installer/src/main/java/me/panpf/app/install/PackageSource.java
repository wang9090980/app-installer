package me.panpf.app.install;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;

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

    @InstallStatus
    int getStatus();

    void setStatus(@InstallStatus int status);

    long getDecompressTotalLength();

    long getDecompressCompletedLength();

    void setDecompressTotalLength(long totalLength);

    void setDecompressCompletedLength(long completedLength);

    void onPost(@NonNull Context context);

    @Nullable
    String getRequests();
}
