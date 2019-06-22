package me.panpf.app.install.auto;

import android.view.accessibility.AccessibilityEvent;

import java.util.List;

public interface AccessibilityHandler {
    void onWindowStateChanged(AccessibilityEvent event);
    void onWindowContentChanged(AccessibilityEvent event);
    void onViewScrolled(AccessibilityEvent event);

    void onInterrupt();
    void onServiceConnected();
    void onUnbind();
    void onDestroy();

    List<TargetPage> getTargetPageList();
}
