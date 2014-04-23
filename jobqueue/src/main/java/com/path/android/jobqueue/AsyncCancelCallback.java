package com.path.android.jobqueue;

/**
 * If you are canceling the job via the async canceler, you can provide a callback method to receive result.
 * Please keep in mind that job manager will keep a strong reference to this callback. So if the callback is an
 * anonymous class inside an {@link android.app.Activity} context, it may leak the activity until the job is canceled.
 */
public interface AsyncCancelCallback {
    public void onCancel(long jobId, boolean isCanceled);
}
