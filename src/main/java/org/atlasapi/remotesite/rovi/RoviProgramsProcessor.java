package org.atlasapi.remotesite.rovi;

import static org.atlasapi.remotesite.rovi.RoviConstants.FILE_CHARSET;
import static org.atlasapi.remotesite.rovi.RoviPredicates.IS_BRAND_NO_PARENT;
import static org.atlasapi.remotesite.rovi.RoviPredicates.IS_BRAND_WITH_PARENT;
import static org.atlasapi.remotesite.rovi.RoviPredicates.NO_BRAND_NO_PARENT;
import static org.atlasapi.remotesite.rovi.RoviPredicates.NO_BRAND_WITH_PARENT;

import java.io.File;
import java.io.IOException;

import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.remotesite.rovi.program.ProgramLineContentExtractorSupplier;
import org.atlasapi.remotesite.rovi.program.RoviProgramDescriptionLine;
import org.atlasapi.remotesite.rovi.program.RoviProgramLineParser;
import org.atlasapi.remotesite.rovi.schedule.ScheduleFileProcessor;
import org.atlasapi.remotesite.rovi.series.RoviEpisodeSequenceLine;
import org.atlasapi.remotesite.rovi.series.RoviSeasonHistoryLine;
import org.atlasapi.remotesite.rovi.series.RoviSeasonHistoryLineParser;
import org.atlasapi.remotesite.rovi.series.RoviSeriesLine;
import org.atlasapi.remotesite.rovi.series.SeriesFromSeasonHistoryExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;

public class RoviProgramsProcessor {

    private final static Logger LOG = LoggerFactory.getLogger(RoviProgramsProcessor.class);

    private final KeyedFileIndexer<String, RoviProgramDescriptionLine> programDescriptionIndexer;
    private final KeyedFileIndexer<String, RoviEpisodeSequenceLine> episodeSequenceIndexer;
    private final KeyedFileIndexer<String, RoviSeriesLine> seriesIndexer;
    private final KeyedFileIndexer<String, RoviSeasonHistoryLine> seasonHistoryIndexer;
    private final RoviContentWriter contentWriter;
    private final ContentResolver contentResolver;
    private final ScheduleFileProcessor scheduleFileProcessor;
    
    public RoviProgramsProcessor(
            KeyedFileIndexer<String, RoviProgramDescriptionLine> programDescriptionIndexer,
            KeyedFileIndexer<String, RoviEpisodeSequenceLine> episodeSequenceIndexer,
            KeyedFileIndexer<String, RoviSeriesLine> seriesIndexer,
            KeyedFileIndexer<String, RoviSeasonHistoryLine> seasonHistoryIndexer,
            RoviContentWriter contentWriter,
            ContentResolver contentResolver,
            ScheduleFileProcessor scheduleFileProcessor) {

        this.programDescriptionIndexer = programDescriptionIndexer;
        this.episodeSequenceIndexer = episodeSequenceIndexer;
        this.seriesIndexer = seriesIndexer;
        this.seasonHistoryIndexer = seasonHistoryIndexer;
        this.contentWriter = contentWriter;
        this.contentResolver = contentResolver;
        this.scheduleFileProcessor = scheduleFileProcessor;
    }

    public void process(File programFile, File seasonsFile, File scheduleFile) throws IOException {
        KeyedFileIndex<String, RoviProgramDescriptionLine> descriptionIndex = null;
        KeyedFileIndex<String, RoviEpisodeSequenceLine> episodeSequenceIndex = null;
        KeyedFileIndex<String, RoviSeriesLine> seriesIndex = null;
        KeyedFileIndex<String, RoviSeasonHistoryLine> seasonHistoryIndex = null;

        try {
            LOG.info("Indexing files");
            descriptionIndex = programDescriptionIndexer.index();
            episodeSequenceIndex = episodeSequenceIndexer.index();
            seriesIndex = seriesIndexer.index();
            seasonHistoryIndex = seasonHistoryIndexer.index();
            LOG.info("Indexing completed");
            
            ProgramLineContentExtractorSupplier contentExtractorSupplier = new ProgramLineContentExtractorSupplier(
                    descriptionIndex,
                    seriesIndex,
                    episodeSequenceIndex,
                    seasonHistoryIndex,
                    contentResolver);
    
            LOG.info("Start processing programs");
            
            // Step 1. Process brands with no parent
            RoviDataProcessingResult processingBrandsNoParentResult = Files.readLines(programFile, FILE_CHARSET, new RoviProgramLineProcessor(
                    new RoviProgramLineParser(),
                    contentExtractorSupplier,
                    IS_BRAND_NO_PARENT,
                    contentWriter,
                    FILE_CHARSET));
            
            LOG.info("Processing brands with no parent complete, result: {}", processingBrandsNoParentResult);
    
            // Step 2. Process brands with  parent
            RoviDataProcessingResult processingBrandsWithParentResult = Files.readLines(programFile, FILE_CHARSET, new RoviProgramLineProcessor(
                    new RoviProgramLineParser(),
                    contentExtractorSupplier,
                    IS_BRAND_WITH_PARENT,
                    contentWriter,
                    FILE_CHARSET));
            
            LOG.info("Processing brands with parent complete, result: {}", processingBrandsWithParentResult);
            
            // Step 3. Process series
            SeriesFromSeasonHistoryExtractor seriesExtractor = new SeriesFromSeasonHistoryExtractor(contentResolver);
            RoviDataProcessingResult processingSeriesResult = Files.readLines(seasonsFile, FILE_CHARSET, new RoviSeasonLineProcessor(
                    new RoviSeasonHistoryLineParser(),
                    seriesExtractor,
                    contentWriter,
                    FILE_CHARSET));
            
            seriesExtractor.clearCache();
            
            LOG.info("Processing series complete, result: {}", processingSeriesResult);
            
            // Step 4. Process programs without parent
            RoviDataProcessingResult processingNoBrandsNoParentResult = Files.readLines(programFile, FILE_CHARSET, new RoviProgramLineProcessor(
                    new RoviProgramLineParser(),
                    contentExtractorSupplier,
                    NO_BRAND_NO_PARENT,
                    contentWriter,
                    FILE_CHARSET));
            
            LOG.info("Processing programs (no brands) with no parent complete, result: {}", processingNoBrandsNoParentResult);
            
            // Step 5. Process programs with parent
            RoviDataProcessingResult processingNoBrandsWithParentResult = Files.readLines(programFile, FILE_CHARSET, new RoviProgramLineProcessor(
                    new RoviProgramLineParser(),
                    contentExtractorSupplier,
                    NO_BRAND_WITH_PARENT,
                    contentWriter,
                    FILE_CHARSET));
            
            LOG.info("Processing programs (no brands) with parent complete, result: {}", processingNoBrandsWithParentResult);
            
            // Step 6. Process schedule
            scheduleFileProcessor.process(scheduleFile);
      
            LOG.info("Processing schedule complete");
        } finally {
            releaseIndexResources(ImmutableSet.of(descriptionIndex,
                    episodeSequenceIndex,
                    seriesIndex,
                    seasonHistoryIndex));
        }
    }

    private void releaseIndexResources(Iterable<KeyedFileIndex<String, ? extends KeyedLine<String>>> indexes) {
        for (KeyedFileIndex<?, ?> index: indexes) {
            if (index != null) {
                index.releaseResources();
            }
        }
    }

}
