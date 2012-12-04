package org.atlasapi.remotesite.btvod;

import org.atlasapi.media.entity.Content;
import org.atlasapi.remotesite.btvod.model.BtVodItemData;

public interface BtVodContentCreator<T extends Content> {

    T extract(BtVodItemData data);
}
