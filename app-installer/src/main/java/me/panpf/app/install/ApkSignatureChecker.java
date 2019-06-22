package me.panpf.app.install;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import me.panpf.javax.io.Streamx;
import me.panpf.javax.security.Digestx;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipFile;

public class ApkSignatureChecker implements PackageChecker {

    private static final String NAME = "ApkSignatureChecker";

    @Override
    public boolean onCheck(@NonNull Context appContext, @NonNull PackageSource packageSource, @NonNull File apkFile, @NonNull EventMonitor eventMonitor) {
        PackageInfo packageInfo;
        try {
            packageInfo = appContext.getPackageManager().getPackageInfo(packageSource.getPackageName(), PackageManager.GET_UNINSTALLED_PACKAGES);
        } catch (PackageManager.NameNotFoundException e) {
            AILog.d(NAME, "checkApkSignature. uninstalled don't need check signature. " + packageSource.getLogInfo());
            return true;
        }

        String installedAppSignature;
        try {
            installedAppSignature = ApkInspector.getSignatureByFile(packageInfo.applicationInfo.sourceDir);
            if (TextUtils.isEmpty(installedAppSignature)) {
                throw new IllegalArgumentException("installedAppSignature result is null");
            }
        } catch (Exception e) {
            e.printStackTrace();
            AILog.w(NAME, "get signature failed from installed apk. " + packageSource.getLogInfo());
            eventMonitor.onGetInstalledApkSignatureException(packageSource, e);
            return true;
        }

        String newAppSignature;
        try {
            newAppSignature = ApkInspector.getSignatureByFile(apkFile.getPath());
            if (TextUtils.isEmpty(newAppSignature)) {
                throw new IllegalArgumentException("newAppSignature result is null");
            }
        } catch (Exception e) {
            e.printStackTrace();

            AILog.w(NAME, "get signature failed from new apk. " + packageSource.getLogInfo());
            String packageMd5;
            try {
                packageMd5 = Digestx.getMD5(apkFile);
            } catch (IOException ee) {
                ee.printStackTrace();
                packageMd5 = "";
            }
            eventMonitor.onGetNewApkSignatureException(packageSource, packageMd5, e);
            return true;
        }

        if (installedAppSignature.equals(newAppSignature)) {
            AILog.d(NAME, "checkApkSignature. signature match. " + packageSource.getLogInfo());
            return true;
        } else {
            AILog.w(NAME, "signature not matched. " + installedAppSignature + ":" + newAppSignature + " " + packageSource.getLogInfo());
            eventMonitor.onApkSignatureNotMatched(packageSource, installedAppSignature, newAppSignature);

            // 提示签名不匹配后会让用户卸载旧版本，如果旧版本已卸载就继续安装
            try {
                appContext.getPackageManager().getPackageInfo(packageSource.getPackageName(), PackageManager.GET_UNINSTALLED_PACKAGES);
                return false;
            } catch (PackageManager.NameNotFoundException e) {
                AILog.d(NAME, "checkApkSignature. old version uninstalled. " + packageSource.getLogInfo());
                return true;
            }
        }
    }

    private static class ApkInspector {
        public static String getSignatureByPackage(Context context, String packageName) throws Exception {
            PackageInfo packageInfo;
            try {
                packageInfo = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
                return ApkInspector.getSignatureByFile(packageInfo.applicationInfo.sourceDir);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }

        @NonNull
        public static String getSignatureByFile(String filePath) throws Exception {
            ZipFile zipFile = null;
            try {
                zipFile = new ZipFile(filePath);
                List<Certificate> certs = getJarCerts(zipFile.getName());
                if (certs != null && certs.size() > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (Certificate cert : certs) {
                        sb.append(Digestx.getSHA512(Streamx.inputStream(cert.getPublicKey().getEncoded())));
                    }
                    zipFile.close();
                    return Digestx.getSHA512(sb.toString());
                } else {
                    throw new IllegalArgumentException("no certificates");
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
        }

        private static List<Certificate> getJarCerts(String fileName) throws IOException {
            JarFile jf = new JarFile(fileName, true);
            Enumeration<JarEntry> entries = jf.entries();
            List<Certificate> certs = new ArrayList<>();
            Manifest mf = jf.getManifest();
            if (mf != null) {
                Map<String, Attributes> ates = mf.getEntries();
                while (entries.hasMoreElements()) {
                    JarEntry je = entries.nextElement();
                    if (ates.containsKey(je.getName())) {
                        byte[] buffer = new byte[256];
                        InputStream is = jf.getInputStream(je);
                        while ((is.read(buffer, 0, buffer.length)) != -1) {
                            // 什么都不用做
                        }

                        is.close();

                        Collections.addAll(certs, je.getCertificates());
                        break;
                    }
                }
            }
            jf.close();
            return certs;
        }
    }
}
