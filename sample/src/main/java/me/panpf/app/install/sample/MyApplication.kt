package me.panpf.app.install.sample

import android.app.Application

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

//        Sketch.with(this).configuration.uriModelManager.add(XpkIconUriModel())
    }
}
