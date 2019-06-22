package me.panpf.app.install;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface InstallStatusListener {
    /**
     * 安装状态变化
     *
     * @param key            key 会有两种. com.google.map:121；/sdcard/google_map.apk
     * @param appPackageName 包名
     * @param appVersionCode 版本号
     * @param installStatus  新的状态
     */
    void onInstallStatusChanged(@NonNull String key, @Nullable String appPackageName, int appVersionCode, @InstallStatus int installStatus);
}
