package com.path.android.jobqueue.test.jobmanager;

import com.path.android.jobqueue.AsyncCancelCallback;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.JobHolder;
import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.JobQueue;
import com.path.android.jobqueue.Params;
import com.path.android.jobqueue.test.jobs.DummyJob;

import org.fest.reflect.core.Reflection;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class CancelInBackgroundTest extends JobManagerTestBase {
    @Test
    public void testCancelInBackground() throws Exception {
        cancelInBackground(true);
        cancelInBackground(false);
    }

    public void cancelInBackground(final boolean useCallback) throws Exception {
        long currentThreadId = Thread.currentThread().getId();
        final AtomicLong onCancelThreadId = new AtomicLong();
        final CountDownLatch cancelLatch = new CountDownLatch(2);

        Job dummyJob = new DummyJob(new Params(1)) {
            @Override
            public void onCancel() {
                super.onCancel();
                onCancelThreadId.set(Thread.currentThread().getId());
                cancelLatch.countDown();
            }
        };
        JobManager jobManager = createJobManager();
        jobManager.stop();
        final long jobId = jobManager.addJob(dummyJob);
        final JobQueue queue = getNonPersistentQueue(jobManager);
        assertNotNull(queue.findJobById(jobId));
        if (useCallback) {
            jobManager.cancelJobInBackground(jobId, false, new AsyncCancelCallback() {
                @Override
                public void onCancel(long callbackJobId, boolean isCanceled) {
                    assertEquals("jobId should be passsed to callback. id:" + jobId + ", use cb: true"
                            , jobId, callbackJobId);
                    assertTrue("isCanceled should be true" , isCanceled);
                    cancelLatch.countDown();
                }
            });
        } else {
            cancelLatch.countDown();
            jobManager.cancelJobInBackground(jobId, false);
        }
        cancelLatch.await();
        MatcherAssert.assertThat("thread ids should be different." , currentThreadId, CoreMatchers.not(onCancelThreadId.get()));
        if (useCallback) {
            JobHolder holder = queue.findJobById(jobId);
            MatcherAssert.assertThat("there should not be a job in the holder. id:" + jobId + ", use cb: true"
                    , holder, CoreMatchers.nullValue());
        }
    }

    protected JobQueue getNonPersistentQueue(JobManager jobManager) {
        return Reflection.field("nonPersistentJobQueue").ofType(JobQueue.class).in(jobManager).get();
    }
}
