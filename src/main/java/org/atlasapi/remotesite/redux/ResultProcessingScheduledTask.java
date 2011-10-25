package org.atlasapi.remotesite.redux;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.collect.Lists;
import com.metabroadcast.common.scheduling.ScheduledTask;

//TODO: split this into more cohesive parts
public final class ResultProcessingScheduledTask<T> extends ScheduledTask {

    private String producerStatus = "";
    private String processorStatus = "";

    private final Executor executor;
    private final Iterable<Callable<T>> producer;
    private final ResultProcessor<? super T, ?> consumer;

    public ResultProcessingScheduledTask(Iterable<Callable<T>> taskProducer, ResultProcessor<? super T, ?> taskProcessor, Executor executor) {
        this.producer = taskProducer;
        this.consumer = taskProcessor;
        this.executor = executor;
    }

    @Override
    protected final void runTask() {
        final CompletionService<T> completer = new ExecutorCompletionService<T>(executor);
        final Semaphore available = new Semaphore(0);
        final List<Future<T>> tasks = Lists.newArrayList();
        final AtomicBoolean producing = new AtomicBoolean(true);

        Thread processor = new Thread(new Runnable() {
            @Override
            public void run() {
                UpdateProgress progress = UpdateProgress.START;
                progress = processorTasks(completer, available, producing, progress);
                processorRemainingTasks(completer, available, progress);
            }
        }, this.getName() + " Result Processor");
        processor.start();

        produceTasks(completer, available, tasks, producing);
        try {
            processor.join();
        } catch (Exception e) {
            return;
        }
    }

    private UpdateProgress produceTasks(CompletionService<T> completer, Semaphore available, List<Future<T>> tasks, AtomicBoolean producing) {
        UpdateProgress progress = UpdateProgress.START;
        Iterator<Callable<T>> taskIterator = producer.iterator();

        while (true) {
            try {
                tasks.add(completer.submit(taskIterator.next()));
                if (!taskIterator.hasNext() || !shouldContinue()) {
                    producing.set(false);
                    available.release();
                    progress = progress.reduce(UpdateProgress.SUCCESS);
                    break;
                }
                available.release();
                progress = progress.reduce(UpdateProgress.SUCCESS);
            } catch (RejectedExecutionException rje) {
                progress = progress.reduce(UpdateProgress.FAILURE);
            }
            updateProducerStatus("Submitting tasks.", progress);
        }

        if (!shouldContinue()) {
            cancelTasks(tasks, progress);
        } else {
            updateProducerStatus("Finished submitting tasks.", progress);
        }
        return progress;
    }

    private void processorRemainingTasks(CompletionService<T> completer, Semaphore available, UpdateProgress progress) {
        while (available.tryAcquire()) {
            try {
                progress = processResult(completer.take(), progress);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    private UpdateProgress processorTasks(CompletionService<T> results, Semaphore available, AtomicBoolean stillProducing, UpdateProgress progress) {
        while (stillProducing.get()) {
            try {
                available.acquire();
                progress = processResult(results.take(), progress);
            } catch (InterruptedException ie) {
                return progress;
            }
        }
        return progress;
    }

    private UpdateProgress processResult(Future<T> result, UpdateProgress consumeProgress) throws InterruptedException {
        if (!result.isCancelled()) {
            try {
                return updateProcessorStatus(consumeProgress.reduce(UpdateProgress.SUCCESS), consumer.process(result.get()));
            } catch (Exception e) {
                return updateProcessorStatus(consumeProgress.reduce(UpdateProgress.FAILURE), null);
            }
        }
        return consumeProgress;
    }

    protected void cancelTasks(List<Future<T>> tasks, UpdateProgress progress) {
        updateProducerStatus("Cancelling submitted tasks.", progress);
        int cancelled = 0;
        for (Future<T> task : tasks) {
            if (task.cancel(false)) {
                cancelled++;
            }
        }
        updateProducerStatus("Cancelled " + cancelled + " submitted tasks.", progress);
    }

    protected final void updateProducerStatus(String prefix, UpdateProgress progress) {
        this.producerStatus = prefix + progress.toString(" %s tasks submitted" + (progress.hasFailures() ? ", %s rejected" : ""));
        updateStatus();
    }

    protected final UpdateProgress updateProcessorStatus(UpdateProgress progress, Object result) {
        this.processorStatus = progress.toString("%s tasks processed" + (progress.hasFailures() ? ", %s failed. " : ". "));
        if (result != null) {
            processorStatus += result.toString();
        }
        updateStatus();
        return progress;
    }

    private void updateStatus() {
        reportStatus(String.format("%s. %s", producerStatus, processorStatus));
    }
}
