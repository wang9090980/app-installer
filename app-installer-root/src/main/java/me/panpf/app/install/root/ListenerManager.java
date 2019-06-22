package me.panpf.app.install;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;

import java.lang.ref.WeakReference;
import java.util.LinkedList;

@SuppressWarnings("WeakerAccess")
public class ListenerManager {
    private static final String KEY_WATCH_ALL_APP = "KEY_WATCH_ALL_APP";

    @NonNull
    private final ArrayMap<String, LinkedList<InstallStatusListener>> statusListenerMap = new ArrayMap<>();
    @NonNull
    private final ArrayMap<String, LinkedList<InstallProgressListener>> progressListenerMap = new ArrayMap<>();

    @NonNull
    private AppInstaller appInstaller;
    @NonNull
    private ListenerDispatchHandler listenerDispatchHandler;

    ListenerManager(@NonNull AppInstaller appInstaller, @NonNull HandlerThread handlerThread) {
        this.appInstaller = appInstaller;
        this.listenerDispatchHandler = new ListenerDispatchHandler(this, handlerThread);
    }

    @NonNull
    @AnyThread
    private String genKey(@NonNull final String packageName, final int versionCode) {
        return packageName + ":" + versionCode;
    }

    /**
     * 注册状态监听器
     *
     * @param key      key
     * @param listener 状态监听器
     */
    @AnyThread
    public void registerStatusListener(@NonNull String key, @NonNull final InstallStatusListener listener) {
        //noinspection ConstantConditions
        if (listener != null) {
            synchronized (statusListenerMap) {
                LinkedList<InstallStatusListener> listeners = statusListenerMap.get(key);
                if (listeners == null) {
                    listeners = new LinkedList<>();
                    statusListenerMap.put(key, listeners);
                }
                listeners.add(listener);
            }
        }
    }

    /**
     * 注册某个 app 的状态监听器
     *
     * @param packageName app 包名
     * @param versionCode app 版本号
     * @param listener    状态监听器
     */
    @AnyThread
    public void registerStatusListener(@NonNull final String packageName, final int versionCode, @NonNull final InstallStatusListener listener) {
        registerStatusListener(genKey(packageName, versionCode), listener);
    }

    /**
     * 注册状态监听器，不关心某一个 app 而是关心所有的应用
     *
     * @param listener 状态监听器
     */
    @AnyThread
    public void registerStatusListener(@NonNull final InstallStatusListener listener) {
        registerStatusListener(KEY_WATCH_ALL_APP, listener);
    }

    /**
     * 删除状态监听器
     *
     * @param key      key
     * @param listener 状态监听器
     * @return true：删除成功；false：未注册或 listener 为 null
     */
    @AnyThread
    public boolean unregisterStatusListener(@NonNull String key, @NonNull final InstallStatusListener listener) {
        synchronized (statusListenerMap) {
            //noinspection ConstantConditions
            if (listener != null && !statusListenerMap.isEmpty()) {
                LinkedList<InstallStatusListener> listeners = statusListenerMap.get(key);
                if (listeners != null && !listeners.isEmpty()) {
                    return listeners.remove(listener);
                }
            }
        }
        return false;
    }

    /**
     * 删除某个 app 的状态监听器
     *
     * @param packageName app 包名
     * @param versionCode app 版本号
     * @param listener    状态监听器
     */
    @AnyThread
    public void unregisterStatusListener(@NonNull final String packageName, final int versionCode, @NonNull final InstallStatusListener listener) {
        unregisterStatusListener(genKey(packageName, versionCode), listener);
    }

    /**
     * 删除状态监听器，不关心某一个 app 而是关心所有的应用
     *
     * @param listener 状态监听器
     * @return true：删除成功；false：未注册或 listener 为 null
     */
    @AnyThread
    @SuppressWarnings("unused")
    public boolean unregisterStatusListener(@NonNull final InstallStatusListener listener) {
        return unregisterStatusListener(KEY_WATCH_ALL_APP, listener);
    }

    /**
     * 注册进度监听器
     *
     * @param key      key
     * @param listener 进度监听器
     */
    @AnyThread
    public void registerProgressListener(@NonNull String key, @NonNull final InstallProgressListener listener) {
        //noinspection ConstantConditions
        if (listener != null) {
            synchronized (progressListenerMap) {
                LinkedList<InstallProgressListener> listeners = progressListenerMap.get(key);
                if (listeners == null) {
                    listeners = new LinkedList<>();
                    progressListenerMap.put(key, listeners);
                }
                listeners.add(listener);
            }
        }
    }

