package me.panpf.app.install.xpk.mount;

import android.content.Context;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.panpf.app.install.AILog;
import me.panpf.app.packages.PackageUtils;
import me.panpf.shell.Cmd;
import me.panpf.shell.CmdResult;
import me.panpf.shell.Sheller;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class MountRepairTask implements Runnable {
    @NonNull
    private Context appContext;
    @NonNull
    private MountRecordManager recordManager;
    @NonNull
    private MountManager mountManager;
    @Nullable
    private Handler callbackHandler;
    @Nullable
    private MountRepairListener listener;

    MountRepairTask(@NonNull Context context, @NonNull MountRecordManager recordManager,
                    @NonNull MountManager mountManager, @Nullable Handler callbackHandler, @Nullable MountRepairListener listener) {
        this.appContext = context.getApplicationContext();
        this.recordManager = recordManager;
        this.mountManager = mountManager;
        this.callbackHandler = callbackHandler;
        this.listener = listener;
    }

    @Override
    public void run() {
        @Nullable
        List<String> mountedDirs = MountUtils.getAllMountPointPath(true);
        @Nullable
        List<MountRecord> mountRecordList = recordManager.getRecords();
        @Nullable
        Set<String> appIdList = PackageUtils.getAllAppId(appContext, false, false);

        LinkedList<Object> repairList = new LinkedList<>();

        // 根据挂载记录寻找需要挂载的
        if (mountRecordList != null && !mountRecordList.isEmpty()) {
            for (MountRecord mountRecord : mountRecordList) {
                // 如果当前 App 已经被卸载了就跳过
                if (appIdList != null && !appIdList.contains(mountRecord.getAppId())) {
                    AILog.w("oneKeyRepair. uninstalled：" + mountRecord.toString());
                    recordManager.removeRecord(mountRecord.getAppId());
                    continue;
                }

                // 已挂载就跳过
                if (mountManager.isMounted(mountedDirs, mountRecord.getMountPointDir(), mountRecord.getAppId())) {
                    AILog.d("oneKeyRepair. mounted：" + mountRecord.toString());
                    continue;
                }

                // 加入待媳妇列表
                AILog.w("oneKeyRepair. need mount：" + mountRecord.toString());
                repairList.add(mountRecord);
            }
        }

        // 寻找需要卸载的
        if (mountedDirs != null && !mountedDirs.isEmpty()) {
            for (String mountedDirPath : mountedDirs) {
                // 从挂载点上截取包名
                int index = mountedDirPath.lastIndexOf(File.separator);
                if (index <= -1) {
                    AILog.w("oneKeyRepair. interception packageName failed：" + mountedDirPath);
                    continue;
                }
                String appId = mountedDirPath.substring(index + 1, mountedDirPath.length()).trim();

                if (appIdList != null && !appIdList.contains(appId)) {
                    AILog.w("oneKeyRepair. uninstalled need umount：" + mountedDirPath);
                    repairList.add(mountedDirPath);
                }
            }
        }

        CmdResult result = null;
        if (!repairList.isEmpty()) {
            // 拼成一个命令串
            StringBuilder commandBuilder = new StringBuilder();
            commandBuilder.append("su");
            for (Object item : repairList) {
                if (item instanceof MountRecord) {
                    MountRecord mountRecord = (MountRecord) item;
                    commandBuilder.append("\n").append("mount -o bind ").append(mountRecord.getMountSourceDir()).append(" ").append(mountRecord.getMountPointDir());
                } else if (item instanceof String) {
                    commandBuilder.append("\n").append("umount ").append((String) item);
                }
            }

            // 执行
            if (commandBuilder.length() > 2) {
                result = new Sheller(new Cmd(commandBuilder.toString()).timeout(20 * 1000)).syncExecute();
                if (result.isSuccess()) {
                    AILog.i("oneKeyRepair. " + result.toString());
                } else {
                    AILog.w("oneKeyRepair. " + result.toString());
                }
            }
        }

        if (listener != null) {
            if (callbackHandler != null) {
                final CmdResult callbackResult1 = result;
                callbackHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onFinished(callbackResult1);
                    }
                });
            } else {
                listener.onFinished(result);
            }
        }
    }
}
