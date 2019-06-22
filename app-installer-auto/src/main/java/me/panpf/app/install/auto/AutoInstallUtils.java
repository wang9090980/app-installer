package me.panpf.app.install.auto;

import android.app.ActivityManager;
import android.content.Context;
import android.view.accessibility.AccessibilityEvent;

import java.lang.reflect.Method;
import java.util.List;

public class AutoInstallUtils {
    public static String getEventName(AccessibilityEvent event) {
        String eventType;
        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                eventType = "VIEW_CLICKED";
                break;
            case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:
                eventType = "VIEW_LONG_CLICKED";
                break;
            case AccessibilityEvent.TYPE_VIEW_SELECTED:
                eventType = "VIEW_SELECTED";
                break;
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                eventType = "VIEW_FOCUSED";
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                eventType = "VIEW_TEXT_CHANGED";
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                eventType = "WINDOW_STATE_CHANGED";
                break;
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                eventType = "NOTIFICATION_STATE_CHANGED";
                break;
            case AccessibilityEvent.TYPE_VIEW_HOVER_ENTER:
                eventType = "VIEW_HOVER_ENTER";
                break;
            case AccessibilityEvent.TYPE_VIEW_HOVER_EXIT:
                eventType = "VIEW_HOVER_EXIT";
                break;
            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START:
                eventType = "TOUCH_EXPLORATION_GESTURE_START";
                break;
            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_END:
                eventType = "TOUCH_EXPLORATION_GESTURE_END";
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                eventType = "WINDOW_CONTENT_CHANGED";
                break;
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                eventType = "VIEW_SCROLLED";
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED:
                eventType = "VIEW_TEXT_SELECTION_CHANGED";
                break;
            case AccessibilityEvent.TYPE_ANNOUNCEMENT:
                eventType = "ANNOUNCEMENT";
                break;
            case AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED:
                eventType = "VIEW_ACCESSIBILITY_FOCUSED";
                break;
            case AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED:
                eventType = "VIEW_ACCESSIBILITY_FOCUS_CLEARED";
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY:
                eventType = "VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY";
                break;
            default:
                eventType = "Unknown";
                break;
        }
        return eventType;
    }

    public static String getSystemProperty(String key) {
        Method getMethod;
        try {
            getMethod = Class.forName("android.os.SystemProperties").getMethod("get", String.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        try {
            return (String) getMethod.invoke(null, key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isServiceRunning(Context context, String packageName, String serviceClassName) {
        if (serviceClassName != null) {
            ActivityManager accessibilityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningServiceInfo> runningServiceInfoList = accessibilityManager.getRunningServices(Integer.MAX_VALUE);
            if (runningServiceInfoList != null) {
                for (ActivityManager.RunningServiceInfo serviceInfo : runningServiceInfoList) {
                    if (packageName.equals(serviceInfo.service.getPackageName()) && serviceClassName.equals(serviceInfo.service.getClassName())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}