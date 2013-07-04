package org.atlasapi.remotesite.redux;

import static org.atlasapi.persistence.logging.AdapterLogEntry.warnEntry;

import java.util.concurrent.Callable;

import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.SiteSpecificAdapter;

import com.metabroadcast.common.scheduling.UpdateProgress;

public class ReduxDiskrefUpdateTask implements Callable<UpdateProgress>{

    public static final Builder diskRefUpdateTaskBuilder(ContentWriter writer,  SiteSpecificAdapter<Item> handler, AdapterLog log) {
        return new Builder(writer, handler, log);
    }
    
    public static final class Builder {
        
        private final ContentWriter writer;
        private final AdapterLog log;
        private final SiteSpecificAdapter<Item> handler;

        public Builder(ContentWriter writer,  SiteSpecificAdapter<Item> handler, AdapterLog log) {
            this.writer = writer;
            this.handler = handler;
            this.log = log;
        }
        
        public ReduxDiskrefUpdateTask updateFor(String diskRef) {
            return new ReduxDiskrefUpdateTask(writer, handler, log, diskRef);
        }
        
    }
    
    private final ContentWriter writer;
    private final SiteSpecificAdapter<Item> handler;
    private final String diskRef;
    private final AdapterLog log;

    public ReduxDiskrefUpdateTask(ContentWriter writer, SiteSpecificAdapter<Item> handler, AdapterLog log, String diskRef) {
        this.writer = writer;
        this.handler = handler;
        this.log = log;
        this.diskRef = diskRef;
    }

    @Override
    public UpdateProgress call() throws Exception {
        try {
            writer.createOrUpdate(handler.fetch(FullProgrammeItemExtractor.REDUX_PROGRAMME_URI_BASE + diskRef));
            return UpdateProgress.SUCCESS;
        } catch (Exception e) {
            log.record(warnEntry().withSource(getClass()).withCause(e).withDescription("Exception updating diskref %s", diskRef));
            return UpdateProgress.FAILURE;
        }
    }
}