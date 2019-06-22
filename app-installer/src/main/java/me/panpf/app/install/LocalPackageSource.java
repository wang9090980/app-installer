package me.panpf.app.install;

import android.content.Context;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.util.Locale;

public class LocalPackageSource implements PackageSource {
    @NonNull
    private String id;

    @NonNull
    private String appName;

    @NonNull
    private File packageFile;

    @Nullable
    private ApkInfo apkInfo;

    @InstallStatus
    private int status;
    private long totalLength;
    private long completedLength;

    @SuppressWarnings("WeakerAccess")
    public LocalPackageSource(@NonNull File packageFile, @Nullable String appName) {
        this.packageFile = packageFile;
        this.id = packageFile.getPath();
        this.appName = !TextUtils.isEmpty(appName) ? appName : packageFile.getName();
    }

    public LocalPackageSource(@NonNull String packageFilePath, @Nullable String appName) {
        this(new File(packageFilePath), appName);
    }

    @NonNull
    @Override
    public File getFile() {
        return packageFile;
    }

    @NonNull
    @Override
    public String getId() {
        return id;
    }

    @NonNull
    @Override
    public String getShowAppName() {
        return appName;
    }

    @Nullable
    @Override
    public String getPackageName() {
        return apkInfo != null ? apkInfo.packageName : null;
    }

    @Nullable
    @Override
    public String getVersionName() {
        return apkInfo != null ? apkInfo.versionName : null;
    }

    @Override
    public int getVersionCode() {
        return apkInfo != null ? apkInfo.versionCode : 0;
    }

    @Nullable
    @Override
    public String getAppIconUrl() {
        return null;
    }

    @Nullable
    @Override
    public String getRealAppName() {
        return apkInfo != null ? apkInfo.name : null;
    }

    @Nullable
    @Override
    public ApkInfo getApkInfo() {
        return apkInfo;
    }

    @Override
    public void setApkInfo(@NonNull ApkInfo apkInfo) {
        this.apkInfo = apkInfo;
    }

    @NonNull
    @Override
    public String getLogInfo() {
        return String.format(Locale.US, "Local(%s:%s:%s:%d)", packageFile.getPath(), getShowAppName(), getPackageName(), getVersionCode());
    }

    @Override
    @InstallStatus
    public int getStatus() {
        return status;
    }

    @Override
    public void setStatus(@InstallStatus int status) {
        this.status = status;
    }

    @Override
    public long getDecompressTotalLength() {
        return totalLength;
    }

    @Override
    public long getDecompressCompletedLength() {
        return completedLength;
    }

    @Override
    public void setDecompressTotalLength(long totalLength) {
        this.totalLength = totalLength;
    }

    @Override
    public void setDecompressCompletedLength(long completedLength) {
        this.completedLength = completedLength;
    }

    @Override
    public void onPost(@NonNull Context context) {

    }

    @Nullable
    @Override
    public String getRequests() {
        return null;
    }
}
