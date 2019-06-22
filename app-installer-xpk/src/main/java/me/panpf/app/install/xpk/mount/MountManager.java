package me.panpf.app.install.xpk.mount;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import me.panpf.app.install.AILog;
import com.yingyonghui.market.app.install.MyAppInstaller;
import me.panpf.androidx.Androidx;
import me.panpf.androidx.os.storage.Storagex;
import me.panpf.javax.io.Filex;
import me.panpf.shell.Cmd;
import me.panpf.shell.CmdResult;
import me.panpf.shell.Sheller;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 数据包目录挂载管理器
 */
public class MountManager {

    /**
     * 这里保存等待删除的从XPK解压出来的APK，待收到安装成功的广播后回来这里检查一下有的话就删除了
     */
    private final Map<String, String> waitingDeletePackageMap = new HashMap<>();

    @NonNull
    private Context appContext;
    @NonNull
    private Handler workHandler;
    @NonNull
    private MountRecordManager recordManager;
    @NonNull
    private MyAppInstaller appInstaller;

    public MountManager(@NonNull Context context, @NonNull MyAppInstaller appInstaller, @NonNull Handler workHandler) {
        this.appContext = context.getApplicationContext();
        this.workHandler = workHandler;
        this.appInstaller = appInstaller;

        this.recordManager = new MountRecordManager(appContext);
    }

    /* ***********************************核心挂载逻辑************************************************ */

    /**
     * 挂载数据包目录
     *
     * @param packageName     包名
     * @param mountDevicePath A 挂载到 B 上，这是 A
     * @param mountPointPath  A 挂载到 B 上，这是 B
     */
    @WorkerThread
    public MountResult mount(String packageName, final String mountDevicePath, final String mountPointPath) {
        CmdResult result = new Sheller(new Cmd("su" + "\n" + "mount -o bind " + mountDevicePath + " " + mountPointPath).timeout(20 * 1000)).syncExecute();
        if (result.isSuccess()) {
            AILog.i("mount. " + result.toString());
        } else {
            AILog.w("mount. " + result.toString());
        }

        boolean isMounted = isMounted(mountPointPath, null);
        if (isMounted) {
            AILog.i("mount. validation mount result: success. " + mountPointPath);
        } else {
            AILog.w("mount. validation mount result: failed. " + mountPointPath);
        }

        recordManager.addRecord(packageName, mountDevicePath, mountPointPath);
        return new MountResult(isMounted, result);
    }

