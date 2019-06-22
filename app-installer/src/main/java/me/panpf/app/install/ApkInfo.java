package me.panpf.app.install;

import androidx.annotation.NonNull;

public class ApkInfo {
    @NonNull
    public String name;
    @NonNull
    public String packageName;
    @NonNull
    public String versionName;
    public int versionCode;

    public ApkInfo(@NonNull String name, @NonNull String packageName, @NonNull String versionName, int versionCode) {
        this.name = name;
        this.packageName = packageName;
        this.versionName = versionName;
        this.versionCode = versionCode;
    }
}
