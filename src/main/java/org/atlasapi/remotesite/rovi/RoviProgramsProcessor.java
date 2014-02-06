package org.atlasapi.remotesite.rovi;

import static org.atlasapi.remotesite.rovi.RoviConstants.FILE_CHARSET;
import static org.atlasapi.remotesite.rovi.RoviPredicates.IS_BRAND_NO_PARENT;

import java.io.File;
import java.io.IOException;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Content;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.rovi.program.ProgramLineContentExtractorSupplier;
import org.atlasapi.remotesite.rovi.program.RoviProgramDescriptionLine;
import org.atlasapi.remotesite.rovi.program.RoviProgramLine;
import org.atlasapi.remotesite.rovi.program.RoviProgramLineParser;
import org.atlasapi.remotesite.rovi.program.RoviReleaseDatesLine;
import org.atlasapi.remotesite.rovi.series.RoviEpisodeSequenceLine;
import org.atlasapi.remotesite.rovi.series.RoviSeasonHistoryLine;
import org.atlasapi.remotesite.rovi.series.RoviSeriesLine;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Predicate;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;

public class RoviProgramsProcessor {

    private final static Logger LOG = LoggerFactory.getLogger(RoviProgramsProcessor.class);

    private final KeyedFileIndexer<String, RoviProgramDescriptionLine> programDescriptionIndexer;
    private final KeyedFileIndexer<String, RoviReleaseDatesLine> releaseDatesIndexer;
    private final KeyedFileIndexer<String, RoviEpisodeSequenceLine> episodeSequenceIndexer;
    private final KeyedFileIndexer<String, RoviSeasonHistoryLine> seasonHistoryIndexer;
    private final KeyedFileIndexer<String, RoviSeriesLine> seriesIndexer;
    private final ContentWriter contentWriter;

    public RoviProgramsProcessor(
            KeyedFileIndexer<String, RoviProgramDescriptionLine> programDescriptionIndexer,
            KeyedFileIndexer<String, RoviReleaseDatesLine> releaseDatesIndexer,
            KeyedFileIndexer<String, RoviEpisodeSequenceLine> episodeSequenceIndexer,
            KeyedFileIndexer<String, RoviSeasonHistoryLine> seasonHistoryIndexer,
            KeyedFileIndexer<String, RoviSeriesLine> seriesIndexer,
            ContentWriter contentWriter) {

        this.programDescriptionIndexer = programDescriptionIndexer;
        this.releaseDatesIndexer = releaseDatesIndexer;
        this.episodeSequenceIndexer = episodeSequenceIndexer;
        this.seasonHistoryIndexer = seasonHistoryIndexer;
        this.seriesIndexer = seriesIndexer;
        this.contentWriter = contentWriter;
    }

    public void process(File programFile) throws IOException {
        LOG.info("Indexing files");
        KeyedFileIndex<String, RoviProgramDescriptionLine> descriptionIndex = programDescriptionIndexer.index();
        KeyedFileIndex<String, RoviReleaseDatesLine> releaseDatesIndex = releaseDatesIndexer.index();
        KeyedFileIndex<String, RoviEpisodeSequenceLine> episodeSequenceIndex = episodeSequenceIndexer.index();
        KeyedFileIndex<String, RoviSeasonHistoryLine> seasonHistoryIndex = seasonHistoryIndexer.index();
        KeyedFileIndex<String, RoviSeriesLine> seriesIndex = seriesIndexer.index();
        LOG.info("Indexing completed");

        ProgramLineContentExtractorSupplier contentExtractorSupplier = new ProgramLineContentExtractorSupplier(
                descriptionIndex,
                seriesIndex);

        LOG.info("Start processing programs");
        
        RoviDataProcessingResult processingResult = Files.readLines(programFile, FILE_CHARSET, new ProgramLineProcessor(
                new RoviProgramLineParser(),
                contentExtractorSupplier,
                IS_BRAND_NO_PARENT,
                contentWriter));
        
        LOG.info("Processing programs complete, result: {}", processingResult);
    }

    private class ProgramLineProcessor implements LineProcessor<RoviDataProcessingResult> {

        private long scannedLines = 0;
        private long processedLines = 0;
        private long failedLines = 0;

        private final RoviProgramLineParser programLineParser;
        private final ProgramLineContentExtractorSupplier contentExtractorSupplier;
        private final Predicate<RoviProgramLine> isToProcess;
        private final ContentWriter contentWriter;
        private DateTime startTime;
        
        public ProgramLineProcessor(RoviProgramLineParser programLineParser,
                ProgramLineContentExtractorSupplier contentExtractorSupplier, Predicate<RoviProgramLine> isToProcess, ContentWriter contentWriter) {
            this.programLineParser = programLineParser;
            this.contentExtractorSupplier = contentExtractorSupplier;
            this.isToProcess = isToProcess;
            this.contentWriter = contentWriter;
        }

        @Override
        public boolean processLine(String line) throws IOException {
            // Removing BOM if charset is UTF-16LE see (http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4508058)
            if (scannedLines == 0) {
                startTime = now();
            }
            
            try {
                RoviProgramLine programLine = programLineParser.parseLine(removeBom(line));
                if (isToProcess.apply(programLine)) {
                    ContentExtractor<RoviProgramLine, ? extends Content> contentExtractor = contentExtractorSupplier.getContentExtractor(programLine.getShowType());
                    Content content = contentExtractor.extract(programLine);
                    writeContent(content);
                    processedLines++;
                }
            } catch (Exception e) {
                LOG.error("Error occurred while processing the line [" + line + "]", e);
                failedLines++;
            } finally {
                scannedLines++;
            }

            return true;
        }

        private void writeContent(Content content) {
            if (content instanceof Brand) {
                contentWriter.createOrUpdate((Brand) content);
            }
        }

        private String removeBom(String line) {
            if (scannedLines == 0 && FILE_CHARSET.equals(Charsets.UTF_16LE)) {
                line = line.substring(1);
            }
            return line;
        }

        @Override
        public RoviDataProcessingResult getResult() {
            return new RoviDataProcessingResult(processedLines, failedLines, startTime, now());
        }
        
        private DateTime now() {
            return DateTime.now(DateTimeZone.UTC);
        }

    }
}
