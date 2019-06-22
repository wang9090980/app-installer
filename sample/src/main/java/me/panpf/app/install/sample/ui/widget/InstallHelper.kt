package me.panpf.app.install.sample.ui.widget

import android.content.Context
import me.panpf.app.install.*
import me.panpf.app.install.sample.AppPackage
import me.panpf.app.install.sample.My
import me.panpf.javaxkt.util.requireNotNull
import java.lang.ref.WeakReference

class InstallHelper(context: Context, private val callback: Callback) {

    private var appPackage: AppPackage? = null
    private var key: String? = null
    private val appInstaller = My.getPackageMonitor(context)
    private val listener = MyInstallListener(WeakReference(this))

    fun setPackage(appPackage: AppPackage?) {
        onDetachedFromWindow()
        this.appPackage = appPackage
        this.key = appPackage?.filePath
        onAttachedToWindow()
    }

    fun onAttachedToWindow() {
        val appPackage = this.appPackage
        val key = this.key
        if (appPackage != null && key != null) {
            appInstaller.registerStatusListener(listener)
            appInstaller.registerProgressListener(listener)
            val installStatus = appInstaller.queryStatus(key)
            if (installStatus == InstallStatus.DECOMPRESSING) {
                val progress = appInstaller.queryProgress(key)
                refreshStatus(installStatus, progress)
            } else {
                refreshStatus(installStatus, null)
            }
        } else {
            refreshStatus(-1, null)
        }
    }

    fun onDetachedFromWindow() {
        val appPackage = this.appPackage
        val key = this.key
        if (appPackage != null && key != null) {
            appInstaller.unregisterStatusListener(listener)
            appInstaller.unregisterProgressListener(listener)
        }
    }

    fun onClick() {
        val appPackage = this.appPackage
        val key = this.key
        if (appPackage != null && key != null) {
            val installStatus = appInstaller.queryStatus(key)
            if (installStatus == -1) {
                appInstaller.postInstall(LocalPackageSource(appPackage.filePath, appPackage.appName))
            }
        }
    }

    fun refreshStatus(@InstallStatus newStatus: Int, progress: Progress?) {
        when (newStatus) {
            InstallStatus.WAITING_QUEUE -> {
                callback.onQueueing()
            }
            InstallStatus.DECOMPRESSING -> {
                callback.onDecompressing(progress.requireNotNull())
            }
            InstallStatus.INSTALLING -> {
                callback.onInstalling()
            }
            else -> {
                callback.onNone()
            }
        }
    }

    interface Callback {
        fun onNone()
        fun onQueueing()
        fun onDecompressing(progress: Progress)
        fun onInstalling()
    }

    private class MyInstallListener(val weakRef: WeakReference<InstallHelper>) : StatusChangedListener,
        DecompressProgressChangedListener {
        override fun onStatusChanged(key: String, newStatus: Int) {
            val installHelper = weakRef.get() ?: return
            if (key == installHelper.key) {
                if (newStatus == InstallStatus.DECOMPRESSING) {
                    val progress = installHelper.appInstaller.queryProgress(key)
                    installHelper.refreshStatus(newStatus, progress)
                } else {
                    installHelper.refreshStatus(newStatus, null)
                }
            }
        }

        override fun onDecompressProgressChanged(key: String, totalLength: Long, completedLength: Long) {
            val installHelper = weakRef.get() ?: return
            if (key == installHelper.key) {
                installHelper.refreshStatus(InstallStatus.DECOMPRESSING, Progress(totalLength, completedLength))
            }
        }
    }
}