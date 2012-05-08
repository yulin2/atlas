package org.atlasapi.remotesite.music.musicbrainz;

import com.google.common.base.Throwables;
import com.metabroadcast.common.scheduling.ScheduledTask;
import com.metabroadcast.common.time.SystemClock;
import com.metabroadcast.common.time.Timestamp;
import com.metabroadcast.common.time.Timestamper;
import java.io.File;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.people.ItemsPeopleWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;

/**
 */
class MusicBrainzUpdater extends ScheduledTask {

    private final Timestamper timestamper = new SystemClock();
    private final ContentWriter contentWriter;
    private final ItemsPeopleWriter peopleWriter;
    private final AdapterLog log;
    private final String dataDir;

    public MusicBrainzUpdater(ContentWriter contentWriter, ItemsPeopleWriter peopleWriter, AdapterLog log, String dataDir) {
        this.contentWriter = contentWriter;
        this.peopleWriter = peopleWriter;
        this.log = log;
        this.dataDir = dataDir;
    }

    @Override
    public void runTask() {
        try {
            Timestamp start = timestamper.timestamp();
            log.record(new AdapterLogEntry(AdapterLogEntry.Severity.INFO).withDescription("Music Brainz update started!").withSource(getClass()));

            new MusicBrainzProcessor().process(new File(dataDir), contentWriter, peopleWriter);

            Timestamp end = timestamper.timestamp();
            log.record(new AdapterLogEntry(AdapterLogEntry.Severity.INFO).withDescription("Music Brainz update completed in " + start.durationTo(end).getStandardSeconds() + " seconds").withSource(getClass()));
        } catch (Exception e) {
            log.record(new AdapterLogEntry(AdapterLogEntry.Severity.ERROR).withCause(e).withSource(getClass()).withDescription("Exception when processing Music Brainz."));
            Throwables.propagate(e);
        }
    }
}
