package me.panpf.app.install.xpk.mount;

import me.panpf.shell.CmdResult;

public class MountResult {
    public boolean confirmResult;
    public CmdResult cmdResult;

    public MountResult(boolean confirmResult, CmdResult cmdResult) {
        this.confirmResult = confirmResult;
        this.cmdResult = cmdResult;
    }
}