    /**
     * 判断指定挂载点是否已挂载
     *
     * @param mountPointPath       挂载点路径
     * @param mountPointPathSuffix 挂载点路径后缀
     */
    @WorkerThread
    @SuppressLint("SdCardPath")
    @SuppressWarnings("WeakerAccess")
    public boolean isMounted(List<String> mountedPointList, String mountPointPath, final String mountPointPathSuffix) {
        if (mountedPointList == null || mountedPointList.isEmpty()) {
            return false;
        }

        // 替换以 /sdcard/ 开头的挂载点路径，因为 df 挂载记录中显示的是真实的路径
        if (mountPointPath.startsWith("/sdcard")) {
            mountPointPath = mountPointPath.replace("/sdcard", Environment.getExternalStorageDirectory().getPath());
        }

        for (String item : mountedPointList) {
            if (item.equals(mountPointPath) || (mountPointPathSuffix != null && item.endsWith(mountPointPathSuffix))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断指定挂载点是否已挂载
     *
     * @param mountPointPath       挂载点路径
     * @param mountPointPathSuffix 挂载点路径后缀
     */
    @SuppressLint("SdCardPath")
    @WorkerThread
    public boolean isMounted(String mountPointPath, final String mountPointPathSuffix) {
        List<String> mountedPointList = MountUtils.getAllMountPointPath(true);
        return isMounted(mountedPointList, mountPointPath, mountPointPathSuffix);
    }

    /**
     * 取消挂载
     *
     * @param packageName 包名
     */
    @SuppressLint("SdCardPath")
    @WorkerThread
    public boolean umount(final String packageName) {
        // 删除记录文件
        recordManager.removeRecord(packageName);

        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return false;
        }

        File sdcardDir = Environment.getExternalStorageDirectory();
        final File appObbDir = new File(sdcardDir, "Android/obb/" + packageName);

        // 先尝试删除默认存储中的数据包目录，如果删除失败，说明有挂载，那么就先取消挂载然后再删除
        if (!Filex.deleteRecursively(appObbDir)) {
            CmdResult result = new Sheller(new Cmd("su" + "\n" + "umount " + appObbDir).timeout(20 * 1000)).syncExecute();
            if (result.isSuccess()) {
                AILog.i("umount. " + result.toString());
            } else {
                AILog.w("umount. " + result.toString());
            }
        }

        // 搜集所有的数据包目录
        List<File> bindRecordDirs = new LinkedList<>();
        File[] sdcardPaths = Storagex.getExternalStorageDirectorys(appContext);
        if (sdcardPaths.length > 0) {
            for (File sdcardPath : sdcardPaths) {
                bindRecordDirs.add(new File(sdcardPath + File.separator + "Android" + File.separator + "obb" + File.separator + packageName));
                bindRecordDirs.add(new File(sdcardPath + File.separator + "Android" + File.separator + "data" + File.separator + packageName));
            }
        }

        // 删除所有的数据包目录
        boolean allRemoved = true;
        for (File bindRecordDir : bindRecordDirs) {
            if (bindRecordDir.exists()) {
                if (Filex.deleteRecursively(bindRecordDir)) {
                    AILog.d("umount. data dir removed：" + bindRecordDir.getPath());
                } else {
                    AILog.e("umount. data dir remove failed：" + bindRecordDir.getPath());
                    allRemoved = false;
                }
            } else {
                AILog.d("umount. data dir not exists：" + bindRecordDir.getPath());
            }
        }
        return allRemoved;
    }

    /**
     * 一键修复
     */
    @WorkerThread
    public void oneKeyRepair(Handler callbackHandler, final MountRepairListener listener) {
        new Thread(new MountRepairTask(appContext, recordManager, this, callbackHandler, listener)).start();
    }

    /* ***********************************辅助方法************************************************ */

    /**
     * 是否满足使用“数据包挂载到外置SD卡”功能的条件
     */
    public boolean isUsable(boolean requiredRooted) {
        File[] sdcardPaths = Storagex.getExternalStorageDirectorys(appContext);
        boolean sdcardMeet = sdcardPaths.length > 1;
        if (requiredRooted) {
            sdcardMeet = sdcardMeet && Androidx.isRooted();
        }
        return sdcardMeet;
    }

    public boolean isEnabled() {
        return appInstaller.getPreferences().isEnabledMount();
    }

    public void setEnable(boolean enable) {
        appInstaller.getPreferences().setEnabledMount(enable);
    }

    public void postInit() {
        workHandler.post(new MountInitTask(appContext, this));
    }

    @NonNull
    public MountRecordManager getRecordManager() {
        return recordManager;
    }

    /**
     * 放进来一个等待在安装完成后删除的安装包的路径
     *
     * @param packageName 包名
     * @param versionCode 版本号
     * @param packagePath 安装包路径
     */
    public void putWaitingDeletePackage(String packageName, int versionCode, String packagePath) {
        if (packageName == null || versionCode <= 0 || packagePath == null) {
            new IllegalArgumentException("param error: packageName=" + packageName + "; versionCode=" + versionCode + "; packagePath=" + packagePath).printStackTrace();
            return;
        }

        synchronized (waitingDeletePackageMap) {
            String key = packageName + ":" + versionCode;
            waitingDeletePackageMap.put(key, packagePath);
        }
    }

    /**
     * 根据包名和版本号获取等待删除的安装包的路径
     *
     * @param packageName 包名
     * @param versionCode 版本号
     * @return 等待删除的安装包的路径
     */
    @SuppressWarnings("WeakerAccess")
    public String getWaitingDeletePackage(String packageName, int versionCode) {
        synchronized (waitingDeletePackageMap) {
            String key = packageName + ":" + versionCode;
            return waitingDeletePackageMap.get(key);
        }
    }
}