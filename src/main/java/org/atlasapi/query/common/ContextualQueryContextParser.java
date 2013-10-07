package org.atlasapi.query.common;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.servlet.http.HttpServletRequest;

import org.atlasapi.application.ApplicationSources;
import org.atlasapi.application.query.ApplicationSourcesFetcher;
import org.atlasapi.output.JsonResponseWriter;
import org.atlasapi.query.annotation.ContextualAnnotationsExtractor;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.query.Selection;
import com.metabroadcast.common.query.Selection.SelectionBuilder;

public class ContextualQueryContextParser implements ParameterNameProvider {
    
    private final ApplicationSourcesFetcher configFetcher;
    private final ContextualAnnotationsExtractor annotationExtractor;
    private final SelectionBuilder selectionBuilder;

    public ContextualQueryContextParser(ApplicationSourcesFetcher configFetcher, ContextualAnnotationsExtractor annotationsParser, Selection.SelectionBuilder selectionBuilder) {
        this.configFetcher = checkNotNull(configFetcher);
        this.annotationExtractor = checkNotNull(annotationsParser);
        this.selectionBuilder = checkNotNull(selectionBuilder);
    }

    public QueryContext parseContext(HttpServletRequest request) throws QueryParseException {
        return new QueryContext(
            configFetcher.sourcesFor(request).or(ApplicationSources.EMPTY_SOURCES),
            annotationExtractor.extractFromRequest(request),
            selectionBuilder.build(request)
        );
    }

    public ImmutableSet<String> getParameterNames() {
        return ImmutableSet.copyOf(Iterables.concat(
            configFetcher.getParameterNames(), 
            annotationExtractor.getParameterNames(), 
            selectionBuilder.getParameterNames(),
            ImmutableSet.of(JsonResponseWriter.CALLBACK)));
    }
    
}
