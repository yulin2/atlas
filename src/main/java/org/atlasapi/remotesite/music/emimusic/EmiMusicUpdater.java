package org.atlasapi.remotesite.music.emimusic;

import com.google.common.base.Throwables;
import com.metabroadcast.common.scheduling.ScheduledTask;
import com.metabroadcast.common.time.SystemClock;
import com.metabroadcast.common.time.Timestamp;
import com.metabroadcast.common.time.Timestamper;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.s3.DefaultS3Client;
import org.atlasapi.s3.S3Client;

/**
 */
public class EmiMusicUpdater extends ScheduledTask {

    static final String S3_BUCKET = "dlf-emimusic";
    //
    private final Timestamper timestamper = new SystemClock();
    private final ContentWriter contentWriter;
    private final AdapterLog log;
    private S3Client client;

    public EmiMusicUpdater(ContentWriter contentWriter, AdapterLog log, String s3Access, String s3Secret) {
        this.contentWriter = contentWriter;
        this.log = log;
        this.client = new DefaultS3Client(s3Access, s3Secret, S3_BUCKET);
    }

    @Override
    public void runTask() {
        try {
            Timestamp start = timestamper.timestamp();
            log.record(new AdapterLogEntry(AdapterLogEntry.Severity.INFO).withDescription("EMI Music Updater started!").withSource(getClass()));
            new EmiMusicProcessor().process(client, contentWriter, log);
            log.record(new AdapterLogEntry(AdapterLogEntry.Severity.INFO).withDescription("EMI Music Updater completed in " + start.durationTo(timestamper.timestamp()).getStandardSeconds() + " seconds").withSource(getClass()));
        } catch (Exception e) {
            log.record(new AdapterLogEntry(AdapterLogEntry.Severity.ERROR).withCause(e).withSource(getClass()).withDescription("Exception when processing EMI Music."));
            Throwables.propagate(e);
        }
    }
}
