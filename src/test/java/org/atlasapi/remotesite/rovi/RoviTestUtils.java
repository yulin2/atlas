package org.atlasapi.remotesite.rovi;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.atlasapi.media.entity.Content;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.persistence.content.ResolvedContent.ResolvedContentBuilder;
import org.atlasapi.remotesite.rovi.model.ActionType;
import org.atlasapi.remotesite.rovi.model.RoviCulture;
import org.atlasapi.remotesite.rovi.model.RoviProgramDescriptionLine;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;


public class RoviTestUtils {
    
    public static final String SHORT_DESCRIPTION = "This is the short description";
    public static final String MEDIUM_DESCRIPTION = "This is the medium description";
    public static final String LONG_DESCRIPTION = "This is the long description";
    public static final String DESCRIPTION_CULTURE = "English - UK";
    public static final Locale DESCRIPTION_LOCALE = RoviCulture.localeFromCulture(DESCRIPTION_CULTURE);

    public static Collection<RoviProgramDescriptionLine> descriptions(String programId) {
        List<RoviProgramDescriptionLine> descriptions = Lists.newArrayList();
        
        RoviProgramDescriptionLine.Builder builderGenericDesc = RoviProgramDescriptionLine.builder();
        builderGenericDesc.withProgramId(programId);
        builderGenericDesc.withDescription(SHORT_DESCRIPTION);
        builderGenericDesc.withDescriptionCulture(DESCRIPTION_CULTURE);
        builderGenericDesc.withDescriptionType("Generic Description");
        builderGenericDesc.withActionType(ActionType.INSERT);
        descriptions.add(builderGenericDesc.build());

        RoviProgramDescriptionLine.Builder builderPlotSynopsis = RoviProgramDescriptionLine.builder();
        builderPlotSynopsis.withProgramId(programId);
        builderPlotSynopsis.withDescription(MEDIUM_DESCRIPTION);
        builderPlotSynopsis.withDescriptionCulture(DESCRIPTION_CULTURE);
        builderPlotSynopsis.withDescriptionType("Plot Synopsis");
        builderPlotSynopsis.withActionType(ActionType.INSERT);
        descriptions.add(builderPlotSynopsis.build());
        
        RoviProgramDescriptionLine.Builder builderSynopsis = RoviProgramDescriptionLine.builder();
        builderSynopsis.withProgramId(programId);
        builderSynopsis.withDescription(LONG_DESCRIPTION);
        builderSynopsis.withDescriptionCulture(DESCRIPTION_CULTURE);
        builderSynopsis.withDescriptionType("Synopsis");
        builderSynopsis.withActionType(ActionType.INSERT);
        descriptions.add(builderSynopsis.build());        
        
        return descriptions;
    }
    
    public static File fileFromResource(String resourcePath) {
        URL fileUrl = Resources.getResource(resourcePath);
        return new File(fileUrl.getPath());
    }
    
    public static <T extends Content>  ResolvedContent resolvedContent(T content) {
        Map<String, T> map = Maps.newHashMap();
        map.put(content.getCanonicalUri(), content);

        ResolvedContentBuilder resolvedContentBuilder = ResolvedContent.builder();
        resolvedContentBuilder.putAll(map);
        
        ResolvedContent resolvedContent = resolvedContentBuilder.build();
        return resolvedContent;
    }
    
    public static ResolvedContent unresolvedContent() {
        ResolvedContentBuilder resolvedContentBuilder = ResolvedContent.builder();
        ResolvedContent resolvedContent = resolvedContentBuilder.build();
        return resolvedContent;
    }
    
}
