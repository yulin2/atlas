package org.atlasapi.remotesite.redux;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.atlasapi.persistence.logging.AdapterLogEntry.warnEntry;

import java.util.concurrent.Callable;

import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.redux.model.BaseReduxProgramme;
import org.atlasapi.remotesite.redux.model.FullReduxProgramme;
import org.joda.time.LocalDate;

import com.metabroadcast.common.base.Maybe;

public class ReduxDayUpdateTask implements Callable<UpdateProgress> {

    public static final Builder dayUpdateTaskBuilder(ReduxClient client, ReduxProgrammeHandler handler, AdapterLog log) {
        return new Builder(client, handler, log);
    }
    
    public static final class Builder {
        
        private final ReduxClient client;
        private final AdapterLog log;
        private final ReduxProgrammeHandler handler;

        public Builder(ReduxClient client, ReduxProgrammeHandler handler, AdapterLog log) {
            this.client = client;
            this.handler = handler;
            this.log = log;
        }
        
        public ReduxDayUpdateTask updateFor(LocalDate date) {
            return new ReduxDayUpdateTask(client, handler, log, date);
        }
        
    }
    
    private final ReduxClient client;
    private final ReduxProgrammeHandler handler;
    private final AdapterLog log;
    private final LocalDate date;

    public ReduxDayUpdateTask(ReduxClient client, ReduxProgrammeHandler handler, AdapterLog log, LocalDate date) {
        this.client = checkNotNull(client);
        this.handler = checkNotNull(handler);
        this.log = checkNotNull(log);
        this.date = checkNotNull(date);
    }

    @Override
    public UpdateProgress call() {
        int processed = 0;
        int failed = 0;
        for (BaseReduxProgramme programme : client.programmesForDay(date)) {
            try {
                Maybe<FullReduxProgramme> fullProgramme = client.programmeFor(programme.getDiskref());
                if(fullProgramme.hasValue()) {
                    handler.handle(fullProgramme.requireValue());
                    processed++;
                }
            } catch (Exception e) {
                failed++;
                log.record(warnEntry().withCause(e).withSource(getClass()).withDescription("Exception processing diskref %s", programme.getDiskref()));
            }
        }
        return new UpdateProgress(processed, failed);
    }

}
