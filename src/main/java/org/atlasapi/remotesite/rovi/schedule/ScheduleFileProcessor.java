package org.atlasapi.remotesite.rovi.schedule;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;

import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.remotesite.rovi.KeyedFileIndex;
import org.atlasapi.remotesite.rovi.MapBasedKeyedFileIndexer;
import org.atlasapi.remotesite.rovi.RoviConstants;
import org.atlasapi.remotesite.rovi.RoviUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.metabroadcast.common.base.Maybe;


public class ScheduleFileProcessor {

    private static final Logger log = LoggerFactory.getLogger(ScheduleFileProcessor.class);
    
    private final ItemBroadcastUpdater itemBroadcastUpdater;
    private final ScheduleLineBroadcastExtractor scheduleLineBroadcastExtractor;
    
    public ScheduleFileProcessor(ItemBroadcastUpdater itemBroadcastUpdater, 
            ScheduleLineBroadcastExtractor scheduleLineBroadcastExtractor) {
        this.itemBroadcastUpdater = checkNotNull(itemBroadcastUpdater);
        this.scheduleLineBroadcastExtractor = checkNotNull(scheduleLineBroadcastExtractor);
    }
    
    public void process(File scheduleFile) throws IOException {
        MapBasedKeyedFileIndexer<String, ScheduleLine> scheduleIndexer = new MapBasedKeyedFileIndexer<>(
                scheduleFile,
                RoviConstants.FILE_CHARSET,
                new ScheduleLineParser());
        
        log.trace("Start processing schedule ingest of {}", scheduleFile.getCanonicalPath());
        KeyedFileIndex<String, ScheduleLine> index = scheduleIndexer.index() ;
        for (String programmeId : index.getKeys()) {
            log.trace("Processing programme ID {}", programmeId);
            try {
                FluentIterable<Broadcast> broadcasts = FluentIterable.from(index.getLinesForKey(programmeId))
                                                                     .transform(toBroadcast)
                                                                     .filter(Maybe.HAS_VALUE)
                                                                     .transform(Maybe.<Broadcast>requireValueFunction());
                
                itemBroadcastUpdater.addBroadcasts(RoviUtils.canonicalUriForProgram(programmeId), broadcasts);
            } catch (Exception e) {
                log.error("Rovi programme ID " + programmeId, e);
            }
        }
        log.trace("Done processing schedule ingest of {}", scheduleFile.getCanonicalPath());
    }
    
    private final Function<ScheduleLine, Maybe<Broadcast>> toBroadcast = new Function<ScheduleLine, Maybe<Broadcast>>() {

        @Override
        public Maybe<Broadcast> apply(ScheduleLine scheduleLine) {
            return scheduleLineBroadcastExtractor.extract(scheduleLine);
        }
        
    };
}
