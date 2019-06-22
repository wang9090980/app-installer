package me.panpf.app.install.auto;

import android.os.Handler;

import java.lang.ref.WeakReference;

public class FindTargetButtonTask extends DelayPerformTask implements DelayPerformTask.PerformListener {
    private WeakReference<FindTargetButtonListener> findTargetListenerWeakReference;

    public FindTargetButtonTask(FindTargetButtonListener findTargetButtonListener, Handler handler) {
        super(handler, null);

        this.findTargetListenerWeakReference = new WeakReference<>(findTargetButtonListener);
        setPerformListener(this);
    }

    @Override
    public void onPerform() {
        FindTargetButtonListener findTargetButtonListener = findTargetListenerWeakReference.get();
        if(findTargetButtonListener != null){
            findTargetButtonListener.onFindTargetButton();
        }
    }

    public interface FindTargetButtonListener {
        void onFindTargetButton();
    }
}
