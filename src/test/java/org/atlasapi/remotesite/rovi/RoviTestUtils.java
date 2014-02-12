package org.atlasapi.remotesite.rovi;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import org.atlasapi.remotesite.rovi.program.RoviProgramDescriptionLine;

import com.google.common.collect.Lists;
import com.google.common.io.Resources;


public class RoviTestUtils {
    
    private static final String SHORT_DESCRIPTION = "This is the short description";
    private static final String MEDIUM_DESCRIPTION = "This is the medium description";
    private static final String LONG_DESCRIPTION = "This is the long description";
    private static final String ENGLISH_UK_CULTURE = "English - UK";

    public static Collection<RoviProgramDescriptionLine> descriptions(String programId) {
        List<RoviProgramDescriptionLine> descriptions = Lists.newArrayList();
        
        RoviProgramDescriptionLine.Builder builderGenericDesc = RoviProgramDescriptionLine.builder();
        builderGenericDesc.withProgramId(programId);
        builderGenericDesc.withDescription(SHORT_DESCRIPTION);
        builderGenericDesc.withDescriptionCulture(ENGLISH_UK_CULTURE);
        builderGenericDesc.withDescriptionType("Generic Description");
        descriptions.add(builderGenericDesc.build());

        RoviProgramDescriptionLine.Builder builderPlotSynopsis = RoviProgramDescriptionLine.builder();
        builderPlotSynopsis.withProgramId(programId);
        builderPlotSynopsis.withDescription(MEDIUM_DESCRIPTION);
        builderPlotSynopsis.withDescriptionCulture(ENGLISH_UK_CULTURE);
        builderPlotSynopsis.withDescriptionType("Plot Synopsis");
        descriptions.add(builderPlotSynopsis.build());
        
        RoviProgramDescriptionLine.Builder builderSynopsis = RoviProgramDescriptionLine.builder();
        builderSynopsis.withProgramId(programId);
        builderSynopsis.withDescription(LONG_DESCRIPTION);
        builderSynopsis.withDescriptionCulture(ENGLISH_UK_CULTURE);
        builderSynopsis.withDescriptionType("Synopsis");
        descriptions.add(builderSynopsis.build());        
        
        return descriptions;
    }
    
    public static File fileFromResource(String resourcePath) {
        URL fileUrl = Resources.getResource(resourcePath);
        return new File(fileUrl.getPath());
    }
    
}
