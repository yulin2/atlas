package org.atlasapi.remotesite.redux;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

import com.google.common.collect.Lists;
import com.metabroadcast.common.scheduling.ScheduledTask;

public abstract class ProducerConsumerScheduledTask<T extends Reducible<T>> extends ScheduledTask {
    
    private String producerStatus = "";
    private String consumerStatus = "";
    
    private final Semaphore available = new Semaphore(0);
    private final ExecutorService executor;
    
    private CompletionService<T> completer;
    private ArrayList<Future<T>> tasks;
    
    public ProducerConsumerScheduledTask(ExecutorService executor) {
        this.executor = executor;
    }
    
    @Override
    protected final void runTask() {
        startTask();
        
        this.completer = new ExecutorCompletionService<T>(executor);
        this.tasks = Lists.newArrayList();
        
        Future<?> submitter = executor.submit(new Runnable(){
            @Override
            public void run() {
                produceTasks();
                updateProducerStatus("Finished submitting tasks. " + submittedTasks() + " tasks submitted");
            }
        });
        
        consumeTasks(submitter);
        
        finishTask();
    }

    private void consumeTasks(Future<?> submitter) {
        T base = null;
        try {
            while(!submitter.isDone() || available.availablePermits() > 0) {
                available.acquire();
                T result = processResult(completer.take());
                base = base == null ? result : base.reduce(result);
                updateConsumerStatus(base);
            }
        } catch (InterruptedException e) {
            updateConsumerStatus(base);
            return;
        }
        updateConsumerStatus(base);
    }
    
    private T processResult(Future<T> result) throws InterruptedException {
        if(!result.isCancelled()) {
            try {
                return result.get();
            } catch (ExecutionException e) {
                return null;
            }
        }
        return null;
    }

    protected void finishTask() {
    }

    protected void startTask() {
    }

    protected abstract void produceTasks();
    
    protected void cancelTasks() {
        updateProducerStatus("Cancelling submitted tasks. " + tasks.size() + " tasks submitted");
        int cancelled = 0;
        for (Future<T> task : tasks) {
            if(task.cancel(false)){
                cancelled++;
            }
        }
        updateProducerStatus("Cancelled " + cancelled + " submitted tasks. " + tasks.size() + " tasks submitted");
    }
    
    protected int submittedTasks() {
        return tasks.size();
    }

    protected void submitTask(Callable<T> task) {
        tasks.add(completer.submit(task));
        available.release();
        updateProducerStatus("Submitting tasks. " + tasks.size() + " tasks submitted");
    }
    
    protected final void updateProducerStatus(String status) {
        this.producerStatus = status;
        updateStatus();
    }
    
    protected final void updateConsumerStatus(Object status) {
        if(status != null) {
            this.consumerStatus = status.toString();
            updateStatus();
        }
    }
    
    private void updateStatus() {
        reportStatus(String.format("%s. %s", producerStatus, consumerStatus));
    }
}
