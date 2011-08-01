package org.atlasapi.remotesite.bbc.ion;

import static org.atlasapi.persistence.logging.AdapterLogEntry.debugEntry;
import static org.atlasapi.persistence.logging.AdapterLogEntry.errorEntry;

import java.util.concurrent.Callable;

import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.bbc.BbcIonScheduleClient;
import org.atlasapi.remotesite.bbc.ion.model.IonSchedule;
import org.joda.time.DateTime;

public class BbcIonScheduleUpdateTask implements Callable<Integer> {
    
    private final String serviceKey;
    private final DateTime date;
    private final BbcIonScheduleClient scheduleClient;
    private final BbcIonScheduleHandler handler;
    private final AdapterLog log;



    public BbcIonScheduleUpdateTask(String serviceKey, DateTime date, BbcIonScheduleClient scheduleClient, BbcIonScheduleHandler handler, AdapterLog log){
        this.serviceKey = serviceKey;
        this.date = date;
        this.scheduleClient = scheduleClient;
        this.handler = handler;
        this.log = log;
    }

    @Override
    public Integer call() throws Exception {
        log.record(debugEntry().withSource(getClass()).withDescription("BBC Ion Schedule update %s %s", serviceKey, date.toString()));
        
        IonSchedule schedule;
        try {
            schedule = scheduleClient.scheduleFor(serviceKey, date);
        } catch (Exception e) {
            log.record(errorEntry().withCause(e).withSource(getClass()).withDescription("BBC Ion Schedule couldn't fetch schedule %s %s", serviceKey, date.toString()));
            throw e;
        }
        
        return handler.handle(schedule);

    }
}