    /**
     * 注册某个 app 的进度监听器
     *
     * @param packageName app 包名
     * @param versionCode app 版本号
     * @param listener    进度监听器
     */
    @AnyThread
    @SuppressWarnings("unused")
    public void registerProgressListener(@NonNull final String packageName, final int versionCode, @NonNull final InstallProgressListener listener) {
        registerProgressListener(genKey(packageName, versionCode), listener);
    }

    /**
     * 注册进度监听器，不关心某一个 app 而是关心所有的应用
     *
     * @param listener 进度监听器
     */
    @AnyThread
    @SuppressWarnings("unused")
    public void registerProgressListener(@NonNull final InstallProgressListener listener) {
        registerProgressListener(KEY_WATCH_ALL_APP, listener);
    }

    /**
     * 删除进度监听器
     *
     * @param key      key
     * @param listener 进度监听器
     */
    @AnyThread
    public void unregisterProgressListener(@NonNull String key, @NonNull final InstallProgressListener listener) {
        synchronized (progressListenerMap) {
            //noinspection ConstantConditions
            if (listener != null && !progressListenerMap.isEmpty()) {
                LinkedList<InstallProgressListener> listeners = progressListenerMap.get(key);
                if (listeners != null && !listeners.isEmpty()) {
                    listeners.remove(listener);
                }
            }
        }
    }

    /**
     * 删除某个 app 的进度监听器
     *
     * @param packageName app 包名
     * @param versionCode app 版本号
     * @param listener    进度监听器
     */
    @AnyThread
    @SuppressWarnings("unused")
    public void unregisterProgressListener(@NonNull final String packageName, final int versionCode, @NonNull final InstallProgressListener listener) {
        unregisterProgressListener(genKey(packageName, versionCode), listener);
    }

    /**
     * 删除进度监听器，不关心某一个 app 而是关心所有的应用
     *
     * @param listener 进度监听器
     */
    @AnyThread
    @SuppressWarnings("unused")
    public void unregisterProgressListener(@NonNull final InstallProgressListener listener) {
        unregisterProgressListener(KEY_WATCH_ALL_APP, listener);
    }

    @AnyThread
    public void postCallbackStatus(@NonNull String key, @Nullable String appPackageName, int appVersionCode, @InstallStatus int installStatus) {
        listenerDispatchHandler.postDispatchStatus(key, appPackageName, appVersionCode, installStatus);
    }

    @AnyThread
    public void postCallbackProgress(@NonNull String key, @Nullable String appPackageName, int appVersionCode, long completedLength, long totalLength) {
        listenerDispatchHandler.postDispatchProgress(key, appPackageName, appVersionCode, completedLength, totalLength);
    }

    /**
     * 监听分发 Handler，在工作线程中将监听器一个一个分发到 UI 线程，单独执行回调，这样能分散回调的执行避免在 UI 线程同时回调大量监听导致 UI 线程的卡顿
     */
    private static class ListenerDispatchHandler extends Handler {
        private static final int WHAT_DISPATCH_STATUS = 9902;
        private static final int WHAT_DISPATCH_PROGRESS = 9903;

        @NonNull
        private WeakReference<ListenerManager> managerWeakReference;
        @NonNull
        private ListenerCallbackHandler callbackHandler;

        ListenerDispatchHandler(@NonNull ListenerManager manager, @NonNull HandlerThread handlerThread) {
            super(handlerThread.getLooper());
            this.managerWeakReference = new WeakReference<>(manager);
            this.callbackHandler = new ListenerCallbackHandler();
        }

        @AnyThread
        void postDispatchStatus(@NonNull String key, @Nullable String appPackageName, int appVersionCode, @InstallStatus int installStatus) {
            Message message = obtainMessage(WHAT_DISPATCH_STATUS, installStatus, 0, key);

            Bundle bundle = new Bundle();
            bundle.putString("key", key);
            bundle.putString("appPackageName", appPackageName);
            bundle.putInt("appVersionCode", appVersionCode);
            message.setData(bundle);

            message.sendToTarget();
        }

        @AnyThread
        void postDispatchProgress(@NonNull String key, @Nullable String appPackageName, int appVersionCode, long completedLength, long totalLength) {
            Message message = obtainMessage(WHAT_DISPATCH_PROGRESS, key);

            Bundle bundle = new Bundle();
            bundle.putString("key", key);
            bundle.putString("appPackageName", appPackageName);
            bundle.putInt("appVersionCode", appVersionCode);
            bundle.putLong("completedLength", completedLength);
            bundle.putLong("totalLength", totalLength);
            message.setData(bundle);

            message.sendToTarget();
        }

