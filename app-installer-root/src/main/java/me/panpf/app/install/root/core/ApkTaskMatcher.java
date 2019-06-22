package me.panpf.app.install.core;

import android.content.Context;
import androidx.annotation.NonNull;

import me.panpf.app.install.AppInstaller;

public class ApkTaskMatcher implements TaskMatcher {
    @NonNull
    private Context appContext;
    @NonNull
    private AppInstaller appInstaller;

    ApkTaskMatcher(@NonNull Context context, @NonNull AppInstaller appInstaller) {
        this.appContext = context.getApplicationContext();
        this.appInstaller = appInstaller;
    }

    @Override
    public boolean match(@NonNull PackageSource packageSource) {
        return packageSource.getFile().getPath().toLowerCase().endsWith(".apk");
    }

    @NonNull
    @Override
    public BaseInstallTask createInstallTask(@NonNull PackageSource packageSource) {
        return new ApkInstallTask(appContext, appInstaller, packageSource);
    }
}
