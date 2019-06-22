package me.panpf.app.install.sample.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import me.panpf.app.install.Progress
import me.panpf.javax.util.Formatx

class InstallButton : Button {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    val installHelper = InstallHelper(context, InstallCallback(this))
    private var clickListener: OnClickListener? = null

    init {
        super.setOnClickListener(ClickProxy(this))
    }

    override fun setOnClickListener(l: OnClickListener?) {
        this.clickListener = l
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        installHelper.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        installHelper.onDetachedFromWindow()
        super.onDetachedFromWindow()
    }

    private class ClickProxy(val button: InstallButton) : OnClickListener {
        override fun onClick(v: View?) {
            button.installHelper.onClick()
            button.clickListener?.onClick(v)
        }
    }

    private class InstallCallback(val button: InstallButton) : InstallHelper.Callback {
        override fun onNone() {
            button.text = "安装"
            button.isEnabled = true
        }

        override fun onQueueing() {
            button.text = "排队中"
            button.isEnabled = false
        }

        override fun onDecompressing(progress: Progress) {
            button.text = Formatx.percent(progress.completedLength, progress.totalLength)
            button.isEnabled = false
        }

        override fun onInstalling() {
            button.text = "安装中"
            button.isEnabled = false
        }
    }
}