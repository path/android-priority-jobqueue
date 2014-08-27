package com.path.android.jobqueue.flush;

import com.path.android.jobqueue.log.JqLog;

/**
 * Simple implementation of {@link DispatchTimerInterface} that does not keep time.
 * Instead, it will merely call {@link #onFinish()}
 * as soon as {@link #start()} is called.
 */
public class SimpleDispatchTimer implements DispatchTimerInterface {
    private TimerDispatcher clockAction;

    @Override
    public synchronized void start() {
        onFinish();
    }

    @Override
    public synchronized void cancel() {} //do nothing

    @Override
    public synchronized void reset() {
        onFinish();
    }

    @Override
    public void setListener(TimerDispatcher listener) {
        this.clockAction = listener;
    }

    @Override
    public synchronized boolean isRunning() {
        return false; //the zero-timer is never actively running.
    }

    @Override
    public synchronized void onFinish() {
        JqLog.d("dispatching flush!");
        clockAction.onTimeUp();
    }

}
