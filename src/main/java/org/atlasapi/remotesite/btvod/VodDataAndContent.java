package org.atlasapi.remotesite.btvod;

import static com.google.common.base.Preconditions.checkNotNull;

import org.atlasapi.media.entity.Content;
import org.atlasapi.remotesite.btvod.BtVodData.BtVodDataRow;


public class VodDataAndContent {

    private BtVodDataRow btVodDataRow;
    private Content content;

    public VodDataAndContent(BtVodDataRow btVodDataRow, Content content) {
        this.btVodDataRow = checkNotNull(btVodDataRow);
        this.content = checkNotNull(content);
    }
    
    public BtVodDataRow getBtVodDataRow() {
        return btVodDataRow;
    }
    
    public Content getContent() {
        return content;
    }
}
