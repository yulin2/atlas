package org.atlasapi.remotesite.talktalk;

import org.atlasapi.remotesite.talktalk.vod.bindings.ItemDetailType;
import org.atlasapi.remotesite.talktalk.vod.bindings.ItemTypeType;
import org.atlasapi.remotesite.talktalk.vod.bindings.VODEntityType;


public final class TalkTalkUriCompiler {

    private static final String BRAND_URI_PATTERN = "http://talktalk.net/brands/%s";
    private static final String SERIES_URI_PATTERN = "http://talktalk.net/series/%s";
    private static final String EPISODE_URI_PATTERN = "http://talktalk.net/episodes/%s";
    
    public String uriFor(ItemDetailType detail) {
        return String.format(patternFor(detail.getItemType()), detail.getId());
    }

    public String uriFor(VODEntityType entity) {
        return String.format(patternFor(entity.getItemType()), entity.getId());
    }

    private String patternFor(ItemTypeType itemTypeType) {
        String pattern = null;
        switch (itemTypeType){
        case BRAND:
            pattern = BRAND_URI_PATTERN;
            break;
        case BROADCAST_ITEM:
            throw unsupportedItemType(itemTypeType);
        case CHANNEL:
            throw unsupportedItemType(itemTypeType);
        case EPISODE:
            pattern = EPISODE_URI_PATTERN;
            break;
        case IMGWALL:
            throw unsupportedItemType(itemTypeType);
        case LOCATION:
            throw unsupportedItemType(itemTypeType);
        case SERIES:
            pattern = SERIES_URI_PATTERN;
            break;
        default:
            throw unsupportedItemType(itemTypeType);
        }
        return pattern;
    }

    private IllegalArgumentException unsupportedItemType(ItemTypeType type) {
        return new IllegalArgumentException(type.toString());
    }
    
}
