package me.panpf.app.install;

import android.app.Application;
import androidx.annotation.NonNull;

public class ApkTaskMatcher implements TaskMatcher {

    @Override
    public boolean match(@NonNull PackageSource packageSource) {
        return packageSource.getFile().getPath().toLowerCase().endsWith(".apk");
    }

    @NonNull
    @Override
    public BaseInstallTask createInstallTask(@NonNull Application application, @NonNull Preferences preferences, @NonNull EventMonitor eventMonitor,
                                             @NonNull TaskManager taskManager, @NonNull PackageSource packageSource) {
        return new ApkInstallTask(application, preferences, eventMonitor, taskManager, packageSource);
    }
}
