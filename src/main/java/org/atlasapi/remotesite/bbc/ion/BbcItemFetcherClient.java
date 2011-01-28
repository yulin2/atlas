package org.atlasapi.remotesite.bbc.ion;

import org.atlasapi.media.entity.Item;

public interface BbcItemFetcherClient {

    Item createItem(String episodeId);

}
