package org.atlasapi.remotesite.rovi.processing;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.atlasapi.remotesite.rovi.RoviCanonicalUriGenerator.canonicalUriForProgram;
import static org.atlasapi.remotesite.rovi.RoviPredicates.IS_DELETE;

import java.io.File;
import java.io.IOException;

import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.remotesite.rovi.RoviConstants;
import org.atlasapi.remotesite.rovi.indexing.KeyedFileIndex;
import org.atlasapi.remotesite.rovi.indexing.MapBasedKeyedFileIndexer;
import org.atlasapi.remotesite.rovi.model.ScheduleLine;
import org.atlasapi.remotesite.rovi.parsers.ScheduleLineParser;
import org.atlasapi.remotesite.rovi.populators.ScheduleLineBroadcastExtractor;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.metabroadcast.common.base.Maybe;


public class ScheduleFileProcessor implements FileProcessor {

    private static final Logger log = LoggerFactory.getLogger(ScheduleFileProcessor.class);
    
    private final ItemBroadcastUpdater itemBroadcastUpdater;
    private final ScheduleLineBroadcastExtractor scheduleLineBroadcastExtractor;
    private int processedItems = 0;
    private int failedItems = 0;

    public ScheduleFileProcessor(ItemBroadcastUpdater itemBroadcastUpdater, 
            ScheduleLineBroadcastExtractor scheduleLineBroadcastExtractor) {
        this.itemBroadcastUpdater = checkNotNull(itemBroadcastUpdater);
        this.scheduleLineBroadcastExtractor = checkNotNull(scheduleLineBroadcastExtractor);
    }
    
    public RoviDataProcessingResult process(File scheduleFile) throws IOException {
        MapBasedKeyedFileIndexer<String, ScheduleLine> scheduleIndexer = new MapBasedKeyedFileIndexer<>(
                RoviConstants.FILE_CHARSET,
                new ScheduleLineParser());

        DateTime start = DateTime.now(DateTimeZone.UTC);
        log.trace("Start processing schedule ingest of {}", scheduleFile.getCanonicalPath());
        KeyedFileIndex<String, ScheduleLine> index = scheduleIndexer.index(scheduleFile, Predicates.not(IS_DELETE));
        
        try {
            for (String programmeId : index.getKeys()) {
                log.trace("Processing programme ID {}", programmeId);
                try {
                    FluentIterable<Broadcast> broadcasts = FluentIterable.from(index.getLinesForKey(programmeId))
                                                                         .transform(toBroadcast)
                                                                         .filter(Maybe.HAS_VALUE)
                                                                         .transform(Maybe.<Broadcast>requireValueFunction());

                    itemBroadcastUpdater.addBroadcasts(canonicalUriForProgram(programmeId), broadcasts);
                    processedItems++;
                } catch (Exception e) {
                    log.error("Rovi programme ID " + programmeId, e);
                    failedItems++;
                }
            }
            log.trace("Done processing schedule ingest of {}", scheduleFile.getCanonicalPath());
        }
        finally {
            index.releaseResources();
        }

        return new RoviDataProcessingResult(processedItems,
                failedItems,
                start,
                DateTime.now(DateTimeZone.UTC));
    }
    
    private final Function<ScheduleLine, Maybe<Broadcast>> toBroadcast = new Function<ScheduleLine, Maybe<Broadcast>>() {

        @Override
        public Maybe<Broadcast> apply(ScheduleLine scheduleLine) {
            return scheduleLineBroadcastExtractor.extract(scheduleLine);
        }
        
    };
}
