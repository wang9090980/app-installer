package me.panpf.app.install;

import android.content.Context;
import android.text.format.Formatter;
import androidx.annotation.NonNull;
import me.panpf.javax.security.Digestx;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.zip.ZipFile;

public class ApkValidityChecker implements PackageChecker {

    private static final String NAME = "ApkValidityChecker";

    @Override
    public boolean onCheck(@NonNull Context appContext, @NonNull PackageSource packageSource, @NonNull File apkFile, @NonNull EventMonitor eventMonitor) {
        ZipFile zipFile = null;
        try {
            try {
                zipFile = new ZipFile(apkFile);
            } catch (IOException e) {
                e.printStackTrace();

                String packageMd5 = Digestx.getMD5OrEmpty(apkFile);
                AILog.w(NAME, String.format(Locale.US, "Invalid apk. File：%s/%d/%s. %s. %s",
                        packageMd5, apkFile.length(), Formatter.formatFileSize(appContext, apkFile.length()),
                        e.getMessage(), packageSource.getLogInfo()));
                eventMonitor.onApkZipInvalid(packageSource, packageMd5, e);
                return false;
            }

            if (zipFile.getEntry("AndroidManifest.xml") == null) {
                String packageMd5 = Digestx.getMD5OrEmpty(apkFile);
                AILog.w(NAME, String.format(Locale.US, "Invalid apk. File：%s/%d/%s. %s",
                        packageMd5, apkFile.length(), Formatter.formatFileSize(appContext, apkFile.length()),
                        packageSource.getLogInfo()));
                eventMonitor.onApkZipInvalid(packageSource, packageMd5, null);
                return false;
            }
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }
}
