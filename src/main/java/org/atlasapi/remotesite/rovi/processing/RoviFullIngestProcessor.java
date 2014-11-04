package org.atlasapi.remotesite.rovi.processing;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.atlasapi.remotesite.rovi.RoviConstants.FILE_CHARSET;
import static org.atlasapi.remotesite.rovi.RoviPredicates.IS_BRAND_NO_PARENT;
import static org.atlasapi.remotesite.rovi.RoviPredicates.IS_BRAND_WITH_PARENT;
import static org.atlasapi.remotesite.rovi.RoviPredicates.NO_BRAND_NO_PARENT;
import static org.atlasapi.remotesite.rovi.RoviPredicates.NO_BRAND_WITH_PARENT;

import java.io.File;
import java.io.IOException;

import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.remotesite.rovi.RoviContentWriter;
import org.atlasapi.remotesite.rovi.indexing.KeyedFileIndex;
import org.atlasapi.remotesite.rovi.indexing.KeyedFileIndexer;
import org.atlasapi.remotesite.rovi.model.RoviEpisodeSequenceLine;
import org.atlasapi.remotesite.rovi.model.RoviProgramDescriptionLine;
import org.atlasapi.remotesite.rovi.parsers.RoviProgramLineParser;
import org.atlasapi.remotesite.rovi.parsers.RoviSeasonHistoryLineParser;
import org.atlasapi.remotesite.rovi.populators.ContentPopulatorSupplier;
import org.atlasapi.remotesite.rovi.processing.restartable.IngestFileProcessingStep;
import org.atlasapi.remotesite.rovi.processing.restartable.IngestProcessingChain;
import org.atlasapi.remotesite.rovi.processing.restartable.IngestProcessingStep;
import org.atlasapi.remotesite.rovi.processing.restartable.IngestStatus;
import org.atlasapi.remotesite.rovi.processing.restartable.IngestStatusStore;
import org.atlasapi.remotesite.rovi.processing.restartable.IngestStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

public class RoviFullIngestProcessor implements RoviIngestProcessor {
    private final static Logger LOG = LoggerFactory.getLogger(RoviFullIngestProcessor.class);
    private static final int MAX_CACHE_SIZE = 100000;

    private final KeyedFileIndexer<String, RoviProgramDescriptionLine> programDescriptionIndexer;
    private final KeyedFileIndexer<String, RoviEpisodeSequenceLine> episodeSequenceIndexer;
    private final RoviContentWriter contentWriter;
    private final ContentResolver contentResolver;
    private final ScheduleFileProcessor scheduleFileProcessor;
    private final AuxiliaryCacheSupplier auxCacheSupplier;
    private final IngestStatusStore ingestStatusStore;
    
    public RoviFullIngestProcessor(
            KeyedFileIndexer<String, RoviProgramDescriptionLine> programDescriptionIndexer,
            KeyedFileIndexer<String, RoviEpisodeSequenceLine> episodeSequenceIndexer,
            RoviContentWriter contentWriter,
            ContentResolver contentResolver,
            ScheduleFileProcessor scheduleFileProcessor,
            AuxiliaryCacheSupplier auxCacheSupplier,
            IngestStatusStore ingestStatusStore) {

        this.programDescriptionIndexer = checkNotNull(programDescriptionIndexer);
        this.episodeSequenceIndexer = checkNotNull(episodeSequenceIndexer);
        this.contentWriter = checkNotNull(contentWriter);
        this.contentResolver = checkNotNull(contentResolver);
        this.scheduleFileProcessor = checkNotNull(scheduleFileProcessor);
        this.auxCacheSupplier = checkNotNull(auxCacheSupplier);
        this.ingestStatusStore = checkNotNull(ingestStatusStore);
    }

    @Override
    public void process(File programFile, File seasonsFile, File scheduleFile, File programDescriptionsFile, File episodeSequenceFile) throws IOException {
        KeyedFileIndex<String, RoviProgramDescriptionLine> descriptionIndex = null;
        KeyedFileIndex<String, RoviEpisodeSequenceLine> episodeSequenceIndex = null;

        try {
            LOG.info("Indexing files");
            descriptionIndex = programDescriptionIndexer.index(programDescriptionsFile);
            episodeSequenceIndex = episodeSequenceIndexer.index(episodeSequenceFile);
            LOG.info("Indexing completed");
            
            LOG.info("Start processing programs");

            Optional<IngestStatus> maybeRecoveredStatus = ingestStatusStore.getIngestStatus();

            IngestProcessingChain ingestChain = IngestProcessingChain
                    .withFirstStep(brandsWithoutParentIngestStep(programFile,
                            descriptionIndex,
                            episodeSequenceIndex))
                    .andThen(brandsWithParentIngestStep(programFile, descriptionIndex, episodeSequenceIndex))
                    .andThen(seriesIngestStep(seasonsFile))
                    .andThen(itemWithoutParentIngestStep(programFile, descriptionIndex, episodeSequenceIndex))
                    .andFinally(itemWithParentIngestStep(programFile, descriptionIndex, episodeSequenceIndex));

            ingestChain.execute(maybeRecoveredStatus);

            /**
             * Broadcasts processing is not restartable at the moment because its different nature
             * compared to the other steps. It's quicker than other steps though, so not a massive problem for now
             */
            ingestStatusStore.persistIngestStatus(new IngestStatus(IngestStep.BROADCASTS, 0));

            // TODO: schedule processor should be consistent with the other steps
            processBroadcasts(scheduleFile);

            ingestStatusStore.markAsCompleted();
            
            LOG.info("Processing programs complete");
            
        } finally {
            releaseIndexResources(descriptionIndex, episodeSequenceIndex);
        }
    }

