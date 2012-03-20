package org.atlasapi.remotesite.bbc.ion;

import org.atlasapi.media.content.Item;

public interface BbcItemFetcherClient {

    Item createItem(String episodeId);

}
