package org.atlasapi.messaging.workers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.atlasapi.messaging.BeginReplayMessage;
import org.atlasapi.messaging.EndReplayMessage;
import org.atlasapi.messaging.EntityUpdatedMessage;
import org.atlasapi.messaging.ReplayMessage;
import org.atlasapi.messaging.worker.Worker;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 */
public class ReplayingWorkerTest {

    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Test
    public void testNormalProcessing() {
        Worker delegate = mock(Worker.class);
        long threshold = 100;

        ReplayingWorker replayingWorker = new ReplayingWorker(delegate);
        replayingWorker.setReplayThreshold(threshold);
        try {
            replayingWorker.start();

            replayingWorker.process(mockedMessageDispatchingTo(delegate));

            verify(delegate, times(1)).process(any(EntityUpdatedMessage.class));
        } finally {
            replayingWorker.destroy();
        }
    }

    @Test
    public void testReplayProcessing() {
        Worker delegate = mock(Worker.class);
        long threshold = 100;

        ReplayingWorker replayingWorker = new ReplayingWorker(delegate);
        replayingWorker.setReplayThreshold(threshold);
        try {
            replayingWorker.start();

            ReplayMessage replay = mock(ReplayMessage.class);
            EntityUpdatedMessage original = mockedMessageDispatchingTo(delegate);
            when(replay.getOriginal()).thenReturn(original);

            replayingWorker.process(mock(BeginReplayMessage.class));
            replayingWorker.process(replay);
            replayingWorker.process(mock(EndReplayMessage.class));

            verify(delegate, times(1)).process(any(EntityUpdatedMessage.class));
        } finally {
            replayingWorker.destroy();
        }
    }

    @Test
    public void testNormalProcessingIsSuspendedDuringReplay() throws InterruptedException {
        final Worker delegate = mock(Worker.class);
        final long threshold = 10000;

        final ReplayingWorker replayingWorker = new ReplayingWorker(delegate);
        replayingWorker.setReplayThreshold(threshold);
        try {
            replayingWorker.start();

            replayingWorker.process(mock(BeginReplayMessage.class));

            executor.submit(new Runnable() {

                @Override
                public void run() {
                    replayingWorker.process(mockedMessageDispatchingTo(delegate));
                }
            });

            Thread.sleep(5000);

            verify(delegate, times(0)).process(any(EntityUpdatedMessage.class));
        } finally {
            replayingWorker.destroy();
        }
    }

    @Test
    public void testNormalProcessingIsResumedAfterReplay() throws InterruptedException {
        final Worker delegate = mock(Worker.class);
        final long threshold = 10000;

        final ReplayingWorker replayingWorker = new ReplayingWorker(delegate);
        replayingWorker.setReplayThreshold(threshold);
        final CountDownLatch processLatch = new CountDownLatch(1);
        try {
            replayingWorker.start();

            replayingWorker.process(mock(BeginReplayMessage.class));

            executor.submit(new Runnable() {

                @Override
                public void run() {
                    replayingWorker.process(mockedMessageDispatchingTo(delegate));
                    processLatch.countDown();
                }
            });

            replayingWorker.process(mock(EndReplayMessage.class));

            assertTrue(processLatch.await(1, TimeUnit.SECONDS));

            verify(delegate, times(1)).process(any(EntityUpdatedMessage.class));
        } finally {
            replayingWorker.destroy();
        }
    }

    @Test
    public void testNormalProcessingIsResumedAfterReplayInterruption() throws InterruptedException {
        final Worker delegate = mock(Worker.class);
        final long threshold = 1000;

        final ReplayingWorker replayingWorker = new ReplayingWorker(delegate);
        replayingWorker.setReplayThreshold(threshold);
        final CountDownLatch processLatch = new CountDownLatch(1);
        try {
            replayingWorker.start();

            replayingWorker.process(mock(BeginReplayMessage.class));

            executor.submit(new Runnable() {

                @Override
                public void run() {
                    replayingWorker.process(mockedMessageDispatchingTo(delegate));
                    processLatch.countDown();
                }
            });

            assertTrue(processLatch.await(5000, TimeUnit.SECONDS));

            verify(delegate, times(1)).process(any(EntityUpdatedMessage.class));
        } finally {
            replayingWorker.destroy();
        }
    }

    private EntityUpdatedMessage mockedMessageDispatchingTo(final Worker worker) {
        final EntityUpdatedMessage message = mock(EntityUpdatedMessage.class);
        doAnswer(new Answer() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                worker.process(message);
                return null;
            }
        }).when(message).dispatchTo(worker);
        return message;
    }
}
