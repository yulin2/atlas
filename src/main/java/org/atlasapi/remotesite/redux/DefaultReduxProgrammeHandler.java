package org.atlasapi.remotesite.redux;

import static com.google.common.base.Preconditions.checkNotNull;

import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.redux.model.FullReduxProgramme;

public class DefaultReduxProgrammeHandler implements ReduxProgrammeHandler {
    
    private final ContentWriter writer;
    private final ContentExtractor<FullReduxProgramme, Item> contentExtractor;

    public DefaultReduxProgrammeHandler(ContentWriter writer, ContentExtractor<FullReduxProgramme, Item> contentExtractor) {
        this.writer = writer;
        this.contentExtractor = contentExtractor;
    }
    
    @Override
    public void handle(FullReduxProgramme programme) {
        Item item = contentExtractor.extract(checkNotNull(programme));
        
        if(item != null) {
            writer.createOrUpdate(item);
        }
    }

}
