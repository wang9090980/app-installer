package me.panpf.app.install.xpk;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.panpf.app.install.*;
import me.panpf.androidx.os.storage.Storagex;
import me.panpf.javax.io.Filex;
import me.panpf.javax.io.UnableCreateDirException;
import me.panpf.javax.io.UnableCreateFileException;
import me.panpf.javax.security.Digestx;
import me.panpf.javax.util.Formatx;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import org.xmlpull.v1.XmlPullParserException;

import java.io.*;
import java.util.List;
import java.util.Locale;

public class XpkInstallTask extends ApkInstallTask {
    private static final String NAME = "XpkInstallTask";
    private static final int SAVE_SIZE = 1024 * 1024 * 50; // 解压 XPK 时，预留空间大小
    private static final String XPK_APKS = "xpk_apks";

    private File apkOutFile;
    private File dataOutDir;

    private XpkInfo xpkInfo;
    private ZipFile xpkZipFile;

    private AppInstaller myAppInstaller;

    XpkInstallTask(@NonNull Context context, @NonNull AppInstaller appInstaller, @NonNull PackageSource packageSource) {
        super(context, appInstaller, packageSource);
        this.myAppInstaller = appInstaller;
    }

    @Override
    protected File getApkFile() {
        return apkOutFile;
    }

    /* ******************* 核心安装流程 ********************* */

    @Override
    protected boolean checkPackage() {
        return super.checkPackage() && readXpkInfo();
    }

    private boolean readXpkInfo() {
        XpkInfo xpkInfo;
        try {
            this.xpkZipFile = new ZipFile(packageSource.getFile());
            xpkInfo = XpkInfo.parse(xpkZipFile);
        } catch (XpkInfo.InvalidZipException e) {
            e.printStackTrace();
            String packageMd5;
            try {
                packageMd5 = Digestx.getMD5(packageSource.getFile());
            } catch (IOException ee) {
                ee.printStackTrace();
                packageMd5 = "";
            }
            AILog.w(NAME, String.format(Locale.US, "Parsing xpk that the invalid zip. File：%s/%d/%s. %s",
                    packageMd5, packageSource.getFile().length(), Formatx.fileSize(packageSource.getFile().length()),
                    packageSource.getLogInfo()));
            EventMonitor eventMonitor = appInstaller.getEventMonitor();
            if (eventMonitor instanceof XpkEventMonitor) {
                ((XpkEventMonitor) eventMonitor).onXpkZipInvalid(packageSource, packageMd5, e);
            }
            return false;
        } catch (XpkInfo.MissingFileException e) {
            e.printStackTrace();

            String packageMd5;
            try {
                packageMd5 = Digestx.getMD5(packageSource.getFile());
            } catch (IOException ee) {
                ee.printStackTrace();
                packageMd5 = "";
            }
            AILog.w(NAME, String.format(Locale.US, "Parsing xpk that the missing file. %s. File：%s/%d/%s. %s",
                    e.getMessage(),
                    packageMd5, packageSource.getFile().length(), Formatx.fileSize(packageSource.getFile().length()),
                    packageSource.getLogInfo()));
            EventMonitor eventMonitor = appInstaller.getEventMonitor();
            if (eventMonitor instanceof XpkEventMonitor) {
                ((XpkEventMonitor) eventMonitor).onXpkMissingFile(packageSource, packageMd5, e);
            }
            return false;
        } catch (XpkInfo.InfoIncompleteException e) {
            e.printStackTrace();

            String packageMd5;
            try {
                packageMd5 = Digestx.getMD5(packageSource.getFile());
            } catch (IOException ee) {
                ee.printStackTrace();
                packageMd5 = "";
            }
            AILog.w(NAME, String.format(Locale.US, "Parsing xpk that the info incomplete. %s. File：%s/%d/%s. %s",
                    e.getMessage(),
                    packageMd5, packageSource.getFile().length(), Formatx.fileSize(packageSource.getFile().length()),
                    packageSource.getLogInfo()));
            EventMonitor eventMonitor = appInstaller.getEventMonitor();
            if (eventMonitor instanceof XpkEventMonitor) {
                ((XpkEventMonitor) eventMonitor).onXpkInfoIncomplete(packageSource, packageMd5, e);
            }
            return false;
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();

            String packageMd5;
            try {
                packageMd5 = Digestx.getMD5(packageSource.getFile());
            } catch (IOException ee) {
                ee.printStackTrace();
                packageMd5 = "";
            }
            AILog.w(NAME, String.format(Locale.US, "Parsing xpk error. File：%s/%d/%s. %s. %s",
                    packageMd5, packageSource.getFile().length(), Formatx.fileSize(packageSource.getFile().length()),
                    e.toString(), packageSource.getLogInfo()));
            EventMonitor eventMonitor = appInstaller.getEventMonitor();
            if (eventMonitor instanceof XpkEventMonitor) {
                ((XpkEventMonitor) eventMonitor).onXpkParseError(packageSource, packageMd5, e);
            }
            return false;
        } catch (ZipException e) {
            e.printStackTrace();

            String message = e.getMessage();
            if (message != null && (message.contains("file does not exist") || message.contains("No such file or directory"))) {
                AILog.w(NAME, "Parsing xpk that the file lost. " + packageSource.getLogInfo());
                appInstaller.getEventMonitor().onFileLost(packageSource);
            } else {
                String packageMd5;
                try {
                    packageMd5 = Digestx.getMD5(packageSource.getFile());
                } catch (IOException ee) {
                    ee.printStackTrace();
                    packageMd5 = "";
                }
                AILog.w(NAME, String.format(Locale.US, "Parsing xpk that the invalid zip. File：%s/%d/%s. %s. %s",
                        packageMd5, packageSource.getFile().length(), Formatx.fileSize(packageSource.getFile().length()),
                        e.getMessage(), packageSource.getLogInfo()));
                EventMonitor eventMonitor = appInstaller.getEventMonitor();
                if (eventMonitor instanceof XpkEventMonitor) {
                    ((XpkEventMonitor) eventMonitor).onXpkZipInvalid(packageSource, packageMd5, e);
                }
            }
            return false;
        }

        packageSource.setApkInfo(new ApkInfo(xpkInfo.getAppName(), xpkInfo.getPackageName(), xpkInfo.getVersionName(), xpkInfo.getVersionCode()));

        this.xpkInfo = xpkInfo;
        return true;
    }

