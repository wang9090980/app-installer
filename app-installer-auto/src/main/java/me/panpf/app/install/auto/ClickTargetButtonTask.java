package me.panpf.app.install.auto;

import android.os.Handler;
import android.view.accessibility.AccessibilityNodeInfo;

import java.lang.ref.WeakReference;

public class ClickTargetButtonTask extends DelayPerformTask implements DelayPerformTask.PerformListener{
    private WeakReference<ClickTargetButtonListener> clickTargetListenerWeakReference;
    private WeakReference<AccessibilityNodeInfo> accessibilityNodeInfoWeakReference;
    private TargetButton targetButton;

    public ClickTargetButtonTask(ClickTargetButtonListener clickTargetButtonListener, Handler handler, TargetButton targetButton, AccessibilityNodeInfo accessibilityNodeInfo) {
        super(handler, null);

        this.clickTargetListenerWeakReference = new WeakReference<>(clickTargetButtonListener);
        this.accessibilityNodeInfoWeakReference = new WeakReference<>(accessibilityNodeInfo);
        this.targetButton = targetButton;

        setPerformListener(this);
    }

    @Override
    public void onPerform() {
        ClickTargetButtonListener clickTargetButtonListener = clickTargetListenerWeakReference.get();
        AccessibilityNodeInfo accessibilityNodeInfo = accessibilityNodeInfoWeakReference.get();
        if(clickTargetButtonListener != null && accessibilityNodeInfo != null){
            clickTargetButtonListener.onClickTargetButton(targetButton, accessibilityNodeInfo);
        }
    }

    public interface ClickTargetButtonListener {
        void onClickTargetButton(TargetButton targetButton, AccessibilityNodeInfo nodeInfo);
    }
}
