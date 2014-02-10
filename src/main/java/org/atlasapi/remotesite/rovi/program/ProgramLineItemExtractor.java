package org.atlasapi.remotesite.rovi.program;

import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.remotesite.rovi.KeyedFileIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Extracts an {@link Item} from a {@link RoviProgramLine} with {@link RoviShowType} OT (Other Program)
 */
public class ProgramLineItemExtractor extends ProgramLineBaseExtractor<RoviProgramLine, Item> {

    public ProgramLineItemExtractor(
            KeyedFileIndex<String, RoviProgramDescriptionLine> descriptionIndex,
            ContentResolver contentResolver) {
        super(descriptionIndex, contentResolver);
    }

    private final static Logger LOG = LoggerFactory.getLogger(ProgramLineItemExtractor.class);
    
    @Override
    protected Item createContent() {
        return new Item();
    }

    @Override
    protected Item addSpecificData(Item content, RoviProgramLine programLine) {
        // At the moment returns the item directly. It might be needed to add more fields in the future
        return content;
    }
    
    @Override
    protected Logger log() {
        return LOG;
    }

}
