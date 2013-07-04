package org.atlasapi.remotesite.redux;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.atlasapi.persistence.logging.AdapterLogEntry.warnEntry;

import java.util.concurrent.Callable;

import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.redux.model.BaseReduxProgramme;
import org.joda.time.LocalDate;

import com.metabroadcast.common.scheduling.UpdateProgress;

public class ReduxDayUpdateTask implements Callable<UpdateProgress> {

    public static final Builder dayUpdateTaskBuilder(ReduxClient client, ContentWriter writer, SiteSpecificAdapter<Item> adapter, AdapterLog log) {
        return new Builder(client, writer, adapter, log);
    }
    
    public static final class Builder {
        
        private final ReduxClient client;
        private final AdapterLog log;
        private final SiteSpecificAdapter<Item> adapter;
        private final ContentWriter writer;

        public Builder(ReduxClient client, ContentWriter writer, SiteSpecificAdapter<Item> adapter, AdapterLog log) {
            this.client = client;
            this.writer = writer;
            this.adapter = adapter;
            this.log = log;
        }
        
        public ReduxDayUpdateTask updateFor(LocalDate date) {
            return new ReduxDayUpdateTask(client, writer, adapter, log, date);
        }
        
    }
    
    private final ReduxClient client;
    private final SiteSpecificAdapter<Item> adapter;
    private final AdapterLog log;
    private final LocalDate date;
    private final ContentWriter writer;

    public ReduxDayUpdateTask(ReduxClient client, ContentWriter writer, SiteSpecificAdapter<Item> adapter, AdapterLog log, LocalDate date) {
        this.writer = checkNotNull(writer);
        this.client = checkNotNull(client);
        this.adapter = checkNotNull(adapter);
        this.log = checkNotNull(log);
        this.date = checkNotNull(date);
    }

    @Override
    public UpdateProgress call() throws Exception {
        int processed = 0;
        int failed = 0;
        for (BaseReduxProgramme programme : client.programmesForDay(date)) {
            try {
                writer.createOrUpdate(adapter.fetch(FullProgrammeItemExtractor.REDUX_URI_BASE + programme.getCanonical()));
                processed++;
            } catch (Exception e) {
                failed++;
                log.record(warnEntry().withCause(e).withSource(getClass()).withDescription("Exception processing diskref %s", programme.getDiskref()));
            }
        }
        return new UpdateProgress(processed, failed);
    }

}
