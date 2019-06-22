package me.panpf.app.install.xpk;

import androidx.annotation.NonNull;
import me.panpf.app.install.PackageSource;
import me.panpf.shell.CmdResult;

import java.io.File;

public interface XpkEventMonitor {

    public void onXpkZipInvalid(PackageSource packageSource, String packageMD5, Exception e);

    public void onXpkMissingFile(PackageSource packageSource, String packageMD5, XpkInfo.MissingFileException e);

    public void onXpkInfoIncomplete(PackageSource packageSource, String packageMD5, XpkInfo.InfoIncompleteException e);

    public void onXpkParseError(PackageSource packageSource, String packageMD5, Exception e);

    public void onNoSpaceForDecompressApk(PackageSource packageSource, long apkNeedSize, long saveSize, long freeSize);

    public void onUnableCreateApkDir(PackageSource packageSource, File dir);

    public void onUnableCreateApkFile(PackageSource packageSource, File apkOutFile);

    public void onDecompressApkZipException(PackageSource packageSource, Exception e);

    public void onDecompressApkIOException(PackageSource packageSource, Exception e);

    public void onApkNotExistAfterDecompress(PackageSource packageSource);

    public void onNoSpaceForDecompressDataPacket(PackageSource packageSource, long dataNeedSize, long saveSize, long dataDirFreeSize);

    public void onUnableCreateDataPacketDir(PackageSource packageSource, File dataOutDir);

    public void onUnableCreateDataPacketFile(PackageSource packageSource, File file);

    public void onDecompressDataPacketZipException(PackageSource packageSource, Exception e);

    public void onDecompressDataPacketIOException(PackageSource packageSource, Exception e);

    public void onNoSpaceWritingData(PackageSource packageSource);


    /* *************************** 挂载相关 **************************** */

    public void onRemindEnabledMount(PackageSource packageSource, long dataNeedSize, long SAVE_SIZE, long dataDirFreeSize);

    public void onMountSuccess();

    public void onNoSpaceAfterMountSuccess(PackageSource packageSource, @NonNull CmdResult cmdResult);

    public void onMountFailed(@NonNull PackageSource packageSource, @NonNull CmdResult cmdResult);
}
