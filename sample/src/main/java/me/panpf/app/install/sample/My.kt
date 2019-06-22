package me.panpf.app.install.sample

import android.app.Application
import android.content.Context
import android.os.HandlerThread
import me.panpf.app.install.AppInstaller

object My {
    private var appInstaller: AppInstaller? = null

    fun getPackageMonitor(context: Context): AppInstaller {
        val temp = appInstaller
        if (temp != null) return temp

        synchronized(My::class.java) {
            val temp2 = appInstaller
            if (temp2 != null) return temp2

            val handlerThread = HandlerThread("AppInstaller")
            handlerThread.start()
            val newPackageMonitor = AppInstaller(context.applicationContext as Application, null, null, handlerThread)
            this.appInstaller = newPackageMonitor
            return newPackageMonitor
        }
    }
}