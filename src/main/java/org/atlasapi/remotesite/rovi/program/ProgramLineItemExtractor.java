package org.atlasapi.remotesite.rovi.program;

import org.atlasapi.media.entity.Item;
import org.atlasapi.remotesite.rovi.KeyedFileIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ProgramLineItemExtractor extends ProgramLineBaseExtractor<RoviProgramLine, Item> {

    protected ProgramLineItemExtractor(
            KeyedFileIndex<String, RoviProgramDescriptionLine> descriptionIndex) {
        super(descriptionIndex);
    }

    private final static Logger LOG = LoggerFactory.getLogger(ProgramLineItemExtractor.class);
    
    @Override
    protected Item createContent() {
        return new Item();
    }

    @Override
    protected Item addSpecificData(Item content, RoviProgramLine programLine) {
        return content;
    }

}
