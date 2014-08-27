package com.path.android.jobqueue.flush;

/**
 * Interface that can be provided to {@link com.path.android.jobqueue.JobManager} to support a time-based
 * flush.
 */
public interface DispatchTimerInterface {
    /** starts the DispatchTimer*/
    public void start();
    /** cancels the DispatchTimer*/
    public void cancel();
    /** the method called when time expires*/
    public void onFinish();
    /** used to determine if the DispatchTimer is currently running*/
    public boolean isRunning();
    /** Restarts the Timer without calling {@link #onFinish()}. Ought to be equivalent to calling
     * {@link #cancel()} and then {@link #start()}*/
    public void reset();
    /** @param dispatcher the action when time expires */
    public void setListener(TimerDispatcher dispatcher);

    /**
     * Callback interface - called when time expires.
     */
    public static interface TimerDispatcher {
        public void onTimeUp();
    }
}
