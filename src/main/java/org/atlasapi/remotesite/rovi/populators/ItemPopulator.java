package org.atlasapi.remotesite.rovi.populators;

import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.remotesite.rovi.model.RoviProgramDescriptionLine;
import org.atlasapi.remotesite.rovi.model.RoviProgramLine;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;


public class ItemPopulator<T extends Item> extends BaseContentPopulator<T>{

    public ItemPopulator(Optional<RoviProgramLine> program,
            Iterable<RoviProgramDescriptionLine> descriptions, ContentResolver contentResolver) {
        super(program, descriptions, contentResolver);
    }

    @Override
    protected void addSpecificData(T item) {
        if (optionalProgram.isPresent()) {
            createVersionIfNeeded(item, optionalProgram.get());
        }
        
        addItemSpecificData(item);
    }
    
    protected void addItemSpecificData(T item) {
        // Do nothing
    }
    
    private void createVersionIfNeeded(T item, RoviProgramLine program) {
        Version version = new Version();

        if (program.getDuration().isPresent()) {
            version.setDuration(program.getDuration().get());
        }

        item.setVersions(Sets.newHashSet(version));
    }

}
