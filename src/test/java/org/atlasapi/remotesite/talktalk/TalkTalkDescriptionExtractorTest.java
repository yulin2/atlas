package org.atlasapi.remotesite.talktalk;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import org.atlasapi.media.entity.Item;
import org.atlasapi.remotesite.talktalk.vod.bindings.SynopsisListType;
import org.atlasapi.remotesite.talktalk.vod.bindings.SynopsisType;
import org.junit.Test;


public class TalkTalkDescriptionExtractorTest {
    
    private final TalkTalkDescriptionExtractor extractor = new TalkTalkDescriptionExtractor();
    
    @Test
    public void testExtractingDescriptions() {
        
        SynopsisListType synopsisList = new SynopsisListType();
        synopsisList.getSynopsis().add(synopsis("description","LNGSYNPS"));
        synopsisList.getSynopsis().add(synopsis("short-desc","TERSE"));
        synopsisList.getSynopsis().add(synopsis("med-desc","3LNSYNPS"));
        
        Item item = extractor.extractDescriptions(new Item(), synopsisList);
        
        assertThat(item.getDescription(),is("description"));
        assertThat(item.getShortDescription(),is("short-desc"));
        assertThat(item.getMediumDescription(),is("med-desc"));
        assertThat(item.getLongDescription(),is("description"));
        
    }
    
    private SynopsisType synopsis(String text, String type) {
        SynopsisType synopsis = new SynopsisType();
        synopsis.setText(text);
        synopsis.setType(type);
        return synopsis;
    }
    
}
