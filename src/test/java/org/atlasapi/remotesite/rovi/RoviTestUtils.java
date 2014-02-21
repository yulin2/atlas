package org.atlasapi.remotesite.rovi;

import static org.atlasapi.remotesite.rovi.RoviUtils.canonicalUriForProgram;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.atlasapi.media.entity.Content;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.persistence.content.ResolvedContent.ResolvedContentBuilder;
import org.atlasapi.remotesite.rovi.program.RoviProgramDescriptionLine;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;


public class RoviTestUtils {
    
    protected static final String SHORT_DESCRIPTION = "This is the short description";
    protected static final String MEDIUM_DESCRIPTION = "This is the medium description";
    protected static final String LONG_DESCRIPTION = "This is the long description";
    private static final String ENGLISH_UK_CULTURE = "English - UK";

    public static Collection<RoviProgramDescriptionLine> descriptions(String programId) {
        List<RoviProgramDescriptionLine> descriptions = Lists.newArrayList();
        
        RoviProgramDescriptionLine.Builder builderGenericDesc = RoviProgramDescriptionLine.builder();
        builderGenericDesc.withProgramId(programId);
        builderGenericDesc.withDescription(SHORT_DESCRIPTION);
        builderGenericDesc.withDescriptionCulture(ENGLISH_UK_CULTURE);
        builderGenericDesc.withDescriptionType("Generic Description");
        builderGenericDesc.withActionType(ActionType.INSERT);
        descriptions.add(builderGenericDesc.build());

        RoviProgramDescriptionLine.Builder builderPlotSynopsis = RoviProgramDescriptionLine.builder();
        builderPlotSynopsis.withProgramId(programId);
        builderPlotSynopsis.withDescription(MEDIUM_DESCRIPTION);
        builderPlotSynopsis.withDescriptionCulture(ENGLISH_UK_CULTURE);
        builderPlotSynopsis.withDescriptionType("Plot Synopsis");
        builderPlotSynopsis.withActionType(ActionType.INSERT);
        descriptions.add(builderPlotSynopsis.build());
        
        RoviProgramDescriptionLine.Builder builderSynopsis = RoviProgramDescriptionLine.builder();
        builderSynopsis.withProgramId(programId);
        builderSynopsis.withDescription(LONG_DESCRIPTION);
        builderSynopsis.withDescriptionCulture(ENGLISH_UK_CULTURE);
        builderSynopsis.withDescriptionType("Synopsis");
        builderSynopsis.withActionType(ActionType.INSERT);
        descriptions.add(builderSynopsis.build());        
        
        return descriptions;
    }
    
    public static File fileFromResource(String resourcePath) {
        URL fileUrl = Resources.getResource(resourcePath);
        return new File(fileUrl.getPath());
    }
    
    public static <T extends Content>  ResolvedContent resolvedContent(String id, T content) {
        Map<String, T> map = Maps.newHashMap();
        map.put(canonicalUriForProgram(id), content);

        ResolvedContentBuilder resolvedContentBuilder = ResolvedContent.builder();
        resolvedContentBuilder.putAll(map);
        
        ResolvedContent resolvedContent = resolvedContentBuilder.build();
        return resolvedContent;
    }
    
}
