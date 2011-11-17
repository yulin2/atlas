package org.atlasapi.remotesite.bbc.ion;

import static org.atlasapi.persistence.logging.AdapterLogEntry.debugEntry;
import static org.atlasapi.persistence.logging.AdapterLogEntry.errorEntry;

import java.util.concurrent.Callable;

import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.bbc.ion.model.IonBroadcast;
import org.atlasapi.remotesite.bbc.ion.model.IonSchedule;

public class BbcIonScheduleUpdateTask implements Callable<Integer> {

    private final String scheduleUrl;
    private final RemoteSiteClient<IonSchedule> scheduleClient;
    private final BbcIonBroadcastHandler handler;
    private final AdapterLog log;

    public BbcIonScheduleUpdateTask(String scheduleUrl, RemoteSiteClient<IonSchedule> scheduleClient, BbcIonBroadcastHandler handler, AdapterLog log) {
        this.scheduleUrl = scheduleUrl;
        this.scheduleClient = scheduleClient;
        this.handler = handler;
        this.log = log;
    }

    @Override
    public Integer call() throws Exception {
        log.record(debugEntry().withSource(getClass()).withDescription("update schedule: %s", scheduleUrl));

        try {
            IonSchedule schedule = scheduleClient.get(scheduleUrl);
            for (IonBroadcast broadcast : schedule.getBlocklist()) {
                handler.handle(broadcast);
            }
            return schedule.getBlocklist().size();
        } catch (Exception e) {
            log.record(errorEntry().withCause(e).withSource(getClass()).withDescription("exception handling schedule schedule: %s", scheduleUrl));
            throw e;
        }
    }
}
