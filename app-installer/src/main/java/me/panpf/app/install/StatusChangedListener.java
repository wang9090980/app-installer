package me.panpf.app.install;

import androidx.annotation.NonNull;

public interface StatusChangedListener {
    void onStatusChanged(@NonNull String key, @InstallStatus int newStatus);
}
