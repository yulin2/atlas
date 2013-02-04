package org.atlasapi.query.v4.topic;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.content.criteria.AtomicQuerySet;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.topic.Topic;
import org.atlasapi.output.Annotation;
import org.atlasapi.query.common.AnnotationsExtractor;
import org.atlasapi.query.common.Query;
import org.atlasapi.query.common.QueryAttributeParser;
import org.atlasapi.query.common.QueryContext;
import org.atlasapi.query.common.QueryParameterAnnotationsExtractor;
import org.atlasapi.query.common.QueryParser;

import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.query.Selection.SelectionBuilder;

public class TopicQueryParser implements QueryParser<Topic> {

    private final NumberToShortStringCodec idCodec;
    private final QueryAttributeParser attributeParser;
    private final SelectionBuilder selectionBuilder;
    private final ApplicationConfigurationFetcher configFetcher;
    private final AnnotationsExtractor annotationExtractor;

    private final Pattern singleResourcePattern;

    public TopicQueryParser(String resourceName, QueryAttributeParser attributeParser,
                            NumberToShortStringCodec idCodec,
                            ApplicationConfigurationFetcher configFetcher,
                            SelectionBuilder selectionBuilder, 
                            QueryParameterAnnotationsExtractor annotationsParser) {
        this.attributeParser = checkNotNull(attributeParser);
        this.configFetcher = checkNotNull(configFetcher);
        this.idCodec = checkNotNull(idCodec);
        this.selectionBuilder = checkNotNull(selectionBuilder);
        this.annotationExtractor = checkNotNull(annotationsParser);
        this.singleResourcePattern = Pattern.compile(checkNotNull(resourceName) + "/([^.]+)(.*)?$");
    }

    @Override
    public Query<Topic> parse(HttpServletRequest request) {
        Id singleId = tryExtractSingleId(request);
        return singleId != null ? singleQuery(request, singleId) 
                                : listQuery(request);
    }
    
    private Query<Topic> singleQuery(HttpServletRequest request, Id singleId) {
        return Query.singleQuery(singleId, 
            new QueryContext(appConfig(request), annotations(request)));
    }
    

    private Query<Topic> listQuery(HttpServletRequest request) {
        return Query.listQuery(operands(request), 
            new QueryContext(appConfig(request), annotations(request), selectionBuilder.build(request)));
    }
    private AtomicQuerySet operands(HttpServletRequest request) {
        return attributeParser.parse(request);
    }

    private ApplicationConfiguration appConfig(HttpServletRequest request) {
        return configFetcher.configurationFor(request)
            .valueOrDefault(ApplicationConfiguration.DEFAULT_CONFIGURATION);
    }

    private Set<Annotation> annotations(HttpServletRequest request) {
        return annotationExtractor.extractFromRequest(request)
            .or(ImmutableSet.<Annotation> of());
    }

    private Id tryExtractSingleId(HttpServletRequest request) {
        Matcher matcher = singleResourcePattern.matcher(request.getRequestURI());
        return matcher.find() ? Id.valueOf(idCodec.decode(matcher.group(1)))
                              : null;
    }

}