        @Override
        public void handleMessage(Message msg) {
            ListenerManager manager = managerWeakReference.get();
            if (manager == null) {
                return;
            }

            switch (msg.what) {
                case WHAT_DISPATCH_STATUS:
                    assert msg.obj != null;
                    doDispatchStatus(manager, (String) msg.obj, msg.arg1, msg.getData());
                    break;
                case WHAT_DISPATCH_PROGRESS:
                    assert msg.obj != null;
                    doDispatchProgress(manager, (String) msg.obj, msg.getData());
                    break;
            }
        }

        private void doDispatchStatus(@NonNull ListenerManager manager, @NonNull String key, @InstallStatus int installStatus, @NonNull Bundle data) {
            synchronized (manager.statusListenerMap) {
                LinkedList<InstallStatusListener> keyListeners = manager.statusListenerMap.get(key);
                if (keyListeners != null && !keyListeners.isEmpty()) {
                    for (InstallStatusListener listener : keyListeners) {
                        callbackHandler.postCallbackStatus(listener, data, installStatus);
                    }
                }

                LinkedList<InstallStatusListener> noTargetListeners = manager.statusListenerMap.get(KEY_WATCH_ALL_APP);
                if (noTargetListeners != null && !noTargetListeners.isEmpty()) {
                    for (InstallStatusListener listener : noTargetListeners) {
                        callbackHandler.postCallbackStatus(listener, data, installStatus);
                    }
                }
            }
        }

        private void doDispatchProgress(@NonNull ListenerManager manager, @NonNull String key, @NonNull Bundle data) {
            synchronized (manager.statusListenerMap) {
                LinkedList<InstallProgressListener> keyListeners = manager.progressListenerMap.get(key);
                if (keyListeners != null && !keyListeners.isEmpty()) {
                    for (InstallProgressListener listener : keyListeners) {
                        callbackHandler.postCallbackProgress(listener, data);
                    }
                }

                LinkedList<InstallProgressListener> noTargetListeners = manager.progressListenerMap.get(KEY_WATCH_ALL_APP);
                if (noTargetListeners != null && !noTargetListeners.isEmpty()) {
                    for (InstallProgressListener listener : noTargetListeners) {
                        callbackHandler.postCallbackProgress(listener, data);
                    }
                }
            }
        }
    }

    private static class ListenerCallbackHandler extends Handler {
        private static final int WHAT_CALLBACK_STATUS = 8801;
        private static final int WHAT_CALLBACK_PROGRESS = 8802;

        ListenerCallbackHandler() {
            super(Looper.getMainLooper());
        }

        @AnyThread
        void postCallbackStatus(@NonNull InstallStatusListener listener, @NonNull Bundle data, @InstallStatus int newStatus) {
            Message message = obtainMessage(WHAT_CALLBACK_STATUS, newStatus, 0, listener);
            message.setData(data);
            message.sendToTarget();
        }

        @AnyThread
        void postCallbackProgress(@NonNull InstallProgressListener listener, @NonNull Bundle data) {
            Message message = obtainMessage(WHAT_CALLBACK_PROGRESS, listener);
            message.setData(data);
            message.sendToTarget();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_CALLBACK_STATUS:
                    final String key = msg.getData().getString("key");
                    final String appPackageName = msg.getData().getString("appPackageName");
                    final int appVersionCode = msg.getData().getInt("appVersionCode");
                    assert key != null;
                    doCallbackStatus((InstallStatusListener) msg.obj, key, appPackageName, appVersionCode, msg.arg1);
                    break;
                case WHAT_CALLBACK_PROGRESS:
                    final String key2 = msg.getData().getString("key");
                    final String appPackageName2 = msg.getData().getString("appPackageName");
                    final int appVersionCode2 = msg.getData().getInt("appVersionCode");
                    final long completedLength = msg.getData().getLong("completedLength");
                    final long totalLength = msg.getData().getLong("totalLength");
                    assert key2 != null;
                    doCallbackProgress((InstallProgressListener) msg.obj, key2, appPackageName2, appVersionCode2, completedLength, totalLength);
                    break;
            }
        }

        private void doCallbackStatus(@NonNull InstallStatusListener listener, @NonNull String key, @Nullable String appPackageName,
                                      int appVersionCode, @InstallStatus int newStatus) {
            listener.onInstallStatusChanged(key, appPackageName, appVersionCode, newStatus);
        }

        private void doCallbackProgress(@NonNull InstallProgressListener listener, @NonNull String key, @Nullable String appPackageName,
                                        int appVersionCode, long completedLength, long totalLength) {
            listener.onInstallProgressChanged(key, appPackageName, appVersionCode, completedLength, totalLength);
        }
    }
}
