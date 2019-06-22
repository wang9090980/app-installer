package me.panpf.app.install;

import androidx.annotation.NonNull;

public interface DecompressProgressChangedListener {
    void onDecompressProgressChanged(@NonNull String key, long totalLength, long completedLength);
}
