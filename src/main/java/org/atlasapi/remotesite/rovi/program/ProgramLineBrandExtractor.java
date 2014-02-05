package org.atlasapi.remotesite.rovi.program;

import java.io.IOException;
import java.util.Collection;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.remotesite.rovi.KeyedFileIndex;
import org.atlasapi.remotesite.rovi.series.RoviSeriesLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ProgramLineBrandExtractor extends ProgramLineBaseExtractor<RoviProgramLine, Brand> {

    private final static Logger LOG = LoggerFactory.getLogger(ProgramLineBrandExtractor.class);
    
    private final KeyedFileIndex<String, RoviSeriesLine> seriesIndex;
    
    public ProgramLineBrandExtractor(KeyedFileIndex<String, RoviSeriesLine> seriesIndex) {
        this.seriesIndex = seriesIndex;
    }
    
    @Override
    protected Brand createContent() {
        return new Brand();
    }

    @Override
    protected Brand addSpecificData(Brand content, RoviProgramLine programLine) {
        Collection<RoviSeriesLine> seriesLines;
        try {
            seriesLines = seriesIndex.getLinesForKey(programLine.getKey());
            if (seriesLines.size() > 0) {
                RoviSeriesLine seriesLine = seriesLines.iterator().next();
                content.setDescription(seriesLine.getSynopsis());
            }
        } catch (IOException e) {
            LOG.error("Error while retrieving descriptions for brand {} from index", programLine.getKey(), e);
        }

        LOG.trace("Extracted brand {} with description {}", content.getCanonicalUri(), content.getDescription());
        return content;
    }

}
