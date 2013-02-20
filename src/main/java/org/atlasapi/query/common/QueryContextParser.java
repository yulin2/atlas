package org.atlasapi.query.common;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.servlet.http.HttpServletRequest;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.output.Annotation;

import com.metabroadcast.common.query.Selection;
import com.metabroadcast.common.query.Selection.SelectionBuilder;

public class QueryContextParser {
    
    private final ApplicationConfigurationFetcher configFetcher;
    private final QueryParameterAnnotationsExtractor annotationExtractor;
    private final SelectionBuilder selectionBuilder;

    public QueryContextParser(ApplicationConfigurationFetcher configFetcher, QueryParameterAnnotationsExtractor annotationsParser, Selection.SelectionBuilder selectionBuilder) {
        this.configFetcher = checkNotNull(configFetcher);
        this.annotationExtractor = checkNotNull(annotationsParser);
        this.selectionBuilder = checkNotNull(selectionBuilder);
    }

    public QueryContext parseContext(HttpServletRequest request) {
        return new QueryContext(
            configFetcher.configurationFor(request).valueOrDefault(ApplicationConfiguration.DEFAULT_CONFIGURATION),
            annotationExtractor.extractFromRequest(request).or(Annotation.defaultAnnotations()),
            selectionBuilder.build(request)
        );
    }
    
}
