package org.atlasapi.remotesite.redux;

import static org.atlasapi.persistence.logging.AdapterLogEntry.warnEntry;

import java.util.concurrent.Callable;

import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.redux.model.FullReduxProgramme;

import com.metabroadcast.common.base.Maybe;

public class ReduxDiskrefUpdateTask implements Callable<UpdateProgress>{

    public static final Builder diskRefUpdateTaskBuilder(ReduxClient client, ReduxProgrammeHandler handler, AdapterLog log) {
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
        
        public ReduxDiskrefUpdateTask updateFor(String diskRef) {
            return new ReduxDiskrefUpdateTask(client, handler, log, diskRef);
        }
        
    }
    
    private final ReduxClient client;
    private final ReduxProgrammeHandler handler;
    private final String diskRef;
    private final AdapterLog log;

    public ReduxDiskrefUpdateTask(ReduxClient client, ReduxProgrammeHandler handler, AdapterLog log, String diskRef) {
        this.client = client;
        this.handler = handler;
        this.log = log;
        this.diskRef = diskRef;
    }

    @Override
    public UpdateProgress call() throws Exception {
        try {
            Maybe<FullReduxProgramme> possibleProgramme = client.programmeFor(diskRef);
            if (possibleProgramme.isNothing()) {
                return UpdateProgress.FAILURE;
            }
            handler.handle(possibleProgramme.requireValue());
            return UpdateProgress.SUCCESS;
        } catch (Exception e) {
            log.record(warnEntry().withSource(getClass()).withCause(e).withDescription("Exception updating diskref %s", diskRef));
            return UpdateProgress.FAILURE;
        }
    }
}