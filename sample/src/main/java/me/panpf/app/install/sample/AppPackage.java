package me.panpf.app.install.sample;

import androidx.annotation.NonNull;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

public class AppPackage implements FileScanner.FileItem {
    public String filePath;
    public String fileName;
    public long fileLength;
    public long fileLastModified;

    public String appName;
    public String appPackageName;
    public String appVersionName;
    public int appVersionCode;

    public boolean installed;   // 是否已安装
    public int installedVersionCode;    // 已安装的版本，当已安装时才会用到此参数

    public boolean xpk; // 是否是XPK安装包
    public boolean broken;  // 是否是破损包

    public boolean tempChecked;

    // 清理的时候会先在子线程中把所有标记删除的文件删掉
    // 然后在列表中数据部分先标记为已删除，等全部删除完毕回到主线程的时候一次性把所有标记删除的数据从列表中删除
    // 这么做是因为在非主线程中删除列表中的数据会导致Adapter刷新不及时而引发IndexOutOfBoundsException异常
    public boolean tempDeleted;

    @Override
    public long getFileLength() {
        return fileLength;
    }

    public boolean isChecked() {
        return tempChecked;
    }

    public void setChecked(boolean checked) {
        tempChecked = checked;
    }

    @Override
    public String getFilePath() {
        return filePath;
    }

    public boolean isDeleted() {
        return tempDeleted;
    }

    public void setDeleted(boolean deleted) {
        this.tempDeleted = deleted;
    }

    public boolean isNewVersion(){
        return installed && appVersionCode > installedVersionCode;
    }

    public boolean isOldVersion(){
        return installed && appVersionCode < installedVersionCode;
    }

    public boolean isSameVersion(){
        return installed && appVersionCode == installedVersionCode;
    }

    @NotNull
    @Override
    public String toString() {
        return "AppPackage{" +
                "filePath='" + filePath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileLength=" + fileLength +
                ", fileLastModified=" + fileLastModified +
                ", appName='" + appName + '\'' +
                ", appPackageName='" + appPackageName + '\'' +
                ", appVersionName='" + appVersionName + '\'' +
                ", appVersionCode=" + appVersionCode +
                ", installed=" + installed +
                ", installedVersionCode=" + installedVersionCode +
                ", xpk=" + xpk +
                ", broken=" + broken +
                ", tempChecked=" + tempChecked +
                ", tempDeleted=" + tempDeleted +
                '}';
    }

    @NonNull
    public JSONObject toJson() {
        JSONObject jo = new JSONObject();
        try {
            jo.put("filePath", filePath);
            jo.put("fileName", fileName);
            jo.put("fileLength", fileLength);
            jo.put("fileLastModified", fileLastModified);
            jo.put("appName", appName);
            jo.put("appPackageName", appPackageName);
            jo.put("appVersionName", appVersionName);
            jo.put("appVersionCode", appVersionCode);
            jo.put("installed", installed);
            jo.put("installedVersionCode", installedVersionCode);
            jo.put("xpk", xpk);
            jo.put("broken", broken);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jo;
    }
}
