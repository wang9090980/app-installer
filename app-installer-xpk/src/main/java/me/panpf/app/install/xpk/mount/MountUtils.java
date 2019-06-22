package me.panpf.app.install.xpk.mount;

import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import me.panpf.app.install.AILog;
import me.panpf.shell.Cmd;
import me.panpf.shell.CmdResult;
import me.panpf.shell.Sheller;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class MountUtils {
    private static final String DELETED_SUFFIX = "\040(deleted)";

    /**
     * 获取所有挂载点
     *
     * @param onlyIncludeStartWithDataAndObbDir 只返回以默认数据目录和 obb 目录开头的挂载点
     */
    @Nullable
    @WorkerThread
    public static List<String> getAllMountPointPath(@SuppressWarnings("SameParameterValue") boolean onlyIncludeStartWithDataAndObbDir) {
        // 执行 df 命令获取挂载表
        CmdResult result = new Sheller(new Cmd("df").timeout(20 * 1000)).syncExecute();
        if (result.isSuccess()) {
            AILog.i("getAllMountPointPath. " + result.toString());
        } else {
            AILog.w("getAllMountPointPath. " + result.toString());
        }

        if (!result.isSuccess() || TextUtils.isEmpty(result.getText())) {
            return null;
        }

        File sdcardDir = Environment.getExternalStorageDirectory();
        String obbDirPath = sdcardDir.getPath() + File.separator + "Android" + File.separator + "obb";
        String dataDirPath = sdcardDir.getPath() + File.separator + "Android" + File.separator + "data";

        /* 挂载表数据结构示例 (7.0以下 版本)：
            Filesystem             Size   Used   Free   Blksize
            /dev                   345M   784K   344M   4096
            /mnt/asec              345M     0K   345M   4096
            /mnt/obb               345M     0K   345M   4096
            /system                387M   349M    38M   2048
            /data                  968M   560M   407M   2048
            /cache                  96M     2M    94M   2048
            /preload               192M   121M    71M   2048
            /mnt/sdcard              5G     1G     4G   4096
            /mnt/secure/asec         5G     1G     4G   4096
            /mnt/sdcard/extra_sd    14G    14M    14G   32768
            /mnt/extrasd_bind       14G    14M    14G   32768
            /mnt/sdcard/mount-test1     5G     1G     4G   4096
        */

        /* 挂载表数据结构示例 (7.0及以上 版本)：
            Filesystem            1K-blocks    Used Available Use% Mounted on
            tmpfs                   1462780     612   1462168   1% /dev
            tmpfs                   1462780       0   1462780   0% /mnt
            /dev/block/mmcblk0p23   1290112  806816    483296  63% /system
            /dev/block/mmcblk0p24    387032    7884    379148   3% /cache
            /dev/block/mmcblk0p25  13081708 7577060   5373576  59% /data
            /dev/block/mmcblk0p21     16112    4236     11876  27% /persist
            /dev/block/mmcblk0p22     65488   54352     11136  83% /firmware
            /data/media            13081708 7577060   5373576  59% /storage/emulated
         */
        List<String> mountPointPathList = new LinkedList<>();
        for (String mountRecord : result.getText().split("\n")) {
            mountRecord = mountRecord.trim();

            String lowerCaseRecord = mountRecord.toLowerCase();
            if (lowerCaseRecord.contains("permission denied") || lowerCaseRecord.contains("filesystem")) {
                continue;
            }

            // 7.0 及以上版本根据 % 截取，一下版本根据第一个空格截取
            String mountPointPath;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                int index = mountRecord.lastIndexOf("%");
                if (index <= -1) {
                    continue;
                }

                mountPointPath = mountRecord.substring(index + 2).trim();
            } else {
                int index = mountRecord.indexOf(" ");
                if (index <= -1) {
                    continue;
                }
                mountPointPath = mountRecord.substring(0, index + 1).trim();
            }

            // 当挂载源已删除的时候，挂载点路径后面会加上 " (deleted)" 必须要去掉
            if (mountPointPath.contains(DELETED_SUFFIX)) {
                int index = mountPointPath.indexOf(DELETED_SUFFIX);
                if (index <= -1) {
                    continue;
                }
                mountPointPath = mountPointPath.substring(0, index + 1).trim();
            }

            if (onlyIncludeStartWithDataAndObbDir) {
                if (mountPointPath.startsWith(obbDirPath) || mountPointPath.startsWith(dataDirPath)) {
                    if (!mountPointPath.equalsIgnoreCase(obbDirPath) && !mountPointPath.equalsIgnoreCase(dataDirPath)) {
                        mountPointPathList.add(mountPointPath);
                    }
                }
            } else {
                mountPointPathList.add(mountPointPath);
            }
        }
        return !mountPointPathList.isEmpty() ? mountPointPathList : null;
    }
}
