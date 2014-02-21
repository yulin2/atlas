package org.atlasapi.remotesite.rovi.program;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.remotesite.rovi.IndexAccessException;
import org.atlasapi.remotesite.rovi.KeyedFileIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Extracts a {@link Brand} from a {@link RoviProgramLine} with {@link RoviShowType} SM (Series Master) 
 */
public class ProgramLineBrandExtractor extends ProgramLineBaseExtractor<RoviProgramLine, Brand> {

    private final static Logger LOG = LoggerFactory.getLogger(ProgramLineBrandExtractor.class);
    
    public ProgramLineBrandExtractor(
            KeyedFileIndex<String, RoviProgramDescriptionLine> descriptionIndex,
            ContentResolver contentResolver) {
        super(descriptionIndex, contentResolver);
    }
    
    @Override
    protected Brand createContent() {
        return new Brand();
    }

    @Override
    protected Brand addSpecificData(Brand content, RoviProgramLine programLine) throws IndexAccessException {
        return content;
    }
    
    @Override
    protected Logger log() {
        return LOG;
    }

}
