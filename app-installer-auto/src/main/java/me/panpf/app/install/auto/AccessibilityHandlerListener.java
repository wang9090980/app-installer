package me.panpf.app.install.auto;

import android.content.Context;
import android.view.accessibility.AccessibilityNodeInfo;

public interface AccessibilityHandlerListener {
    Context getContext();
    BindManager getBindManager();
    AccessibilityNodeInfo getRootNodeInActiveWindow();
    boolean isAcceptPackageName(String packageName);
    void onNewApp(String newAppName);
    void onClickButton(TargetButton button, AccessibilityNodeInfo nodeInfo);
}
