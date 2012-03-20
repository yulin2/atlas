package org.atlasapi.remotesite.bbc.ion;

import org.atlasapi.media.content.Item;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.bbc.ion.model.IonEpisode;

public class BbcIonEpisodeItemContentExtractor extends BaseBbcIonEpisodeItemExtractor implements ContentExtractor<IonEpisode, Item> {

    public BbcIonEpisodeItemContentExtractor(AdapterLog log) {
        super(log);
    }

    @Override
    public Item extract(IonEpisode source) {
        return super.extract(source);
    }
}
