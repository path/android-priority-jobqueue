package com.path.android.jobqueue.flush;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

import com.path.android.jobqueue.log.JqLog;

/**
 * Factory class for {@link com.path.android.jobqueue.flush.DispatchTimerFactory.DispatchTimer}.
 *
 * @see com.path.android.jobqueue.flush.DispatchTimerFactory.DispatchTimer
 */
public class DispatchTimerFactory {

    public static DispatchTimer createTimer(long dispatchInMs, DispatchTimer.TimerDispatcher clockAction, boolean resetAfterComplete) {
        if (dispatchInMs > 0L) {
            return new DispatchTimer(dispatchInMs, clockAction, resetAfterComplete);
        } else { //(dispatchInMs <=0){
            return new ZeroDispatchTimer(clockAction);
        }
    }

    /**
     * Implementation is very similar to {@link android.os.CountDownTimer}, but since that class marks many of its
     * implemented methods as <code>final</code>, it is infeasible to use.
     * <p>Unlike CountDownTimer, this class does not provide the option to "tick" multiple times; it "ticks" once
     * - when {@link com.path.android.jobqueue.flush.DispatchTimerFactory.DispatchTimer#onFinish()} is called.
     * <p>Most notably, this class can reset itself (boolean in the constructor) upon completion of the countdown, creating a loop that will be called until
     * {@link com.path.android.jobqueue.flush.DispatchTimerFactory.DispatchTimer#cancel()} is called.
     */
    public static class DispatchTimer { //extends CountDownTimer

        public static interface TimerDispatcher {
            public void onTimeUp();
        }

        protected static final int MSG = 1;
        protected long mStopTimeInFuture;
        protected long dispatchInterval;
        protected final TimerDispatcher clockAction;
        protected boolean resetAfterComplete;
        protected boolean running = false;

        /**
         * @param dispatchInMs       how long to wait before calling {@link com.path.android.jobqueue.flush.DispatchTimerFactory.DispatchTimer.TimerDispatcher#onTimeUp()}
         * @param clockAction        the action when time expires
         * @param resetAfterComplete if true, DispatchTimer will reset itself with the same parameters.
         */
        public DispatchTimer(long dispatchInMs, TimerDispatcher clockAction, boolean resetAfterComplete) {
            this.dispatchInterval = dispatchInMs;
            this.clockAction = clockAction;
            this.resetAfterComplete = resetAfterComplete;
        }

        /**
         * Start the countdown.
         */
        public synchronized DispatchTimer start() {
            if (dispatchInterval <= 0) {
                onFinish();
                return this;
            }
            mStopTimeInFuture = SystemClock.elapsedRealtime() + dispatchInterval;
            mHandler.sendMessage(mHandler.obtainMessage(MSG));
            return this;
        }

        /**
         * Cancel the countdown
         */
        public synchronized void cancel() {
            mHandler.removeMessages(MSG);
            running = false;
        }

        /**
         * Called when the time is up.
         */
        public synchronized void onFinish() {
            JqLog.d("dispatching flush!");
            clockAction.onTimeUp();
            running = false;

            if (resetAfterComplete && dispatchInterval > 0) { //ensures that a dispatchInterval of 0 will not create an endless loop
                this.start(); //restart the timer
            }
        }

        public synchronized boolean isRunning() {
            return running;
        }

        public synchronized void reset() {
            this.cancel();
            this.start();
        }

        // handles counting down
        private Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                synchronized (DispatchTimer.this) {
                    running = true;
                    final long millisLeft = mStopTimeInFuture - SystemClock.elapsedRealtime();
                    if (millisLeft <= 0) {
                        onFinish();
                    } else {//if (millisLeft < mCountdownInterval) {
                        // no tick, just delay until done
                        sendMessageDelayed(obtainMessage(MSG), millisLeft);
                    }
                }
            }
        };
    }

    /**
     * Specialty subclass of {@link com.path.android.jobqueue.flush.DispatchTimerFactory.DispatchTimer} that more efficiently handles the case
     * where the user wants no timer. A call to {@link #start()} will immediately call {@link #onFinish()}.
     */
    private static class ZeroDispatchTimer extends DispatchTimer {
        /**
         * @param clockAction the action when time expires
         */
        public ZeroDispatchTimer(TimerDispatcher clockAction) {
            super(0, clockAction, false);
        }

        @Override
        public synchronized DispatchTimer start() {
            onFinish();
            return this;
        }

        @Override
        public synchronized void cancel() {} //do nothing

        @Override
        public synchronized void reset() {
            onFinish();
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
}
