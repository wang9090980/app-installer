package me.panpf.app.install.auto;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;

import me.panpf.app.install.AILog;
import me.panpf.app.packages.PackageUtils;

public class UnbindTask implements Runnable {

    @NonNull
    private Context appContext;
    @NonNull
    private PackageUtils.AppPackage appPackage;
    @NonNull
    private BindManager bindManager;

    public UnbindTask(@NonNull Context context, @NonNull PackageUtils.AppPackage appPackage, @NonNull BindManager bindManager) {
        this.appContext = context.getApplicationContext();
        this.appPackage = appPackage;
        this.bindManager = bindManager;
    }

    @Override
    public void run() {
        PackageInfo packageInfo = null;
        try {
            packageInfo = appContext.getPackageManager().getPackageInfo(appPackage.packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo != null) {
            CharSequence appLabel = packageInfo.applicationInfo != null ? packageInfo.applicationInfo.loadLabel(appContext.getPackageManager()) : null;
            String appName = appLabel != null ? appLabel.toString() : "";
            AILog.d("UnbindTask", "The app 【" + appName + "】has been installed successfully");
            bindManager.unbindApp(appName);
        }
    }
}
