package me.panpf.app.install.xpk.mount;

import android.content.Context;
import androidx.annotation.NonNull;
import me.panpf.app.install.AILog;
import me.panpf.app.packages.PackageUtils;

public class UmountTask implements Runnable {
    @NonNull
    private Context appContext;
    @NonNull
    private String packageName;
    @NonNull
    private MountManager mountManager;

    public UmountTask(@NonNull Context context, @NonNull String packageName, @NonNull MountManager mountManager) {
        this.appContext = context.getApplicationContext();
        this.packageName = packageName;
        this.mountManager = mountManager;
    }

    @Override
    public void run() {
        if (PackageUtils.isInstalled(appContext, packageName)) {
            return;
        }

        // 取消挂载数据包目录
        boolean isSuccess = mountManager.umount(packageName);
        if (isSuccess) {
            AILog.i("umount. success. " + packageName);
        } else {
            AILog.w("umount. failed. " + packageName);
        }
    }
}
