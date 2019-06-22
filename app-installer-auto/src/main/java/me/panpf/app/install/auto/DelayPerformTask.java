package me.panpf.app.install.auto;

import android.os.Handler;
import android.os.Looper;

public class DelayPerformTask implements Runnable{
    private Handler handler;
    private boolean finished = true;
    private PerformListener performListener;

    public DelayPerformTask(Handler handler, PerformListener performListener) {
        this.handler = handler;
        if(this.handler == null){
            this.handler = new Handler(Looper.getMainLooper());
        }
        this.performListener = performListener;
    }

    @Override
    public void run() {
        if(!finished){
            finished = true;
            if(performListener != null){
                performListener.onPerform();
            }
        }
    }

    public void postDelayed(int delayMillis){
        if(finished){
            finished = false;
            handler.postDelayed(this, delayMillis);
        }
    }

    public void postDelayed(){
        postDelayed(100);
    }

    public boolean isFinished(){
        return finished;
    }

    public void cancel(){
        handler.removeCallbacks(this);
        finished = true;
    }

    public void setPerformListener(PerformListener performListener) {
        this.performListener = performListener;
    }

    public interface PerformListener {
        void onPerform();
    }
}
