package org.atlasapi.remotesite.rovi.processing;

import static com.google.common.base.Predicates.and;
import static org.atlasapi.remotesite.rovi.RoviConstants.FILE_CHARSET;
import static org.atlasapi.remotesite.rovi.RoviPredicates.IS_BRAND_NO_PARENT;
import static org.atlasapi.remotesite.rovi.RoviPredicates.IS_BRAND_WITH_PARENT;
import static org.atlasapi.remotesite.rovi.RoviPredicates.IS_INSERT;
import static org.atlasapi.remotesite.rovi.RoviPredicates.NO_BRAND_NO_PARENT;
import static org.atlasapi.remotesite.rovi.RoviPredicates.NO_BRAND_WITH_PARENT;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.remotesite.rovi.RoviContentWriter;
import org.atlasapi.remotesite.rovi.RoviPredicates;
import org.atlasapi.remotesite.rovi.indexing.KeyedActionLine;
import org.atlasapi.remotesite.rovi.indexing.KeyedFileIndex;
import org.atlasapi.remotesite.rovi.indexing.KeyedFileIndexer;
import org.atlasapi.remotesite.rovi.model.RoviEpisodeSequenceLine;
import org.atlasapi.remotesite.rovi.model.RoviProgramDescriptionLine;
import org.atlasapi.remotesite.rovi.model.RoviProgramLine;
import org.atlasapi.remotesite.rovi.parsers.RoviProgramLineParser;
import org.atlasapi.remotesite.rovi.parsers.RoviSeasonHistoryLineParser;
import org.atlasapi.remotesite.rovi.populators.ContentPopulatorSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.google.common.io.Files;


public class RoviDeltaIngestProcessor implements RoviIngestProcessor {
    
    private static final int MAX_CACHE_SIZE = 100000;

    private final static Logger LOG = LoggerFactory.getLogger(RoviDeltaIngestProcessor.class);

    private final KeyedFileIndexer<String, RoviProgramLine> programIndexer;
    private final KeyedFileIndexer<String, RoviProgramDescriptionLine> programDescriptionIndexer;
    private final KeyedFileIndexer<String, RoviEpisodeSequenceLine> episodeSequenceIndexer;
    private final RoviContentWriter contentWriter;
    private final ContentResolver contentResolver;
    // TODO: Implement a DeltaScheduleFileProcessor
    private final ScheduleFileProcessor scheduleFileProcessor;
    private final AuxiliaryCacheSupplier auxCacheSupplier;
    
    public RoviDeltaIngestProcessor(
            KeyedFileIndexer<String, RoviProgramLine> programIndexer,
            KeyedFileIndexer<String, RoviProgramDescriptionLine> programDescriptionIndexer,
            KeyedFileIndexer<String, RoviEpisodeSequenceLine> episodeSequenceIndexer,
            RoviContentWriter contentWriter,
            ContentResolver contentResolver,
            ScheduleFileProcessor scheduleFileProcessor,
            AuxiliaryCacheSupplier auxCacheSupplier) {

        this.programIndexer = programIndexer;
        this.programDescriptionIndexer = programDescriptionIndexer;
        this.episodeSequenceIndexer = episodeSequenceIndexer;
        this.contentWriter = contentWriter;
        this.contentResolver = contentResolver;
        this.scheduleFileProcessor = scheduleFileProcessor;
        this.auxCacheSupplier = auxCacheSupplier;
    }

