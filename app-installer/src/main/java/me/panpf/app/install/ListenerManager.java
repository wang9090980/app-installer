package me.panpf.app.install;

import android.os.*;
import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;

import java.lang.ref.WeakReference;
import java.util.LinkedList;

@SuppressWarnings("WeakerAccess")
public class ListenerManager {
    private static final String KEY_WATCH_ALL_APP = "KEY_WATCH_ALL_APP";

    @NonNull
    private final ArrayMap<String, LinkedList<StatusChangedListener>> statusListenerMap = new ArrayMap<>();
    @NonNull
    private final ArrayMap<String, LinkedList<DecompressProgressChangedListener>> progressListenerMap = new ArrayMap<>();

    @NonNull
    private ListenerDispatchHandler listenerDispatchHandler;

    ListenerManager(@NonNull HandlerThread handlerThread) {
        this.listenerDispatchHandler = new ListenerDispatchHandler(this, handlerThread);
    }

    /**
     * 注册状态监听器
     *
     * @param key      key
     * @param listener 状态监听器
     */
    @AnyThread
    public void registerStatusListener(@NonNull String key, @NonNull final StatusChangedListener listener) {
        //noinspection ConstantConditions
        if (listener != null) {
            synchronized (statusListenerMap) {
                LinkedList<StatusChangedListener> listeners = statusListenerMap.get(key);
                if (listeners == null) {
                    listeners = new LinkedList<>();
                    statusListenerMap.put(key, listeners);
                }
                listeners.add(listener);
            }
        }
    }

