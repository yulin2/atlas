package org.atlasapi.remotesite.music.emipub;

import com.google.common.base.Throwables;
import com.metabroadcast.common.scheduling.ScheduledTask;
import com.metabroadcast.common.time.SystemClock;
import com.metabroadcast.common.time.Timestamp;
import com.metabroadcast.common.time.Timestamper;
import java.io.File;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;

/**
 */
class EmiPubUpdater extends ScheduledTask {

    private final Timestamper timestamper = new SystemClock();
    private final ContentWriter contentWriter;
    private final AdapterLog log;
    private final String dataFile;

    public EmiPubUpdater(ContentWriter contentWriter, AdapterLog log, String dataFile) {
        this.contentWriter = contentWriter;
        this.log = log;
        this.dataFile = dataFile;
    }

    @Override
    public void runTask() {
        try {
            Timestamp start = timestamper.timestamp();
            log.record(new AdapterLogEntry(AdapterLogEntry.Severity.INFO).withDescription("Emi Publishing update started!").withSource(getClass()));

            new EmiPubProcessor().process(new File(dataFile), log, contentWriter);

            Timestamp end = timestamper.timestamp();
            log.record(new AdapterLogEntry(AdapterLogEntry.Severity.INFO).withDescription("Emi Publishing update completed in " + start.durationTo(end).getStandardSeconds() + " seconds").withSource(getClass()));
        } catch (Exception e) {
            log.record(new AdapterLogEntry(AdapterLogEntry.Severity.ERROR).withCause(e).withSource(getClass()).withDescription("Exception when processing Emi Publishing."));
            Throwables.propagate(e);
        }
    }
}