    @Override
    protected boolean preCheckApk() {
        return super.preCheckApk() && checkApkSpace() && decompressApk();
    }

    private boolean checkApkSpace() {
        long apkNeedSize = xpkInfo.getApkSize() + SAVE_SIZE;

        String apkFileName = packageSource.getFile().getName() + ".apk";
        File targetFile = Storagex.getFileIn(Storagex.getAppExternalCacheDirs(appContext), XPK_APKS + File.separator + apkFileName, apkNeedSize, true);
        if (targetFile != null) {
            apkOutFile = targetFile;
            return true;
        } else {
            AILog.w(NAME, "No space for decompress apk. " + packageSource.getLogInfo());
            EventMonitor eventMonitor = appInstaller.getEventMonitor();
            if (eventMonitor instanceof XpkEventMonitor) {
                ((XpkEventMonitor) eventMonitor).onNoSpaceForDecompressApk(packageSource, apkNeedSize, SAVE_SIZE, Storagex.getExternalStorageAvailableBytes());
            }
            return false;
        }
    }

    private boolean decompressApk() {
        InputStream inputStream = null;
        try {
            //noinspection unchecked
            List<FileHeader> fileHeaders = xpkZipFile.getFileHeaders();
            if (fileHeaders != null && fileHeaders.size() > 0) {
                // 兼容备份 xpk 中的 apk
                for (FileHeader fileHeader : fileHeaders) {
                    if (fileHeader != null && fileHeader.getFileName().endsWith(".apk")) {
                        inputStream = xpkZipFile.getInputStream(fileHeader);
                        break;
                    }
                }
            } else {
                inputStream = XpkInfo.getApkInputStream(xpkZipFile);
            }
            writeToDestination(inputStream, apkOutFile, null);
        } catch (UnableCreateFileException e) {
            e.printStackTrace();

            AILog.w(NAME, "Unable create file for decompress apk. " + packageSource.getLogInfo());
            EventMonitor eventMonitor = appInstaller.getEventMonitor();
            if (eventMonitor instanceof XpkEventMonitor) {
                ((XpkEventMonitor) eventMonitor).onUnableCreateApkFile(packageSource, apkOutFile);
            }
            return false;
        } catch (UnableCreateDirException e) {
            e.printStackTrace();

            AILog.w(NAME, "Unable create dir for decompress apk. " + packageSource.getLogInfo());
            EventMonitor eventMonitor = appInstaller.getEventMonitor();
            if (eventMonitor instanceof XpkEventMonitor) {
                ((XpkEventMonitor) eventMonitor).onUnableCreateApkDir(packageSource, e.file);
            }
            return false;
        } catch (ZipException e) {
            e.printStackTrace();

            final String message = e.getMessage();
            if (message != null && (message.contains("file does not exist") || message.contains("No such file or directory"))) {
                AILog.w(NAME, "Zip file does not exist for decompress apk. " + packageSource.getLogInfo());
                appInstaller.getEventMonitor().onFileLost(packageSource);
            } else {
                AILog.w(NAME, "Zip exception for decompress apk. " + packageSource.getLogInfo());
                EventMonitor eventMonitor = appInstaller.getEventMonitor();
                if (eventMonitor instanceof XpkEventMonitor) {
                    ((XpkEventMonitor) eventMonitor).onDecompressApkZipException(packageSource, e);
                }
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();

            String exceptionMessage = e.getMessage();
            if (exceptionMessage != null && exceptionMessage.contains("No space")) {
                AILog.w(NAME, "Decompress write failed no space. " + packageSource.getLogInfo());
                EventMonitor eventMonitor = appInstaller.getEventMonitor();
                if (eventMonitor instanceof XpkEventMonitor) {
                    ((XpkEventMonitor) eventMonitor).onNoSpaceWritingData(packageSource);
                }
            } else {
                String message = e.getMessage();
                if (message != null) {
                    if (message.contains("Permission denied")) {
                        AILog.w(NAME, "Permission denied for decompress apk. " + packageSource.getLogInfo());
                    } else if (message.contains("Read-only file system")) {
                        AILog.w(NAME, "apk file Read-only file system for decompress apk. " + packageSource.getLogInfo());
                    } else {
                        AILog.w(NAME, "write apk failed for decompress apk. " + packageSource.getLogInfo());
                    }
                } else {
                    AILog.w(NAME, "write apk failed for decompress apk. " + packageSource.getLogInfo());
                }
                EventMonitor eventMonitor = appInstaller.getEventMonitor();
                if (eventMonitor instanceof XpkEventMonitor) {
                    ((XpkEventMonitor) eventMonitor).onDecompressApkIOException(packageSource, e);
                }
            }
            return false;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (!apkOutFile.exists()) {
            AILog.w(NAME, "Apk not exists after decompress: " + apkOutFile.getPath() + ". " + packageSource.getLogInfo());
            EventMonitor eventMonitor = appInstaller.getEventMonitor();
            if (eventMonitor instanceof XpkEventMonitor) {
                ((XpkEventMonitor) eventMonitor).onApkNotExistAfterDecompress(packageSource);
            }
            return false;
        }

        return true;
    }

    @Override
    protected boolean preInstall() {
        return super.preInstall() && checkDataSpace() && decompressDataPacket();
    }

    private boolean checkDataSpace() {
        dataOutDir = new File(xpkInfo.getDestination());
        long dataNeedSize = xpkInfo.getDataSize() + SAVE_SIZE;

        // 剩余空间不够时删除旧的数据文件再试
        long dataDirFreeSize = Storagex.getAvailableBytes(dataOutDir, 0);
        if (dataDirFreeSize < dataNeedSize) {
            // 过滤掉 /sdcard/android 类型的数据包目录
            if (!dataOutDir.getPath().toLowerCase().endsWith("android")) {
                Filex.cleanRecursively(dataOutDir);
            }
        }

        dataDirFreeSize = Storagex.getAvailableBytes(dataOutDir, 0);
        if (dataDirFreeSize < dataNeedSize) {
            // 尝试挂载. 0：无需挂载；1：挂载成功；2：需要提醒用户开启挂载功能；-1：挂载失败
            TryMoundResult mountResult = tryMountDataDir();

            // 提醒用户空间不足，可以开启数据包挂载功能
            if (mountResult.code == 2) {
                // 提醒用户并阻塞线程等待用户选择
                EventMonitor eventMonitor = appInstaller.getEventMonitor();
                if (eventMonitor instanceof XpkEventMonitor) {
                    ((XpkEventMonitor) eventMonitor).onRemindEnabledMount(packageSource, dataNeedSize, SAVE_SIZE, dataDirFreeSize);
                }

                // 再次尝试挂载
                mountResult = tryMountDataDir();

                // 依然提醒用户开启挂载，说明用户拒绝开启数据包挂载功能，可以 over 了
                if (mountResult.code == 2) {
                    return false;
                }
            }

            // 用户设备不满足挂载条件，直接提示空间不足
            if (mountResult.code == 0) {
                AILog.w(NAME, "No space for decompress data packet. " + dataOutDir.getPath() + ". " + packageSource.getLogInfo());
                EventMonitor eventMonitor = appInstaller.getEventMonitor();
                if (eventMonitor instanceof XpkEventMonitor) {
                    ((XpkEventMonitor) eventMonitor).onNoSpaceForDecompressDataPacket(packageSource, dataNeedSize, SAVE_SIZE, dataDirFreeSize);
                }
                return false;
            } else if (mountResult.code == -1) {
                // 挂载失败
                AILog.w(NAME, "Mount failed for decompress data packet. " + packageSource.getLogInfo());
                EventMonitor eventMonitor = appInstaller.getEventMonitor();
                if (eventMonitor instanceof XpkEventMonitor) {
                    ((XpkEventMonitor) eventMonitor).onMountFailed(packageSource, mountResult.cmdResult);
                }
                return false;
            } else if (mountResult.code == 1) {
                // 挂载成功
                EventMonitor eventMonitor = appInstaller.getEventMonitor();
                if (eventMonitor instanceof XpkEventMonitor) {
                    ((XpkEventMonitor) eventMonitor).onMountSuccess();
                }

                // 再次检查空间
                dataDirFreeSize = Storagex.getAvailableBytes(dataOutDir, 0);
                if (dataDirFreeSize < dataNeedSize) {
                    AILog.w(NAME, "No space after mount success for decompress data packet. " + packageSource.getLogInfo());
                    if (eventMonitor instanceof XpkEventMonitor) {
                        ((XpkEventMonitor) eventMonitor).onNoSpaceAfterMountSuccess(packageSource, mountResult.cmdResult);
                    }
                    return false;
                } else {
                    AILog.d(NAME, "checkSpace. after mounting space enough. " + packageSource.getLogInfo());
                }
            }
        }
        return true;
    }

//    /**
//     * 尝试挂载数据包
//     *
//     * @return 0：无需挂载；1：挂载成功；2：需要提醒用户开启挂载功能；-1：挂载失败
//     */
//    private TryMoundResult tryMountDataDir() {
//        // 过滤掉 /sdcard/android 类型的数据包目录
//        if (dataOutDir.getPath().toLowerCase().endsWith("android")) {
//            AILog.d(NAME, "tryMount. skip. " + dataOutDir.getPath() + ". " + packageSource.getLogInfo());
//            return new TryMoundResult(0, null);
//        }
//
//        // 如果已经挂载过了就直接结束
//        final String mountPointPath = dataOutDir.getPath();
//        boolean isMounted = myAppInstaller.getMountManager().isMounted(mountPointPath, xpkInfo.getPackageName());
//        if (isMounted) {
//            AILog.d(NAME, "tryMount. mounted. " + packageSource.getLogInfo());
//            return new TryMoundResult(0, null);
//        }
//
//        // 如果没有找到其它可用的存储卡就直接结束
//        String dir;
//        if (dataOutDir.getPath().contains("/data/")) {
//            dir = "Android/data/" + xpkInfo.getPackageName();
//        } else {
//            dir = "Android/obb/" + xpkInfo.getPackageName();
//        }
//        long needSize = xpkInfo.getDataSize() + xpkInfo.getApkSize() + SAVE_SIZE;
//        File file = Storagex.filterByMinBytes(Storagex.getExternalStorageDirectorysWithPath(appContext, dir, true), needSize);
//        String availableStoragePath = file != null ? file.getPath() : null;
//        if (availableStoragePath == null) {
//            AILog.d(NAME, "tryMount. there is no other available sdcard. " + packageSource.getLogInfo());
//            return new TryMoundResult(0, null);
//        }
//
//        // 如果尚未开启挂载功能就弹窗提示
//        if (!myAppInstaller.getMountManager().isEnabled()) {
//            AILog.d(NAME, "tryMount. mount the packet function has not been open. " + packageSource.getLogInfo());
//            return new TryMoundResult(2, null);
//        }
//
//        // 执行挂载
//        final String mountDevicePath = availableStoragePath + File.separator + dir;
//        MountResult mountResult = myAppInstaller.getMountManager().mount(packageSource.getPackageName(), mountDevicePath, mountPointPath);
//
//        return new TryMoundResult(mountResult.confirmResult ? 1 : -1, mountResult.cmdResult);
//    }

    private boolean decompressDataPacket() {
        // 初始化总大小和已完成大小
        packageSource.setCompletedLength(0);   // 在此之前会解压 APK 时会更新这个字段，所以要重置一下
        packageSource.setTotalLength(xpkInfo.getDataSize());

        // 进入解压中状态
        appInstaller.getStatusManager().setStatus(packageSource, InstallStatus.INSTALL_DECOMPRESSING);

        // 显示解压通知
        NotificationFactory notificationFactory = appInstaller.getNotificationFactory();
        ProgressNotification notification = notificationFactory != null ? notificationFactory.createProgressNotification(packageSource) : null;
        if (notification != null) {
            notification.refresh();
        }

        boolean decompressResult = doDecompressDataPacket(notification);

        // 关闭解压通知
        if (notification != null) {
            notification.close();
        }

        // 还原安装中状态
        appInstaller.getStatusManager().setStatus(packageSource, InstallStatus.INSTALLING);

        if (decompressResult) {
            myAppInstaller.getMountManager().putWaitingDeletePackage(packageSource.getPackageName(), packageSource.getVersionCode(), apkOutFile.getPath());
        }

        return decompressResult;
    }

    private boolean doDecompressDataPacket(@Nullable ProgressNotification notification) {
        // 创建数据包目录
        if (!dataOutDir.exists() && !dataOutDir.mkdirs()) {
            AILog.w(NAME, "Unable create data packet dir. " + dataOutDir.getAbsolutePath() + " " + packageSource.getLogInfo());
            EventMonitor eventMonitor = appInstaller.getEventMonitor();
            if (eventMonitor instanceof XpkEventMonitor) {
                ((XpkEventMonitor) eventMonitor).onUnableCreateDataPacketDir(packageSource, dataOutDir);
            }
            return false;
        }

        // 循环解压文件
        try {
            File dataFileParentDir = dataOutDir.getParentFile();
            List list = xpkZipFile.getFileHeaders();
            for (Object file : list) {
                if (!(file instanceof FileHeader)) {
                    continue;
                }

                FileHeader fileHeader = (FileHeader) file;
                String fileName = fileHeader.getFileName();
                if (fileHeader.isDirectory() || !fileName.startsWith(xpkInfo.getDataName())) {
                    continue;
                }

                InputStream inputStream = xpkZipFile.getInputStream(fileHeader);
                try {
                    writeToDestination(inputStream, new File(dataFileParentDir, fileName), notification);
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (UnableCreateFileException e) {
            e.printStackTrace();

            AILog.w(NAME, "Unable create file for decompress data packet. " + e.file.getPath() + " " + packageSource.getLogInfo());
            EventMonitor eventMonitor = appInstaller.getEventMonitor();
            if (eventMonitor instanceof XpkEventMonitor) {
                ((XpkEventMonitor) eventMonitor).onUnableCreateDataPacketFile(packageSource, e.file);
            }
            return false;
        } catch (UnableCreateDirException e) {
            e.printStackTrace();

            AILog.w(NAME, "Unable create dir for decompress data packet. " + e.file.getPath() + " " + packageSource.getLogInfo());
            EventMonitor eventMonitor = appInstaller.getEventMonitor();
            if (eventMonitor instanceof XpkEventMonitor) {
                ((XpkEventMonitor) eventMonitor).onUnableCreateDataPacketDir(packageSource, e.file);
            }
            return false;
        } catch (ZipException e) {
            e.printStackTrace();

            AILog.w(NAME, "Get data package from zip failed for decompress data packet. " + e.toString() + ". " + packageSource.getLogInfo());
            final String message = e.getMessage();
            if (message != null && (message.contains("file does not exist") || message.contains("No such file or directory"))) {
                AILog.w(NAME, "Zip file does not exist for decompress data packet. " + packageSource.getLogInfo());
                appInstaller.getEventMonitor().onFileLost(packageSource);
            } else {
                EventMonitor eventMonitor = appInstaller.getEventMonitor();
                if (eventMonitor instanceof XpkEventMonitor) {
                    ((XpkEventMonitor) eventMonitor).onDecompressDataPacketZipException(packageSource, e);
                }
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();

            String exceptionMessage = e.getMessage();
            if (exceptionMessage != null && exceptionMessage.contains("write failed") && exceptionMessage.contains("No space")) {
                AILog.w(NAME, "Decompress write failed no space. " + packageSource.getLogInfo());
                EventMonitor eventMonitor = appInstaller.getEventMonitor();
                if (eventMonitor instanceof XpkEventMonitor) {
                    ((XpkEventMonitor) eventMonitor).onNoSpaceWritingData(packageSource);
                }
            } else {
                String message = e.getMessage();
                if (message != null && message.contains("Read-only file system")) {
                    AILog.w(NAME, "Read-only file system for decompress data packet. " + packageSource.getLogInfo());
                } else {
                    AILog.w(NAME, "Write data package failed for decompress data packet. " + packageSource.getLogInfo());
                }
                EventMonitor eventMonitor = appInstaller.getEventMonitor();
                if (eventMonitor instanceof XpkEventMonitor) {
                    ((XpkEventMonitor) eventMonitor).onDecompressDataPacketIOException(packageSource, e);
                }
            }
            return false;
        }

        return true;
    }

    private void updateDecompressProgress(@Nullable ProgressNotification notification, final int readLength) {
        if (notification != null) {
            packageSource.setCompletedLength(packageSource.getCompletedLength() + readLength);

            if (notification.refresh()) {
                appInstaller.getListeners().postCallbackProgress(packageSource.getId(), packageSource.getPackageName(),
                        packageSource.getVersionCode(), packageSource.getTotalLength(), packageSource.getCompletedLength());
            }
        }
    }

    /**
     * 输入流写入到文件
     */
    private void writeToDestination(InputStream inputStream, File file, @Nullable ProgressNotification notification) throws IOException {
        if (file.exists() && file.isFile() && !file.delete()) {
            AILog.e(NAME, "writeToDestination. delete file failed: " + file.getPath());
        }
        Filex.createNewFileOrThrow(file);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));

        int readLength;
        byte data[] = new byte[1024 * 1024];
        try {
            while ((readLength = inputStream.read(data)) != -1) {
                bos.write(data, 0, readLength);
                updateDecompressProgress(notification, readLength);
            }
        } finally {
            try {
                bos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}