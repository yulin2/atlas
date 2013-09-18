package org.atlasapi.remotesite.talktalk;

import org.atlasapi.media.entity.Described;
import org.atlasapi.remotesite.talktalk.vod.bindings.ItemDetailType;
import org.atlasapi.remotesite.talktalk.vod.bindings.SynopsisListType;
import org.atlasapi.remotesite.talktalk.vod.bindings.SynopsisType;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

/**
 * Extracts synopses from TalkTalk {@link ItemDetailType} according to <a
 * href="http://docs.metabroadcast.com/display/mbst/TalkTalk+VOD">http://docs.
 * metabroadcast.com/display/mbst/TalkTalk+VOD</a> and sets them on relevant
 * fields of a {@link Described}.
 * 
 */
public class TalkTalkDescriptionExtractor {
    
    private static final String SHORT_DESCRIPTION_SYNOPSIS_TYPE = "TERSE";
    private static final String MEDIUM_DESCRIPTION_SYNOPSIS_TYPE = "3LNSYNPS";
    private static final String LONG_DESCRIPTION_SYNOPSIS_TYPE = "LNGSYNPS";
    
    public <D extends Described> D extractDescriptions(D described, SynopsisListType synopses) {
        described.setShortDescription(extractSynopsis(synopses, SHORT_DESCRIPTION_SYNOPSIS_TYPE));
        described.setMediumDescription(extractSynopsis(synopses, MEDIUM_DESCRIPTION_SYNOPSIS_TYPE));
        described.setLongDescription(extractSynopsis(synopses, LONG_DESCRIPTION_SYNOPSIS_TYPE));
        described.setDescription(longestDescription(described));
        return described;
    }

    private <D extends Described> String longestDescription(D d) {
        return Strings.emptyToNull(Objects.firstNonNull(d.getLongDescription(),
                Objects.firstNonNull(d.getMediumDescription(),
                        Objects.firstNonNull(d.getShortDescription(), ""))));
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
