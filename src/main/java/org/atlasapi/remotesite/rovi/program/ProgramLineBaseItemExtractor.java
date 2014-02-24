package org.atlasapi.remotesite.rovi.program;

import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.remotesite.rovi.IndexAccessException;
import org.atlasapi.remotesite.rovi.KeyedFileIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Base Extractor that sets common fields on a {@link Item} from a {@link RoviProgramLine}
 */
public abstract class ProgramLineBaseItemExtractor<T extends Item> extends ProgramLineBaseExtractor<RoviProgramLine, T> {

    public ProgramLineBaseItemExtractor(
            KeyedFileIndex<String, RoviProgramDescriptionLine> descriptionIndex,
            ContentResolver contentResolver) {
        super(descriptionIndex, contentResolver);
    }

    private final static Logger LOG = LoggerFactory.getLogger(ProgramLineBaseItemExtractor.class);
    
    @Override
    protected T addSpecificData(T content, RoviProgramLine programLine) throws IndexAccessException {
        addItemSpecificData(content, programLine);
        return content;
    }
    
    protected abstract void addItemSpecificData(T content, RoviProgramLine programLine) throws IndexAccessException;
    
    @Override
    protected Logger log() {
        return LOG;
    }

}
