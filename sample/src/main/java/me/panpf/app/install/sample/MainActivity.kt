package me.panpf.app.install.sample

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_main.*
import me.panpf.adapter.AssemblyRecyclerAdapter
import me.panpf.androidx.app.Permissionx
import me.panpf.androidx.os.storage.Storagex
import me.panpf.app.install.sample.ui.item.PackageItem
import me.panpf.javax.lang.Stringx
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var fileScanner: FileScanner

    private val adapter = AssemblyRecyclerAdapter(ArrayList<Any>()).apply {
        addItemFactory(PackageItem.Factory())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainA_recycler.adapter = adapter

        fileScanner = FileScanner(MyFileChecker(baseContext), MyScanListener()).apply { setDirFilter(MyDirFilter()) }

        if (Permissionx.isGrantPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            start()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1101)
        } else {
            start()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1101) {
            if (Permissionx.isGrantPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                start()
            } else {
                finish()
            }
        }
    }

    fun start() {
        val storages = Storagex.getExternalStorageDirectorys(baseContext).map { it.path }.toTypedArray()
        if (storages.isEmpty()) {
            MyScanListener().onCompleted()
            mainA_loadingText.text = "Empty"
        } else {
            fileScanner.execute(storages)
        }
    }

    override fun onDestroy() {
        fileScanner.cancel()
        super.onDestroy()
    }

    private inner class MyDirFilter : FileScanner.DirFilter {

        override fun accept(dir: File): Boolean {
            val fileNameLowerCase = dir.name.toLowerCase()

            var keyword = "."
            if (fileNameLowerCase.startsWith(keyword)) {
                return false
            }

            keyword = "tuniuapp"
            if (keyword.equals(fileNameLowerCase, ignoreCase = true)) {
                return false
            }

            keyword = "cache"
            if (keyword.equals(fileNameLowerCase, ignoreCase = true) || fileNameLowerCase.endsWith(keyword)) {
                return false
            }

            keyword = "log"
            if (keyword.equals(fileNameLowerCase, ignoreCase = true) || fileNameLowerCase.endsWith(keyword)) {
                return false
            }

            keyword = "dump"
            return !(keyword.equals(fileNameLowerCase, ignoreCase = true) || fileNameLowerCase.endsWith(keyword))

        }
    }

    class MyFileChecker internal constructor(context: Context) : FileScanner.FileChecker {
        private val packageManager = context.packageManager

        override fun accept(file: File): FileScanner.FileItem? {
            val fileNameLowerCase = file.name.toLowerCase()

            // 是文件的话根据后缀名判断是APK还是XPK
            if (file.isFile) {
                return when {
                    fileNameLowerCase.endsWith(".apk") -> parseFromApk(file)
//                    fileNameLowerCase.endsWith(".xpk") -> parseFromXpk(file)
                    else -> null
                }
            }

            return null
        }

        override fun onFinished() {

        }

        private fun parseFromApk(file: File): AppPackage {
            val appPackage = AppPackage()
            appPackage.filePath = file.path
            appPackage.fileName = file.name
            appPackage.fileLength = file.length()
            appPackage.fileLastModified = file.lastModified()

            val packageInfo = packageManager.getPackageArchiveInfo(file.path, 0)
            if (packageInfo?.applicationInfo != null) {
                packageInfo.applicationInfo.sourceDir = file.path
                packageInfo.applicationInfo.publicSourceDir = file.path

                appPackage.appName = Stringx.orEmpty(packageInfo.applicationInfo.loadLabel(packageManager)).toString()
                appPackage.appPackageName = packageInfo.packageName
                appPackage.appVersionName = packageInfo.versionName
                appPackage.appVersionCode = packageInfo.versionCode

                try {
                    val installedPackage = packageManager.getPackageInfo(appPackage.appPackageName, 0)
                    appPackage.installed = true
                    appPackage.installedVersionCode = installedPackage.versionCode
                } catch (e: PackageManager.NameNotFoundException) {
                    //                    e.printStackTrace();
                }

            } else {
                appPackage.broken = true
            }

            return appPackage
        }

//        private fun parseFromXpk(file: File): AppPackage {
//            val appPackage = AppPackage()
//            appPackage.filePath = file.path
//            appPackage.fileName = file.name
//            appPackage.fileLength = file.length()
//            appPackage.xpk = true
//
//            val xpkInfo: XpkInfo
//            try {
//                xpkInfo = XpkInfo.parse(ZipFile(file))
//
//                appPackage.appName = xpkInfo.getAppName()
//                appPackage.appPackageName = xpkInfo.getPackageName()
//                appPackage.appVersionName = xpkInfo.getVersionName()
//                appPackage.appVersionCode = xpkInfo.getVersionCode()
//
//                try {
//                    val installedPackage = packageManager.getPackageInfo(appPackage.appPackageName, 0)
//                    appPackage.installed = true
//                    appPackage.installedVersionCode = installedPackage.versionCode
//                } catch (e: PackageManager.NameNotFoundException) {
//                    //                    e.printStackTrace();
//                }
//
//            } catch (e: Exception) {
//                e.printStackTrace()
//                appPackage.broken = true
//            }
//
//            return appPackage
//        }
    }

    private inner class MyScanListener : FileScanner.ScanListener {
        private var startTime: Long = 0
        private var currentDir: File? = null
        private var totalLength: Int = 0
        private var completedLength: Int = 0

        override fun onStarted() {
            startTime = System.currentTimeMillis()
            mainA_loadingText.isVisible = true
        }

        override fun onScanDir(dir: File) {
            currentDir = dir
            updateProgress()
        }

        override fun onFindFile(fileItem: FileScanner.FileItem) {
            if (fileItem is AppPackage) {
                adapter.dataList?.add(fileItem)
                adapter.notifyDataSetChanged()
            }
        }

        override fun onUpdateProgress(totalLength: Int, completedLength: Int) {
            this.totalLength = totalLength
            this.completedLength = completedLength
            updateProgress()
        }

        fun updateProgress() {
            val percent = completedLength.toFloat() / totalLength
            mainA_loadingText.text = "扫描中: ${(percent * 100).toInt()}"
        }

        override fun onCompleted() {
            adapter.dataList = adapter.dataList?.sortedBy {
                (it as AppPackage).appName
            }
            mainA_loadingText.isVisible = false
        }

        override fun onCanceled() {
        }
    }
}