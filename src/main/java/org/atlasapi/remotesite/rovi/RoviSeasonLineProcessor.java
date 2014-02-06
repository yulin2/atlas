package org.atlasapi.remotesite.rovi;

import org.atlasapi.media.entity.Content;
import org.atlasapi.remotesite.rovi.series.RoviSeasonHistoryLine;
import org.atlasapi.remotesite.rovi.series.RoviSeasonHistoryLineParser;
import org.atlasapi.remotesite.rovi.series.SeriesFromSeasonHistoryExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RoviSeasonLineProcessor extends RoviLineProcessor<RoviSeasonHistoryLine> {

    private static final Logger LOG = LoggerFactory.getLogger(RoviProgramLineProcessor.class);

    private final RoviSeasonHistoryLineParser lineParser;
    private final SeriesFromSeasonHistoryExtractor extractor;

    public RoviSeasonLineProcessor(RoviSeasonHistoryLineParser lineParser, SeriesFromSeasonHistoryExtractor extractor, RoviContentWriter contentWriter) {
        super(contentWriter);
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
        return lineParser.parseLine(line);
    }

}
