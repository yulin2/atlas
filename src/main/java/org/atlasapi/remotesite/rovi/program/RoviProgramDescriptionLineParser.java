package org.atlasapi.remotesite.rovi.program;

import static org.atlasapi.remotesite.rovi.RoviUtils.getActionTypeAtPosition;
import static org.atlasapi.remotesite.rovi.RoviUtils.getPartAtPosition;

import org.atlasapi.remotesite.rovi.RoviConstants;
import org.atlasapi.remotesite.rovi.RoviLineParser;


public class RoviProgramDescriptionLineParser implements RoviLineParser<RoviProgramDescriptionLine> {

    private static final int PROGRAM_ID_POS = 0;
    private static final int SOURCE_ID_POS = 1;
    private static final int DESCRIPTION_CULTURE_POS = 2;
    private static final int DESCRIPTION_TYPE_POS = 3;
    private static final int DESCRIPTION_POS = 4;
    private static final int ACTION_TYPE_POS = 6;
    
    @Override
    public RoviProgramDescriptionLine apply(String line) {
        Iterable<String> parts = RoviConstants.LINE_SPLITTER.split(line);
        RoviProgramDescriptionLine.Builder builder = RoviProgramDescriptionLine.builder();
        
        builder.withProgramId(getPartAtPosition(parts, PROGRAM_ID_POS));
        builder.withSourceId(getPartAtPosition(parts, SOURCE_ID_POS));
        builder.withDescriptionCulture(getPartAtPosition(parts, DESCRIPTION_CULTURE_POS));
        builder.withDescriptionType(getPartAtPosition(parts, DESCRIPTION_TYPE_POS));
        builder.withDescription(getPartAtPosition(parts, DESCRIPTION_POS));
        builder.withActionType(getActionTypeAtPosition(parts, ACTION_TYPE_POS));
        
        return builder.build();
    }

}
