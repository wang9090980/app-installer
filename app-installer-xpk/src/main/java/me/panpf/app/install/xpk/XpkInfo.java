package me.panpf.app.install.xpk;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Xml;
import androidx.annotation.NonNull;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * XPK 信息包装类
 */
public class XpkInfo implements Parcelable {

    public static final Parcelable.Creator<XpkInfo> CREATOR = new Parcelable.Creator<XpkInfo>() {
        @Override
        public XpkInfo createFromParcel(Parcel source) {
            return new XpkInfo(source);
        }

        @Override
        public XpkInfo[] newArray(int size) {
            return new XpkInfo[size];
        }
    };
    @NonNull
    private String appName;// 应用名
    @NonNull
    private String packageName; // 包名
    @NonNull
    private String versionName;// 版本信息
    private int versionCode;// 版本号
    private long dataSize;// 数据大小
    private long apkSize;// apk 大小
    @NonNull
    private String destination; // 数据文件解压位置
    @NonNull
    private String dataName; // 数据文件的名称，用于在解压时判断哪些是数据文件

    private XpkInfo(@NonNull String appName, @NonNull String packageName, @NonNull String versionName, int versionCode, long dataSize, long apkSize, @NonNull String destination, @NonNull String dataName) {
        this.appName = appName;
        this.packageName = packageName;
        this.versionName = versionName;
        this.versionCode = versionCode;
        this.dataSize = dataSize;
        this.apkSize = apkSize;
        this.destination = destination;
        this.dataName = dataName;
    }

    @SuppressWarnings("WeakerAccess")
    protected XpkInfo(Parcel in) {
        this.versionCode = in.readInt();
        this.dataSize = in.readLong();
        this.apkSize = in.readLong();
        this.appName = in.readString();
        this.packageName = in.readString();
        this.versionName = in.readString();
        this.destination = in.readString();
        this.dataName = in.readString();
    }

