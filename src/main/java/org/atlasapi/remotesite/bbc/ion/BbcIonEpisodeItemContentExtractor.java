package org.atlasapi.remotesite.bbc.ion;

import org.atlasapi.media.content.ContentResolver;
import org.atlasapi.media.entity.Item;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.bbc.ion.model.IonEpisode;

public class BbcIonEpisodeItemContentExtractor extends BaseBbcIonEpisodeItemExtractor implements ContentExtractor<IonEpisode, Item> {

    public BbcIonEpisodeItemContentExtractor(ContentResolver contentResolver) {
        super(null, contentResolver);
    }

    @Override
    public Item extract(IonEpisode source) {
        return super.extract(source);
    }
}
