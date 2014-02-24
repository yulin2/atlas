package org.atlasapi.remotesite.rovi;

import java.nio.charset.Charset;

import org.atlasapi.media.entity.Content;
import org.atlasapi.remotesite.rovi.series.RoviSeasonHistoryLine;
import org.atlasapi.remotesite.rovi.series.RoviSeasonHistoryLineParser;
import org.atlasapi.remotesite.rovi.series.SeriesFromSeasonHistoryExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RoviSeasonLineProcessor extends RoviLineExtractorAndWriter<RoviSeasonHistoryLine> {

    private static final Logger LOG = LoggerFactory.getLogger(RoviProgramLineProcessor.class);

    private final RoviSeasonHistoryLineParser lineParser;
    private final SeriesFromSeasonHistoryExtractor extractor;

    public RoviSeasonLineProcessor(RoviSeasonHistoryLineParser lineParser, SeriesFromSeasonHistoryExtractor extractor, RoviContentWriter contentWriter, Charset charset) {
        super(lineParser, charset, contentWriter);
        this.lineParser = lineParser;
        this.extractor = extractor;
    }
    
    @Override
    protected Logger log() {
        return LOG;
    }

    @Override
    protected Content extractContent(RoviSeasonHistoryLine parsedLine) {
        return extractor.extract(parsedLine);
    }

    @Override
    protected boolean isToProcess(RoviSeasonHistoryLine parsedLine) {
        return true;
    }

    @Override
    protected RoviSeasonHistoryLine parse(String line) {
        return lineParser.apply(line);
    }

    @Override
    protected void handleProcessingException(Exception e, String line) {
        // Swallow the exception and logs
        log().error(errorMessage(line), e);
    }

}
