package me.panpf.app.install;

import android.content.Context;
import androidx.annotation.NonNull;

import java.io.File;

public interface PackageChecker {
    boolean onCheck(@NonNull Context appContext, @NonNull PackageSource packageSource, @NonNull File apkFile, @NonNull EventMonitor eventMonitor);
}
