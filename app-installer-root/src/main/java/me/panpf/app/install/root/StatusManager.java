package me.panpf.app.install;

import android.annotation.SuppressLint;

import me.panpf.app.install.core.PackageSource;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;

public class StatusManager {
    @NonNull
    private final ArrayMap<String, Integer> statusMap = new ArrayMap<>();

    @NonNull
    private AppInstaller appInstaller;

    StatusManager(@NonNull AppInstaller appInstaller) {
        this.appInstaller = appInstaller;
    }

    @NonNull
    private String genKey(@NonNull final String packageName, final int versionCode) {
        return packageName + ":" + versionCode;
    }

    @InstallStatus
    @SuppressLint("WrongConstant")
    int query(@NonNull String key) {
        Integer status;
        synchronized (statusMap) {
            status = statusMap.get(key);
        }
        return status != null ? status : -1;
    }

    @InstallStatus
    @SuppressLint("WrongConstant")
    int query(@NonNull String packageName, int versionCode) {
        return query(genKey(packageName, versionCode));
    }

    @AnyThread
    public void setStatus(@NonNull PackageSource packageSource, @InstallStatus int installStatus) {
        packageSource.setStatus(installStatus);

        synchronized (statusMap) {
            statusMap.put(packageSource.getId(), installStatus);
        }
        appInstaller.getListeners().postCallbackStatus(packageSource.getId(),
                packageSource.getPackageName(), packageSource.getVersionCode(), installStatus);
    }

    @AnyThread
    @InstallStatus
    @SuppressLint("WrongConstant")
    public int delete(@NonNull PackageSource packageSource) {
        packageSource.setStatus(-1);

        Integer status;
        synchronized (statusMap) {
            status = statusMap.remove(packageSource.getId());
        }
        appInstaller.getListeners().postCallbackStatus(packageSource.getId(),
                packageSource.getPackageName(), packageSource.getVersionCode(), -1);
        return status != null ? status : -1;
    }
}
