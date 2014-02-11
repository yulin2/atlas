package org.atlasapi.remotesite.rovi.program;

import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.remotesite.rovi.KeyedFileIndex;

/*
 * Extracts an {@link Item} from a {@link RoviProgramLine} with {@link RoviShowType} OT (Other Program)
 */
public class ProgramLineItemExtractor extends ProgramLineBaseItemExtractor<Item>{

    public ProgramLineItemExtractor(
            KeyedFileIndex<String, RoviProgramDescriptionLine> descriptionIndex,
            ContentResolver contentResolver) {
        super(descriptionIndex, contentResolver);
    }

    @Override
    protected void addItemSpecificData(Item content, RoviProgramLine programLine) {
        // Do nothing
    }

    @Override
    protected Item createContent() {
        return new Item();
    }

}
