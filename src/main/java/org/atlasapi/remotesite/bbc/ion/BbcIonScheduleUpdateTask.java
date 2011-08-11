package org.atlasapi.remotesite.bbc.ion;

import static org.atlasapi.persistence.logging.AdapterLogEntry.debugEntry;
import static org.atlasapi.persistence.logging.AdapterLogEntry.errorEntry;

import java.util.concurrent.Callable;

import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.bbc.ion.model.IonSchedule;

public class BbcIonScheduleUpdateTask implements Callable<Integer> {
    
    private final String scheduleUrl;
    private final BbcIonFeedClient<IonSchedule> scheduleClient;
    private final BbcIonScheduleHandler handler;
    private final AdapterLog log;

    public BbcIonScheduleUpdateTask(String scheduleUrl, BbcIonFeedClient<IonSchedule> scheduleClient, BbcIonScheduleHandler handler, AdapterLog log){
        this.scheduleUrl = scheduleUrl;
        this.scheduleClient = scheduleClient;
        this.handler = handler;
        this.log = log;
    }

    @Override
    public Integer call() throws Exception {
        log.record(debugEntry().withSource(getClass()).withDescription("update schedule: %s", scheduleUrl));
        
        IonSchedule schedule;
        try {
            schedule = scheduleClient.getFeed(scheduleUrl);
        } catch (Exception e) {
            log.record(errorEntry().withCause(e).withSource(getClass()).withDescription("couldn't fetch schedule: %s", scheduleUrl));
            throw e;
        }
        
        return handler.handle(schedule);

    }
}
