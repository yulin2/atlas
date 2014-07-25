package org.atlasapi.remotesite.btvod;

import org.atlasapi.media.entity.Content;
import org.atlasapi.remotesite.btvod.BtVodData.BtVodDataRow;


public interface BtVodContentListener {

    void onContent(Content content, BtVodDataRow vodData);
    
}
