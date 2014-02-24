package org.atlasapi.remotesite.rovi.program;

import java.util.Collection;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.remotesite.rovi.IndexAccessException;
import org.atlasapi.remotesite.rovi.KeyedFileIndex;
import org.atlasapi.remotesite.rovi.series.RoviSeriesLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;

/*
 * Extracts a {@link Brand} from a {@link RoviProgramLine} with {@link RoviShowType} SM (Series Master) 
 */
public class ProgramLineBrandExtractor extends ProgramLineBaseExtractor<RoviProgramLine, Brand> {

    private final static Logger LOG = LoggerFactory.getLogger(ProgramLineBrandExtractor.class);
    
    private final KeyedFileIndex<String, RoviSeriesLine> seriesIndex;
    
    public ProgramLineBrandExtractor(
            KeyedFileIndex<String, RoviProgramDescriptionLine> descriptionIndex,
            KeyedFileIndex<String, RoviSeriesLine> seriesIndex,
            ContentResolver contentResolver) {
        super(descriptionIndex, contentResolver);
        this.seriesIndex = seriesIndex;
    }
    
    @Override
    protected Brand createContent() {
        return new Brand();
    }

    @Override
    protected Brand addSpecificData(Brand content, RoviProgramLine programLine) throws IndexAccessException {
        Collection<RoviSeriesLine> seriesLines;
        seriesLines = seriesIndex.getLinesForKey(programLine.getKey());
        RoviSeriesLine firstSeriesLine = Iterables.getFirst(seriesLines, null);

        if (firstSeriesLine != null && firstSeriesLine.getSynopsis().isPresent()) {
            content.setDescription(firstSeriesLine.getSynopsis().get());
        }

        return content;
    }
    
    @Override
    protected Logger log() {
        return LOG;
    }

}
