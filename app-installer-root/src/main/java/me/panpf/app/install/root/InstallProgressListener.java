package me.panpf.app.install;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface InstallProgressListener {
    /**
     * 安装状态变化
     *
     * @param key             key 会有两种. com.google.map:121；/sdcard/google_map.apk
     * @param appPackageName  包名
     * @param appVersionCode  版本号
     * @param completedLength 已完成长度
     * @param totalLength     总长度
     */
    void onInstallProgressChanged(@NonNull String key, @Nullable String appPackageName, int appVersionCode, long completedLength, long totalLength);
}
