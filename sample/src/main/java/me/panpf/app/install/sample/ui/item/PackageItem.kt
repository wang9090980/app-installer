package me.panpf.app.install.sample.ui.item

import android.app.AlertDialog
import android.content.Context
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import me.panpf.adapter.AssemblyItem
import me.panpf.adapter.AssemblyItemFactory
import me.panpf.adapter.ktx.bindView
import me.panpf.androidxkt.util.formatJson
import me.panpf.app.install.sample.AppPackage
import me.panpf.app.install.sample.R
import me.panpf.app.install.sample.ui.widget.InstallButton
import me.panpf.javaxkt.lang.orZero
import me.panpf.javaxkt.util.formatFileSize
import me.panpf.sketch.SketchImageView
import me.panpf.sketch.uri.ApkIconUriModel

class PackageItem(parent: ViewGroup) : AssemblyItem<AppPackage>(R.layout.item_package, parent) {

    private val iconImage by bindView<SketchImageView>(R.id.packageItem_iconImage)
    private val nameText by bindView<TextView>(R.id.packageItem_nameText)
    private val versionNameText by bindView<TextView>(R.id.packageItem_versionNameText)
    private val sizeText by bindView<TextView>(R.id.packageItem_sizeText)
    private val installButton by bindView<InstallButton>(R.id.packageItem_installButton)
    private val xpkFlagText by bindView<TextView>(R.id.packageItem_xpkFlag)

    override fun onConfigViews(context: Context) {
        super.onConfigViews(context)

        itemView.setOnLongClickListener {
            val data = data ?: return@setOnLongClickListener true
            AlertDialog.Builder(context).apply {
                setTitle(data.appName)
                setMessage(data.toJson().toString().formatJson())
                setPositiveButton("取消", null)
            }.show()
            true
        }
    }

    override fun onSetData(position: Int, data: AppPackage?) {
        data ?: return
//        if (data.xpk) {
//            iconImage.displayImage(XpkIconUriModel.makeUri(data.filePath))
//        } else {
            iconImage.displayImage(ApkIconUriModel.makeUri(data.filePath))
//        }
        xpkFlagText.isVisible = data.xpk
        nameText.text = data.appName
        versionNameText.text = "v${data.appVersionName}"
        sizeText.text = data.fileLength.orZero().formatFileSize()
        installButton.installHelper.setPackage(data)
    }

    class Factory : AssemblyItemFactory<AppPackage>() {
        override fun createAssemblyItem(parent: ViewGroup): AssemblyItem<AppPackage> = PackageItem(parent)
        override fun match(data: Any?): Boolean = data is AppPackage
    }
}