package me.panpf.app.install.xpk.mount;

import android.content.Context;
import androidx.annotation.NonNull;
import me.panpf.app.install.AILog;
import me.panpf.androidx.Androidx;

public class MountInitTask implements Runnable {
    private static boolean repaired = false;
    @NonNull
    private Context appContext;
    @NonNull
    private MountManager mountManager;

    MountInitTask(@NonNull Context appContext, @NonNull MountManager mountManager) {
        this.appContext = appContext;
        this.mountManager = mountManager;
    }

    @Override
    public void run() {
        if (Androidx.isRooted()) {
            // 有 ROOT 权限的时候就检查一下是否有需要修复的
            if (!repaired && mountManager.isUsable(true)) {
                repaired = true;
                mountManager.oneKeyRepair(null, null);
            }
        } else {
            // 没有 ROOT 权限却开启了数据包挂载功能，就直接关闭
            if (mountManager.isEnabled()) {
                AILog.w("MountInitTask", "Close mount");
                mountManager.setEnable(false);
            }
        }
    }
}
