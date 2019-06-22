package me.panpf.app.install.xpk;

import android.content.Context;
import androidx.annotation.NonNull;
import me.panpf.app.install.AppInstaller;
import me.panpf.app.install.BaseInstallTask;
import me.panpf.app.install.PackageSource;
import me.panpf.app.install.TaskMatcher;

public class XpkTaskMatcher implements TaskMatcher {
    @NonNull
    private Context appContext;
    @NonNull
    private AppInstaller appInstaller;

    public XpkTaskMatcher(@NonNull Context context, @NonNull AppInstaller appInstaller) {
        this.appContext = context.getApplicationContext();
        this.appInstaller = appInstaller;
    }

    @Override
    public boolean match(@NonNull PackageSource packageSource) {
        return packageSource.getFile().getPath().toLowerCase().endsWith(".xpk");
    }

    @NonNull
    @Override
    public BaseInstallTask createInstallTask(@NonNull PackageSource packageSource) {
        return new XpkInstallTask(appContext, appInstaller, packageSource);
    }
}
