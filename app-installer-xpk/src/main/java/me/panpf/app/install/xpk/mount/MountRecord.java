package me.panpf.app.install.xpk.mount;

import me.panpf.app.install.AILog;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import me.panpf.javax.io.Streamx;

import java.io.*;

/**
 * 挂载记录
 */
public class MountRecord {
    private static final String NAME = "MountRecord";

    @SerializedName("appId")
    private String appId;

    @SerializedName("mountSourceDir")
    private String mountSourceDir;

    @SerializedName("mountPointDir")
    private String mountPointDir;

    public static MountRecord decode(File bindRecordFile, Gson gson) {
        // 打开文件
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(bindRecordFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            AILog.e(NAME, "decode. open bind record file failed：" + bindRecordFile.getPath());
            return null;
        }

        // 读取数据
        String content;
        try {
            content = Streamx.readText(Streamx.reader(inputStream));
        } catch (IOException e) {
            e.printStackTrace();
            AILog.e(NAME, "decode. read mount record file failed：" + bindRecordFile.getPath());
            return null;
        } finally {
            Streamx.closeQuietly(inputStream);
        }

        // 转换结果
        if (gson == null) {
            gson = new Gson();
        }
        MountRecord mountRecord = gson.fromJson(content, MountRecord.class);
        if (mountRecord == null) {
            AILog.e(NAME, "decode. parse bind record file failed：" + bindRecordFile.getPath() + "：" + content);
        }
        return mountRecord;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    String getMountSourceDir() {
        return mountSourceDir;
    }

    void setMountSourceDir(String mountSourceDir) {
        this.mountSourceDir = mountSourceDir;
    }

    String getMountPointDir() {
        return mountPointDir;
    }

    void setMountPointDir(String mountPointDir) {
        this.mountPointDir = mountPointDir;
    }

    @Override
    public String toString() {
        return appId + ": " + mountSourceDir + " - " + mountPointDir;
    }
}
