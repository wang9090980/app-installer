//package me.panpf.app.install.sample.xpk;
//
//import android.content.Context;
//import android.text.TextUtils;
//import androidx.annotation.NonNull;
//import me.panpf.sketch.SLog;
//import me.panpf.sketch.uri.AbsStreamDiskCacheUriModel;
//import me.panpf.sketch.uri.GetDataSourceException;
//import me.panpf.sketch.util.SketchUtils;
//import net.lingala.zip4j.core.ZipFile;
//import net.lingala.zip4j.exception.ZipException;
//import net.lingala.zip4j.model.FileHeader;
//
//import java.io.File;
//import java.io.InputStream;
//
//public class XpkIconUriModel extends AbsStreamDiskCacheUriModel {
//    private static final String SCHEME = "xpk.icon://";
//    private static final String NAME = "XpkIconUriModel";
//
//    public static String makeUri(String filePath) {
//        return SCHEME + filePath;
//    }
//
//    @Override
//    protected boolean match(@NonNull String uri) {
//        return !TextUtils.isEmpty(uri) && uri.startsWith(SCHEME);
//    }
//
//    /**
//     * 获取 uri 所真正包含的内容部分，例如 "xpk.icon:///sdcard/test.xpk"，就会返回 "/sdcard/test.xpk"
//     *
//     * @param uri 图片 uri
//     * @return uri 所真正包含的内容部分，例如 "xpk.icon:///sdcard/test.xpk"，就会返回 "/sdcard/test.xpk"
//     */
//    @NonNull
//    @Override
//    public String getUriContent(@NonNull String uri) {
//        return match(uri) ? uri.substring(SCHEME.length()) : uri;
//    }
//
//    @NonNull
//    @Override
//    public String getDiskCacheKey(@NonNull String uri) {
//        return SketchUtils.createFileUriDiskCacheKey(uri, getUriContent(uri));
//    }
//
//    @NonNull
//    @Override
//    protected InputStream getContent(@NonNull Context context, @NonNull String uri) throws GetDataSourceException {
//        ZipFile zipFile;
//        try {
//            zipFile = new ZipFile(new File(getUriContent(uri)));
//        } catch (ZipException e) {
//            String cause = String.format("Unable open xpk file. %s", uri);
//            SLog.e(NAME, e, cause);
//            throw new GetDataSourceException(cause, e);
//        }
//
//        FileHeader iconFileHeader;
//        try {
//            iconFileHeader = zipFile.getFileHeader("icon.png");
//        } catch (ZipException e) {
//            String cause = String.format("Not found icon.png in xpk file. %s", uri);
//            SLog.e(NAME, cause);
//            throw new GetDataSourceException(cause);
//        }
//        if (iconFileHeader == null) {
//            String cause = String.format("Not found icon.png in xpk file. %s", uri);
//            SLog.e(NAME, cause);
//            throw new GetDataSourceException(cause);
//        }
//
//        try {
//            return zipFile.getInputStream(iconFileHeader);
//        } catch (ZipException e) {
//            String cause = String.format("Open \"icon.png\" input stream exception. %s", uri);
//            SLog.e(NAME, e, cause);
//            throw new GetDataSourceException(cause, e);
//        }
//    }
//}