    @Override
    public void process(File programFile, File seasonsFile, File scheduleFile, File programDescriptionsFile, File episodeSequenceFile) throws IOException {
        KeyedFileIndex<String, RoviProgramLine> programIndex = null;
        KeyedFileIndex<String, RoviProgramDescriptionLine> descriptionIndex = null;
        KeyedFileIndex<String, RoviEpisodeSequenceLine> episodeSequenceIndex = null;

        try {
            LOG.info("Indexing files");
            programIndex = programIndexer.index(programFile);
            descriptionIndex = programDescriptionIndexer.index(programDescriptionsFile);
            episodeSequenceIndex = episodeSequenceIndexer.index(episodeSequenceFile);
            LOG.info("Indexing completed");
            
            ContentPopulatorSupplier contentPopulator = new ContentPopulatorSupplier(
                    descriptionIndex,
                    episodeSequenceIndex,
                    contentResolver,
                    auxCacheSupplier.seasonNumberCache(MAX_CACHE_SIZE));
            
            LOG.info("Start processing programs");
            
            // Step 1. Process brands with no parent
            RoviDataProcessingResult processingBrandsNoParentResult = Files.readLines(programFile, FILE_CHARSET, new RoviProgramLineIngestor(
                    new RoviProgramLineParser(),
                    FILE_CHARSET,
                    descriptionIndex,
                    episodeSequenceIndex,
                    and(IS_INSERT, IS_BRAND_NO_PARENT),
                    contentWriter,
                    contentResolver,
                    contentPopulator
                    ));
            
            LOG.info("Processing brands with no parent complete, result: {}", processingBrandsNoParentResult);
    
            // Step 2. Process brands with  parent
            RoviDataProcessingResult processingBrandsWithParentResult = Files.readLines(programFile, FILE_CHARSET, new RoviProgramLineIngestor(
                    new RoviProgramLineParser(),
                    FILE_CHARSET,
                    descriptionIndex,
                    episodeSequenceIndex,
                    and(IS_INSERT, IS_BRAND_WITH_PARENT),
                    contentWriter,
                    contentResolver,
                    contentPopulator
                    ));
            
            LOG.info("Processing brands with parent complete, result: {}", processingBrandsWithParentResult);
            
            // Step 3. Process series
            RoviDataProcessingResult processingSeriesResult = Files.readLines(seasonsFile, FILE_CHARSET, new RoviSeasonLineIngestor(
                    new RoviSeasonHistoryLineParser(),
                    FILE_CHARSET,
                    contentResolver,
                    contentWriter,
                    auxCacheSupplier.parentPublisherCache(MAX_CACHE_SIZE)));
            
            LOG.info("Processing series complete, result: {}", processingSeriesResult);
            
            // Step 4. Process programs without parent
            RoviDataProcessingResult processingNoBrandsNoParentResult = Files.readLines(programFile, FILE_CHARSET, new RoviProgramLineIngestor(
                    new RoviProgramLineParser(),
                    FILE_CHARSET,
                    descriptionIndex,
                    episodeSequenceIndex,
                    and(IS_INSERT, NO_BRAND_NO_PARENT),
                    contentWriter,
                    contentResolver,
                    contentPopulator
                    ));
            
            LOG.info("Processing programs (no brands) with no parent complete, result: {}", processingNoBrandsNoParentResult);
            
            // Step 5. Process programs with parent
            RoviDataProcessingResult processingNoBrandsWithParentResult = Files.readLines(programFile, FILE_CHARSET, new RoviProgramLineIngestor(
                    new RoviProgramLineParser(),
                    FILE_CHARSET,
                    descriptionIndex,
                    episodeSequenceIndex,
                    and(IS_INSERT, NO_BRAND_WITH_PARENT),
                    contentWriter,
                    contentResolver,
                    contentPopulator
                    ));
            
            LOG.info("Processing programs (no brands) with parent complete, result: {}", processingNoBrandsWithParentResult);
            
            // Step 6. Process updates and deletes of all the types
            RoviDataProcessingResult processingUpdatesAndDeletesResult = Files.readLines(programFile, FILE_CHARSET, new RoviProgramLineIngestor(
                    new RoviProgramLineParser(),
                    FILE_CHARSET,
                    descriptionIndex,
                    episodeSequenceIndex,
                    Predicates.not(RoviPredicates.IS_INSERT),
                    contentWriter,
                    contentResolver,
                    contentPopulator
                    ));
            
            LOG.info("Processing programs (no brands) with parent complete, result: {}", processingUpdatesAndDeletesResult);
            
            // Step 7. Process only auxiliary actions
            Set<String> programsWithOnlyAuxiliaryActions = programsWithOnlyAuxiliaryActions(programIndex,
                    descriptionIndex,
                    episodeSequenceIndex);
            
            ContentPopulatorSupplier populator = new ContentPopulatorSupplier(
                    descriptionIndex,
                    episodeSequenceIndex,
                    contentResolver,
                    auxCacheSupplier.seasonNumberCache(100000));
            
            AuxiliaryActionsProcessor auxiliaryActionsProcessor = new AuxiliaryActionsProcessor(
                    populator,
                    contentResolver,
                    contentWriter);
            
            auxiliaryActionsProcessor.process(programsWithOnlyAuxiliaryActions);

            LOG.info("Processing auxiliary actions only, result: {}", processingNoBrandsWithParentResult);
            
            // Step 8. Process schedule
            scheduleFileProcessor.process(scheduleFile);
      
            LOG.info("Processing schedule complete");
        } finally {
            releaseIndexResources(ImmutableSet.of(programIndex, descriptionIndex, episodeSequenceIndex));
        }
    }
    

    private Set<String> programsWithOnlyAuxiliaryActions(
            KeyedFileIndex<String, RoviProgramLine> programIndex,
            KeyedFileIndex<String, RoviProgramDescriptionLine> descriptionIndex,
            KeyedFileIndex<String, RoviEpisodeSequenceLine> episodeSequenceIndex) {
        
        Set<String> programs = programIndex.getKeys();
        Set<String> programsWithDescriptionActions = descriptionIndex.getKeys();
        Set<String> programsWithEpisodeSequenceActions = episodeSequenceIndex.getKeys();
        
        SetView<String> programsWithAuxiliaryActions = Sets.union(programsWithDescriptionActions,
                programsWithEpisodeSequenceActions);
        
        return Sets.difference(programsWithAuxiliaryActions, programs);
    }

    private void releaseIndexResources(Iterable<KeyedFileIndex<String, ? extends KeyedActionLine<String>>> indexes) {
        for (KeyedFileIndex<?, ?> index: indexes) {
            if (index != null) {
                index.releaseResources();
            }
        }
    }

}
