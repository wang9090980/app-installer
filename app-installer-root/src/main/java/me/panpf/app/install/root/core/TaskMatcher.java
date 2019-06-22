package me.panpf.app.install.core;

import androidx.annotation.NonNull;

public interface TaskMatcher {
    boolean match(@NonNull PackageSource packageSource);

    @NonNull
    BaseInstallTask createInstallTask(@NonNull PackageSource packageSource);
}
