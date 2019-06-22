package me.panpf.app.install.xpk.mount;

import androidx.annotation.Nullable;
import me.panpf.shell.CmdResult;

/**
 * 修复监听器
 */
public interface MountRepairListener {
    void onFinished(@Nullable CmdResult result);
}
