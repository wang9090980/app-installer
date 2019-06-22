package me.panpf.app.install;

import android.app.Application;
import androidx.annotation.NonNull;

public interface TaskMatcher {
    boolean match(@NonNull PackageSource packageSource);

    @NonNull
    BaseInstallTask createInstallTask(@NonNull Application application, @NonNull Preferences preferences, @NonNull EventMonitor eventMonitor,
                                      @NonNull TaskManager taskManager, @NonNull PackageSource packageSource);
}
