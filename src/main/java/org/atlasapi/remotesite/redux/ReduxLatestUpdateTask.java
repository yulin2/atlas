package org.atlasapi.remotesite.redux;

import static org.atlasapi.persistence.logging.AdapterLogEntry.infoEntry;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.redux.ReduxDiskrefUpdateTask.Builder;
import org.atlasapi.remotesite.redux.model.BaseReduxProgramme;
import org.atlasapi.remotesite.redux.model.PaginatedBaseProgrammes;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.metabroadcast.common.query.Selection;

public abstract class ReduxLatestUpdateTask extends ProducerConsumerScheduledTask<UpdateProgress> {
    
    private final Builder taskBuilder;
    private final ReduxClient client;
    private final AdapterLog log;
    
    private int initialOffset;
    private int limit;

    public ReduxLatestUpdateTask(ReduxClient client, ReduxProgrammeHandler handler, AdapterLog log, ExecutorService executor) {
        super(executor);
        this.taskBuilder = ReduxDiskrefUpdateTask.diskRefUpdateTaskBuilder(client, handler, log);
        this.client = client;
        this.log = log;
    }
    
    public ReduxLatestUpdateTask(ReduxClient client, ReduxProgrammeHandler handler, AdapterLog log) {
        this(client, handler, log, Executors.newFixedThreadPool(2, new ThreadFactoryBuilder().setNameFormat("Redux Latest Thread %d").build()));
    }
    
    public ReduxLatestUpdateTask fromOffset(int offset) {
        this.initialOffset = offset;
        return this;
    }
    
    public ReduxLatestUpdateTask withLimit(int limit) {
        this.limit = limit;
        return this;
    }

    @Override
    protected void produceTasks() {
        int offset = initialOffset;
        while(true) {
            
            Selection selection = new Selection(offset);
            PaginatedBaseProgrammes programmes = client.latest(selection);
            
            if (programmes != null) {
                for (BaseReduxProgramme programme : programmes.getResults()) {
                    submitTask(taskBuilder.updateFor(programme.getDiskref()));
                }
                offset = programmes.getLast() + limit;
            } else {
                updateProducerStatus(String.format("Couldn't get programmes for selection %s. %s tasks submitted", selection, submittedTasks()));
                return;
            }
            
            if(finished(programmes)) {
                return;
            }
            
            if(!shouldContinue()) {
                cancelTasks();
                return;
            }
        }
    }

    protected void startTask() {
        log.record(infoEntry().withSource(getClass()).withDescription("Redux Latest update started from offset %s", initialOffset));
    };

    @Override
    protected void finishTask() {
        log.record(infoEntry().withSource(getClass()).withDescription("Redux Latest update finished"));
    }

    protected abstract boolean finished(PaginatedBaseProgrammes programmes);

    protected int limit() {
        return limit;
    }
}