    /**
     * 从 manifest.xml 中获取xpk的信息
     *
     * @param zipFile zip 文件
     * @throws ZipException            不是一个 ZIP 文件
     * @throws XmlPullParserException  解析异常，一般是 manifest 文件异常
     * @throws IOException             读取异常
     * @throws InfoIncompleteException manifest 信息不全
     * @throws MissingFileException    缺失主要文件可能不是 xpk 包
     * @throws InvalidZipException     无效的 zip 文件
     */
    @NonNull
    @SuppressLint("SdCardPath")
    public static XpkInfo parse(ZipFile zipFile) throws InfoIncompleteException, MissingFileException,
            ZipException, XmlPullParserException, IOException, InvalidZipException {

        try {
            zipFile.setFileNameCharset("UTF-8");
        } catch (ZipException e1) {
            e1.printStackTrace();
        }

        // 验证 ZIP 文件是否有效
        if (!zipFile.isValidZipFile()) {
            throw new InvalidZipException();
        }

        FileHeader manifestFileHeader = zipFile.getFileHeader("manifest.xml");
        if (manifestFileHeader == null) {
            throw new MissingFileException("Missing manifest.xml");
        }

        FileHeader apkFileHeader = zipFile.getFileHeader("application.apk");
        if (apkFileHeader == null) {
            throw new MissingFileException("Missing application.apk");
        }

        String defaultStoragePath = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                ? Environment.getExternalStorageDirectory().getPath() : null;
        InputStream is = zipFile.getInputStream(manifestFileHeader);
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(is, "utf-8");

            int versionCode = 0;
            long dataSize = 0;
            long apkSize = 0;
            String appName = null;
            String packageName = null;
            String versionName = null;
            String destination = null;
            String dataName = null;
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    // 数据包大小 long
                    if (parser.getName().equals("data")) {
                        dataName = parser.getAttributeValue(0);
                        dataSize = Long.parseLong(parser.getAttributeValue(1));
                    }
                    // 数据包安装位置
                    else if (parser.getName().equals("destination")) {
                        destination = parser.nextText();
                        if (destination != null && defaultStoragePath != null) {
                            destination = destination.replace("/sdcard", defaultStoragePath);
                        }
                    }
                    // apk大小 long
                    else if (parser.getName().equals("apkinfo")) {
                        apkSize = Long.parseLong(parser.getAttributeValue(0));
                    }
                    // 包名
                    else if (parser.getName().equals("package")) {
                        packageName = parser.nextText();
                    }
                    // 版本信息
                    else if (parser.getName().toLowerCase(Locale.getDefault()).equals("versionname")) {
                        versionName = parser.nextText();
                    }
                    // 版本号
                    else if (parser.getName().toLowerCase(Locale.getDefault()).equals("versioncode")) {
                        versionCode = Integer
                                .parseInt(parser.nextText());
                    }
                    // 游戏名
                    else if (parser.getName().equals("label")) {
                        parser.next();
                        appName = parser.getText();
                    }
                }
                eventType = parser.next();
            }

            // 不能校验 appName、versionName、versionCode，因为 有的 apk 确实会没有 versionName 和 versionCode，有的 xpk 中会没有 appName，但是不影响使用和安装

            if (TextUtils.isEmpty(dataName)) {
                throw new InfoIncompleteException("Missing dataName");
            }

            if (TextUtils.isEmpty(packageName)) {
                throw new InfoIncompleteException("Missing packageName");
            }

            if (TextUtils.isEmpty(destination)) {
                throw new InfoIncompleteException("Missing destination");
            }

            if (apkSize <= 0) {
                throw new InfoIncompleteException("Missing apkSize");
            }

            if (dataSize <= 0) {
                throw new InfoIncompleteException("Missing dataSize");
            }
            return new XpkInfo(appName != null ? appName : "", packageName, versionName != null ? versionName : "", versionCode, dataSize, apkSize, destination, dataName);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    /**
     * 从 manifest.xml 中获取xpk的信息
     *
     * @param file xpk 文件
     * @throws ZipException            不是一个 ZIP 文件
     * @throws XmlPullParserException  解析异常，一般是 manifest 文件异常
     * @throws IOException             读取异常
     * @throws InfoIncompleteException manifest 信息不全
     * @throws MissingFileException    缺失主要文件可能不是 xpk 包
     * @throws InvalidZipException     无效的 zip 文件
     */
    @NonNull
    public static XpkInfo parse(File file) throws ZipException, XmlPullParserException, IOException, InfoIncompleteException, MissingFileException, InvalidZipException {
        return XpkInfo.parse(new ZipFile(file));
    }

    @NonNull
    public static InputStream getApkInputStream(ZipFile zipFile) throws ZipException {
        return zipFile.getInputStream(zipFile.getFileHeader("application.apk"));
    }

    public int getVersionCode() {
        return versionCode;
    }

    public long getDataSize() {
        return dataSize;
    }

    public long getApkSize() {
        return apkSize;
    }

    @NonNull
    public String getAppName() {
        return appName;
    }

    @NonNull
    public String getPackageName() {
        return packageName;
    }

    @NonNull
    public String getVersionName() {
        return versionName;
    }

    /**
     * @return 有三种：
     * /sdcard/Android/obb/com.ovilex.bussimulator17
     * /sdcard/Android/
     * /sdcard/gameloft/games
     */
    @NonNull
    public String getDestination() {
        return destination;
    }

    @NonNull
    public String getDataName() {
        return dataName;
    }

    @Override
    public String toString() {
        return "XpkInfo{" +
                "appName='" + appName + '\'' +
                ", packageName='" + packageName + '\'' +
                ", versionName='" + versionName + '\'' +
                ", versionCode=" + versionCode +
                ", apkSize=" + apkSize +
                ", dataName='" + dataName + '\'' +
                ", destination='" + destination + '\'' +
                ", dataSize=" + dataSize +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.versionCode);
        dest.writeLong(this.dataSize);
        dest.writeLong(this.apkSize);
        dest.writeString(this.appName);
        dest.writeString(this.packageName);
        dest.writeString(this.versionName);
        dest.writeString(this.destination);
        dest.writeString(this.dataName);
    }

    /**
     * manifest 里面缺少信息
     */
    public static class InfoIncompleteException extends Exception {
        public InfoIncompleteException(String message) {
            super(message);
        }
    }

    /**
     * 缺失主要文件可能不是 xpk 包
     */
    public static class MissingFileException extends Exception {
        public MissingFileException(String message) {
            super(message);
        }
    }

    /**
     * 无效的 zip 文件
     */
    public static class InvalidZipException extends Exception {
    }
}