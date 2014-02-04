package org.atlasapi.remotesite.rovi.program;

import org.atlasapi.media.entity.Content;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.rovi.RoviShowType;


public class ProgramLineContentExtractorFactory {
    
    public static ContentExtractor<RoviProgramLine, ? extends Content> getContentExtractor(RoviShowType programType) {
        
        switch(programType) {
            case MO:
                return new ProgramLineFilmExtractor();
            case SE:
                return new ProgramLineEpisodeExtractor();
            case SM:
                return new ProgramLineBrandExtractor();
            case OT:
                return new ProgramLineItemExtractor();
            default:
                throw new RuntimeException("Program type not supported");
        }
        
    }

}
