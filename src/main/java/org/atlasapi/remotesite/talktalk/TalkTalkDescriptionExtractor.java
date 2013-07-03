package org.atlasapi.remotesite.talktalk;

import org.atlasapi.media.entity.Described;
import org.atlasapi.remotesite.talktalk.vod.bindings.ItemDetailType;
import org.atlasapi.remotesite.talktalk.vod.bindings.SynopsisListType;
import org.atlasapi.remotesite.talktalk.vod.bindings.SynopsisType;

/**
 * Extracts synopses from TalkTalk {@link ItemDetailType} according to <a
 * href="http://docs.metabroadcast.com/display/mbst/TalkTalk+VOD">http://docs.
 * metabroadcast.com/display/mbst/TalkTalk+VOD</a> and sets them on relevant
 * fields of a {@link Described}.
 * 
 */
public class TalkTalkDescriptionExtractor {
    
    private static final String DESCRIPTION_SYNOPSIS_TYPE = "LNGSYNPS";
    private static final String SHORT_DESCRIPTION_SYNOPSIS_TYPE = "TERSE";
    private static final String MEDIUM_DESCRIPTION_SYNOPSIS_TYPE = "3LNSYNPS";
    private static final String LONG_DESCRIPTION_SYNOPSIS_TYPE = DESCRIPTION_SYNOPSIS_TYPE;
    
    public <D extends Described> D extractDescriptions(D described, SynopsisListType synopses) {
        described.setDescription(extractSynopsis(synopses, DESCRIPTION_SYNOPSIS_TYPE));
        described.setShortDescription(extractSynopsis(synopses, SHORT_DESCRIPTION_SYNOPSIS_TYPE));
        described.setMediumDescription(extractSynopsis(synopses, MEDIUM_DESCRIPTION_SYNOPSIS_TYPE));
        described.setLongDescription(extractSynopsis(synopses, LONG_DESCRIPTION_SYNOPSIS_TYPE));
        return described;
    }
 
    private String extractSynopsis(SynopsisListType synopses, String type) {
        if (synopses == null) {
            return null;
        }
        for(SynopsisType synopsis : synopses.getSynopsis()){
            if (type.equals(synopsis.getType())) {
                return synopsis.getText();
            }
        }
        return null;
    }
    
}