    /**
     * 注册状态监听器，不关心某一个 app 而是关心所有的应用
     *
     * @param listener 状态监听器
     */
    @AnyThread
    public void registerStatusListener(@NonNull final StatusChangedListener listener) {
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
    public boolean unregisterStatusListener(@NonNull String key, @NonNull final StatusChangedListener listener) {
        synchronized (statusListenerMap) {
            //noinspection ConstantConditions
            if (listener != null && !statusListenerMap.isEmpty()) {
                LinkedList<StatusChangedListener> listeners = statusListenerMap.get(key);
                if (listeners != null && !listeners.isEmpty()) {
                    return listeners.remove(listener);
                }
            }
        }
        return false;
    }

    /**
     * 删除状态监听器，不关心某一个 app 而是关心所有的应用
     *
     * @param listener 状态监听器
     * @return true：删除成功；false：未注册或 listener 为 null
     */
    @AnyThread
    public boolean unregisterStatusListener(@NonNull final StatusChangedListener listener) {
        return unregisterStatusListener(KEY_WATCH_ALL_APP, listener);
    }

    /**
     * 注册进度监听器
     *
     * @param key      key
     * @param listener 进度监听器
     */
    @AnyThread
    public void registerProgressListener(@NonNull String key, @NonNull final DecompressProgressChangedListener listener) {
        //noinspection ConstantConditions
        if (listener != null) {
            synchronized (progressListenerMap) {
                LinkedList<DecompressProgressChangedListener> listeners = progressListenerMap.get(key);
                if (listeners == null) {
                    listeners = new LinkedList<>();
                    progressListenerMap.put(key, listeners);
                }
                listeners.add(listener);
            }
        }
    }

    /**
     * 注册进度监听器，不关心某一个 app 而是关心所有的应用
     *
     * @param listener 进度监听器
     */
    @AnyThread
    public void registerProgressListener(@NonNull final DecompressProgressChangedListener listener) {
        registerProgressListener(KEY_WATCH_ALL_APP, listener);
    }

    /**
     * 删除进度监听器
     *
     * @param key      key
     * @param listener 进度监听器
     */
    @AnyThread
    public void unregisterProgressListener(@NonNull String key, @NonNull final DecompressProgressChangedListener listener) {
        synchronized (progressListenerMap) {
            //noinspection ConstantConditions
            if (listener != null && !progressListenerMap.isEmpty()) {
                LinkedList<DecompressProgressChangedListener> listeners = progressListenerMap.get(key);
                if (listeners != null && !listeners.isEmpty()) {
                    listeners.remove(listener);
                }
            }
        }
    }

    /**
     * 删除进度监听器，不关心某一个 app 而是关心所有的应用
     *
     * @param listener 进度监听器
     */
    @AnyThread
    public void unregisterProgressListener(@NonNull final DecompressProgressChangedListener listener) {
        unregisterProgressListener(KEY_WATCH_ALL_APP, listener);
    }

    @AnyThread
    public void postCallbackStatus(@NonNull String key, @InstallStatus int newStatus) {
        listenerDispatchHandler.postDispatchStatus(key, newStatus);
    }

    @AnyThread
    public void postCallbackProgress(@NonNull String key, long totalLength, long completedLength) {
        listenerDispatchHandler.postDispatchProgress(key, totalLength, completedLength);
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
        void postDispatchStatus(@NonNull String key, @InstallStatus int newStatus) {
            Message message = obtainMessage(WHAT_DISPATCH_STATUS, newStatus, 0, key);

            Bundle bundle = new Bundle();
            bundle.putString("key", key);
            message.setData(bundle);

            message.sendToTarget();
        }

        @AnyThread
        void postDispatchProgress(@NonNull String key, long totalLength, long completedLength) {
            Message message = obtainMessage(WHAT_DISPATCH_PROGRESS, key);

            Bundle bundle = new Bundle();
            bundle.putString("key", key);
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

        private void doDispatchStatus(@NonNull ListenerManager manager, @NonNull String key, @InstallStatus int newStatus, @NonNull Bundle data) {
            synchronized (manager.statusListenerMap) {
                LinkedList<StatusChangedListener> keyListeners = manager.statusListenerMap.get(key);
                if (keyListeners != null && !keyListeners.isEmpty()) {
                    for (StatusChangedListener listener : keyListeners) {
                        callbackHandler.postCallbackStatus(listener, data, newStatus);
                    }
                }

                LinkedList<StatusChangedListener> noTargetListeners = manager.statusListenerMap.get(KEY_WATCH_ALL_APP);
                if (noTargetListeners != null && !noTargetListeners.isEmpty()) {
                    for (StatusChangedListener listener : noTargetListeners) {
                        callbackHandler.postCallbackStatus(listener, data, newStatus);
                    }
                }
            }
        }

        private void doDispatchProgress(@NonNull ListenerManager manager, @NonNull String key, @NonNull Bundle data) {
            synchronized (manager.statusListenerMap) {
                LinkedList<DecompressProgressChangedListener> keyListeners = manager.progressListenerMap.get(key);
                if (keyListeners != null && !keyListeners.isEmpty()) {
                    for (DecompressProgressChangedListener listener : keyListeners) {
                        callbackHandler.postCallbackProgress(listener, data);
                    }
                }

                LinkedList<DecompressProgressChangedListener> noTargetListeners = manager.progressListenerMap.get(KEY_WATCH_ALL_APP);
                if (noTargetListeners != null && !noTargetListeners.isEmpty()) {
                    for (DecompressProgressChangedListener listener : noTargetListeners) {
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
        void postCallbackStatus(@NonNull StatusChangedListener listener, @NonNull Bundle data, @InstallStatus int newStatus) {
            Message message = obtainMessage(WHAT_CALLBACK_STATUS, newStatus, 0, listener);
            message.setData(data);
            message.sendToTarget();
        }

        @AnyThread
        void postCallbackProgress(@NonNull DecompressProgressChangedListener listener, @NonNull Bundle data) {
            Message message = obtainMessage(WHAT_CALLBACK_PROGRESS, listener);
            message.setData(data);
            message.sendToTarget();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_CALLBACK_STATUS:
                    final String key = msg.getData().getString("key");
                    assert key != null;
                    doCallbackStatus((StatusChangedListener) msg.obj, key, msg.arg1);
                    break;
                case WHAT_CALLBACK_PROGRESS:
                    final String key2 = msg.getData().getString("key");
                    final long completedLength = msg.getData().getLong("completedLength");
                    final long totalLength = msg.getData().getLong("totalLength");
                    assert key2 != null;
                    doCallbackProgress((DecompressProgressChangedListener) msg.obj, key2, totalLength, completedLength);
                    break;
            }
        }

        private void doCallbackStatus(@NonNull StatusChangedListener listener, @NonNull String key, @InstallStatus int newStatus) {
            listener.onStatusChanged(key, newStatus);
        }

        private void doCallbackProgress(@NonNull DecompressProgressChangedListener listener, @NonNull String key, long totalLength, long completedLength) {
            listener.onDecompressProgressChanged(key, totalLength, completedLength);
        }
    }
}