    private void processBroadcasts(File scheduleFile) throws IOException {
        scheduleFileProcessor.process(scheduleFile);
        
        LOG.info("Processing schedule complete");
    }

    private IngestProcessingStep brandsWithoutParentIngestStep(File programFile,
            KeyedFileIndex<String, RoviProgramDescriptionLine> descriptionIndex,
            KeyedFileIndex<String, RoviEpisodeSequenceLine> episodeSequenceIndex) {

        RoviProgramLineIngestor ingestor = new RoviProgramLineIngestor (
                new RoviProgramLineParser(),
                FILE_CHARSET,
                IS_BRAND_NO_PARENT,
                contentWriter,
                contentResolver,
                contentPopulator(descriptionIndex, episodeSequenceIndex)
        );

        return IngestFileProcessingStep.forStep(IngestStep.BRANDS_NO_PARENT)
                .withFile(programFile)
                .withCharset(FILE_CHARSET)
                .withProcessor(ingestor)
                .withStatusPersistor(ingestStatusStore)
                .build();
    }

    private IngestProcessingStep brandsWithParentIngestStep(File programFile,
            KeyedFileIndex<String, RoviProgramDescriptionLine> descriptionIndex,
            KeyedFileIndex<String, RoviEpisodeSequenceLine> episodeSequenceIndex) {

        RoviProgramLineIngestor ingestor = new RoviProgramLineIngestor(
                new RoviProgramLineParser(),
                FILE_CHARSET,
                IS_BRAND_WITH_PARENT,
                contentWriter,
                contentResolver,
                contentPopulator(descriptionIndex, episodeSequenceIndex)
        );

        return IngestFileProcessingStep.forStep(IngestStep.BRANDS_WITH_PARENT)
                .withFile(programFile)
                .withCharset(FILE_CHARSET)
                .withProcessor(ingestor)
                .withStatusPersistor(ingestStatusStore)
                .build();
    }

    private IngestProcessingStep seriesIngestStep(File seasonsFile) {
        RoviSeasonLineIngestor ingestor = new RoviSeasonLineIngestor(
                new RoviSeasonHistoryLineParser(),
                FILE_CHARSET,
                contentResolver,
                contentWriter,
                auxCacheSupplier.parentPublisherCache(MAX_CACHE_SIZE));

        return IngestFileProcessingStep.forStep(IngestStep.SERIES)
                .withFile(seasonsFile)
                .withCharset(FILE_CHARSET)
                .withProcessor(ingestor)
                .withStatusPersistor(ingestStatusStore)
                .build();
    }

    private IngestProcessingStep itemWithoutParentIngestStep(File programFile,
            KeyedFileIndex<String, RoviProgramDescriptionLine> descriptionIndex,
            KeyedFileIndex<String, RoviEpisodeSequenceLine> episodeSequenceIndex) {

        RoviProgramLineIngestor ingestor = new RoviProgramLineIngestor(
                new RoviProgramLineParser(),
                FILE_CHARSET,
                NO_BRAND_NO_PARENT,
                contentWriter,
                contentResolver,
                contentPopulator(descriptionIndex, episodeSequenceIndex)
        );

        return IngestFileProcessingStep.forStep(IngestStep.ITEMS_NO_PARENT)
                .withFile(programFile)
                .withCharset(FILE_CHARSET)
                .withProcessor(ingestor)
                .withStatusPersistor(ingestStatusStore)
                .build();

    }

    private IngestProcessingStep itemWithParentIngestStep(File programFile,
            KeyedFileIndex<String, RoviProgramDescriptionLine> descriptionIndex,
            KeyedFileIndex<String, RoviEpisodeSequenceLine> episodeSequenceIndex) {

        RoviProgramLineIngestor ingestor = new RoviProgramLineIngestor(
                new RoviProgramLineParser(),
                FILE_CHARSET,
                NO_BRAND_WITH_PARENT,
                contentWriter,
                contentResolver,
                contentPopulator(descriptionIndex, episodeSequenceIndex)
        );

        return IngestFileProcessingStep.forStep(IngestStep.ITEMS_WITH_PARENT)
                .withFile(programFile)
                .withCharset(FILE_CHARSET)
                .withProcessor(ingestor)
                .withStatusPersistor(ingestStatusStore)
                .build();
    }

    private ContentPopulatorSupplier contentPopulator(
            KeyedFileIndex<String, RoviProgramDescriptionLine> descriptionIndex,
            KeyedFileIndex<String, RoviEpisodeSequenceLine> episodeSequenceIndex) {
        
        return new ContentPopulatorSupplier(
                descriptionIndex,
                episodeSequenceIndex,
                contentResolver,
                auxCacheSupplier.seasonNumberCache(MAX_CACHE_SIZE));
    }

    private void releaseIndexResources(KeyedFileIndex<?, ?>... indexes) {
        for (KeyedFileIndex<?, ?> index: indexes) {
            if (index != null) {
                index.releaseResources();
            }
        }
    }

}
