package org.atlasapi.remotesite.rovi.program;

import org.atlasapi.media.entity.Content;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.rovi.RoviUtils;



public abstract class ProgramLineBaseExtractor<SOURCE, CONTENT extends Content> implements ContentExtractor<RoviProgramLine, CONTENT> {
    
    @Override
    public CONTENT extract(RoviProgramLine roviLine) {
        CONTENT content = createContent();
        
        content.setTitle(roviLine.getLongTitle());
        content.setCanonicalUri(RoviUtils.canonicalUriFor(roviLine.getProgramId()));
        
        return addSpecificData(content, roviLine);
    }
    
    protected abstract CONTENT createContent();
    protected abstract CONTENT addSpecificData(CONTENT content, RoviProgramLine roviLine);
}
