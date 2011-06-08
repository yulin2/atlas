package org.atlasapi.remotesite.bbc.ion;

import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.joda.time.DateTime;

import com.metabroadcast.common.scheduling.ScheduledTask;
import com.metabroadcast.common.time.DateTimeZones;

public class BbcIonOndemandChangeUpdater extends ScheduledTask {

    private final BbcIonOndemandChangeUpdateBuilder updateBuilder;
    
    private boolean isRunning = false;
    private DateTime lastRun = new DateTime(DateTimeZones.UTC).minusMinutes(30);

    private final AdapterLog log;

    public BbcIonOndemandChangeUpdater(BbcIonOndemandChangeUpdateBuilder updateBuilder, AdapterLog log) {
        this.updateBuilder = updateBuilder;
        this.log = log;
    }

    @Override
    public void runTask() {
        if (!isRunning) {
            isRunning = true;
            log.record(AdapterLogEntry.infoEntry().withSource(getClass()).withDescription(String.format("Ondemand Change Updater starting from %s", lastRun.toDateTime(DateTimeZones.UTC))));
            
            DateTime start = lastRun;
            lastRun = new DateTime(DateTimeZones.UTC);
            
            updateBuilder.updateStartingFrom(start).run();
            
            log.record(AdapterLogEntry.infoEntry().withSource(getClass()).withDescription(String.format("Ondemand Change Updater finished with %s", lastRun.toDateTime(DateTimeZones.UTC))));
            isRunning = false;
        }
    }

}
