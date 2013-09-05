package org.atlasapi.query.common;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.servlet.http.HttpServletRequest;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.output.JsonResponseWriter;
import org.atlasapi.query.annotation.AnnotationsExtractor;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.query.Selection;
import com.metabroadcast.common.query.Selection.SelectionBuilder;

public class QueryContextParser implements ParameterNameProvider {
    
    private final ApplicationConfigurationFetcher configFetcher;
    private final AnnotationsExtractor annotationExtractor;
    private final SelectionBuilder selectionBuilder;

    public QueryContextParser(ApplicationConfigurationFetcher configFetcher, AnnotationsExtractor annotationsParser, Selection.SelectionBuilder selectionBuilder) {
        this.configFetcher = checkNotNull(configFetcher);
        this.annotationExtractor = checkNotNull(annotationsParser);
        this.selectionBuilder = checkNotNull(selectionBuilder);
    }
    
    public QueryContext parseSingleContext(HttpServletRequest request) throws QueryParseException {
        return new QueryContext(
                configFetcher.configurationFor(request).valueOrDefault(ApplicationConfiguration.defaultConfiguration()),
                annotationExtractor.extractFromSingleRequest(request),
                selectionBuilder.build(request)
                );
    }

    public QueryContext parseListContext(HttpServletRequest request) throws QueryParseException {
        return new QueryContext(
            configFetcher.configurationFor(request).valueOrDefault(ApplicationConfiguration.defaultConfiguration()),
            annotationExtractor.extractFromListRequest(request),
            selectionBuilder.build(request)
        );
    }

    @Override
    public ImmutableSet<String> getParameterNames() {
        return ImmutableSet.copyOf(Iterables.concat(
            configFetcher.getParameterNames(), 
            annotationExtractor.getParameterNames(), 
            selectionBuilder.getParameterNames(),
            ImmutableSet.of(JsonResponseWriter.CALLBACK)));
    }
    
}