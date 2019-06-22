package me.panpf.app.install.xpk.mount;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import me.panpf.app.install.AILog;
import com.google.gson.Gson;
import me.panpf.javax.io.Streamx;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 挂载记录管理器
 */
public class MountRecordManager {
    private static final String SUFFIX = ".bind_record";

    @NonNull
    private final List<File> dirList = new LinkedList<>();

    @NonNull
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private Context appContext;

    MountRecordManager(@NonNull Context context) {
        this.appContext = context.getApplicationContext();
        this.dirList.add(new File(context.getFilesDir(), "bind_record"));
    }

    public void addDir(@SuppressWarnings("SameParameterValue") int index, File dir) {
        if (dir != null) {
            synchronized (dirList) {
                dirList.add(index, dir);
            }
        }
    }

    /**
     * 添加一条记录
     *
     * @param appPackageName 包名
     * @param mountSourceDir 被挂载的目录
     * @param mountPointDir  挂载点目录
     */
    @WorkerThread
    @SuppressWarnings("UnusedReturnValue")
    boolean addRecord(String appPackageName, String mountSourceDir, String mountPointDir) {
        File dir = null;
        synchronized (dirList) {
            for (File childDir : dirList) {
                if (childDir.exists() || childDir.mkdirs()) {
                    dir = childDir;
                    break;
                }
            }
        }
        if (dir == null) {
            String dirString;
            synchronized (dirList) {
                dirString = dirList.toString();
            }
            AILog.e("addMountRecord. No available folder. " + dirString);
            return false;
        }

        // 组织记录数据
        MountRecord mountRecord = new MountRecord();
        mountRecord.setAppId(appPackageName);
        mountRecord.setMountSourceDir(mountSourceDir);
        mountRecord.setMountPointDir(mountPointDir);
        String jsonContent = new Gson().toJson(mountRecord);

        // 创建记录文件
        File file = new File(dir, appPackageName + SUFFIX);
        try {
            if (!file.exists() && !file.createNewFile()) {
                AILog.e("addMountRecord. create bind record file failed：" + file.getPath());
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            AILog.e("addMountRecord. create bind record file failed：" + file.getPath());
            return false;
        }

        // 写入数据
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(file, false);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            AILog.e("addMountRecord. open bind record file failed：" + file.getPath());
            return false;
        }
        try {
            outputStream.write(jsonContent.getBytes());
        } catch (IOException e1) {
            e1.printStackTrace();
            AILog.e("addMountRecord. write data failed：" + file.getPath());
        } finally {
            Streamx.closeQuietly(outputStream);
        }
        return true;
    }

    /**
     * 获取所有的挂载记录
     */
    @WorkerThread
    List<MountRecord> getRecords() {
        // 搜集所有的目录
        List<File> bindRecordDirs;
        synchronized (dirList) {
            bindRecordDirs = new LinkedList<>(dirList);
        }

        // 搜索所有的记录文件并读取转换
        List<MountRecord> mountRecordList = new ArrayList<>();
        FilenameFilter filenameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.endsWith(SUFFIX);
            }
        };
        Gson gson = new Gson();
        for (File bindRecordDir : bindRecordDirs) {
            if (bindRecordDir == null) {
                continue;
            }
            File[] bindRecordFiles = bindRecordDir.listFiles(filenameFilter);
            if (bindRecordFiles == null || bindRecordFiles.length == 0) {
                continue;
            }

            for (File bindRecordFile : bindRecordFiles) {
                MountRecord mountRecord = MountRecord.decode(bindRecordFile, gson);
                if (mountRecord != null) {
                    mountRecordList.add(mountRecord);
                }
            }
        }
        return mountRecordList;
    }

    /**
     * 删除记录文件
     *
     * @param packageName 包名
     */
    @WorkerThread
    void removeRecord(final String packageName) {
        // 搜集所有的目录
        List<File> bindRecordDirs;
        synchronized (dirList) {
            bindRecordDirs = new LinkedList<>(dirList);
        }

        // 搜索所有的记录文件并删除
        FilenameFilter bindRecordFileFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.endsWith(packageName + SUFFIX);
            }
        };
        for (File bindRecordDir : bindRecordDirs) {
            File[] bindRecordFiles = bindRecordDir.listFiles(bindRecordFileFilter);
            if (bindRecordFiles == null || bindRecordFiles.length == 0) {
                continue;
            }

            for (File bindRecordFile : bindRecordFiles) {
                if (!bindRecordFile.delete()) {
                    AILog.e("removeMountRecord. remove bind record file failed：" + bindRecordFile.getPath());
                }
            }
        }
    }
}
