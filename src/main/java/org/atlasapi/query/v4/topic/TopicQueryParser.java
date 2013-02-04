package org.atlasapi.query.v4.topic;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.content.criteria.AtomicQuery;
import org.atlasapi.content.criteria.IdAttributeQuery;
import org.atlasapi.content.criteria.attribute.Attribute;
import org.atlasapi.content.criteria.attribute.Attributes;
import org.atlasapi.content.criteria.operator.Operator;
import org.atlasapi.content.criteria.operator.Operators;
import org.atlasapi.media.common.Id;
import org.atlasapi.output.Annotation;
import org.atlasapi.query.v2.QueryParameterAnnotationsExtractor;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Iterators;
import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.query.Selection.SelectionBuilder;

public class TopicQueryParser implements QueryParser<TopicQuery> {
    
    private final String resourceName = "topics";
    private final Optional<String> context = Optional.of("topic");

    private final Splitter operandSplitter = Splitter.on('.').omitEmptyStrings().trimResults();
    private final Splitter valueSplitter = Splitter.on(',').omitEmptyStrings().trimResults();
    
    private final NumberToShortStringCodec idCodec;
    private final ApplicationConfigurationFetcher configFetcher;
    private final SelectionBuilder selectionBuilder;
    
    private final Map<String, Attribute<?>> attributes;
    private final Map<Attribute<?>, AttributeCoercer<String,?>> coercers;
    private final QueryParameterAnnotationsExtractor annotationExtractor;
    private final Pattern singleResourcePattern;

    public TopicQueryParser(Map<Attribute<?>, AttributeCoercer<String,?>> attributes,
            NumberToShortStringCodec idCodec, 
            ApplicationConfigurationFetcher configFetcher, 
            SelectionBuilder selectionBuilder) {
        this.configFetcher = checkNotNull(configFetcher);
        this.idCodec = checkNotNull(idCodec);
        this.selectionBuilder = checkNotNull(selectionBuilder);
        this.annotationExtractor = new QueryParameterAnnotationsExtractor();
        this.singleResourcePattern = Pattern.compile(resourceName+"/([^.]+)(.*)?$");
        this.attributes = Maps.uniqueIndex(attributes.keySet(), new Function<Attribute<?>, String>(){
            @Override
            public String apply(Attribute<?> input) {
                return input.externalName();
        }});
        this.coercers = attributes;
    }
    
    @Override
    public TopicQuery queryFrom(HttpServletRequest request) {
        Id singleId = tryExtractSingleId(request);
        return new TopicQuery(
            singleId != null ? singleIdQuery(singleId, request) : parseListQuery(request),
            selectionBuilder.build(request),
            appConfig(request),
            annotations(request),
            singleId == null
        );
    }

    private Iterable<? extends AtomicQuery> singleIdQuery(Id singleId, HttpServletRequest request) {
        return ImmutableSet.of(
            new IdAttributeQuery(Attributes.ID, Operators.EQUALS, ImmutableSet.of(singleId))
        );
    }
    
    private ApplicationConfiguration appConfig(HttpServletRequest request) {
        return configFetcher.configurationFor(request)
            .valueOrDefault(ApplicationConfiguration.DEFAULT_CONFIGURATION);
    }

    private  Set<Annotation> annotations(HttpServletRequest request) {
        return annotationExtractor.extractFromRequest(request, context)
           .or(ImmutableSet.<Annotation>of());
    }

    private Id tryExtractSingleId(HttpServletRequest request) {
        Matcher matcher = singleResourcePattern.matcher(request.getRequestURI());
        return matcher.find() ? Id.valueOf(idCodec.decode(matcher.group(1)))
                              : null;
    }
    
    private Iterable<? extends AtomicQuery> parseListQuery(HttpServletRequest request) {
        Builder<AtomicQuery> operands = ImmutableSet.builder();
        for(Entry<String, String[]> param : getParameterMap(request).entrySet()) {
            Iterator<String> paramParts = operandSplitter.split(param.getKey()).iterator();
            Attribute<?> attr = attribute(Iterators.getNext(paramParts, null));
            Operator operator = operator(Iterators.getNext(paramParts, null));
            operands.add(createQuery(attr, operator, param.getValue()[0]));
        }
        return operands.build();
    }

    private Attribute<?> attribute(String attrName) {
        Attribute<?> possibleAttr = attributes.get(attrName);
        checkArgument(possibleAttr != null, "Invalid param " + attrName);
        return possibleAttr;
    }

    private Operator operator(String possibleOpName) {
        return possibleOpName == null ? Operators.EQUALS
                                      : Operators.lookup(possibleOpName);
    }

    private <T> AtomicQuery createQuery(Attribute<T> attr, Operator op, String paramValue) {
        return attr.createQuery(op, values(attr, paramValue));
    }
    
    private <T> List<T> values(Attribute<T> attr, String paramValue) {
        return coercer(attr).apply(valueSplitter.split(paramValue));
    }

    @SuppressWarnings("unchecked")
    private <T> AttributeCoercer<String, T> coercer(Attribute<T> attr) {
        return (AttributeCoercer<String, T>) coercers.get(attr);
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, String[]> getParameterMap(HttpServletRequest request) {
        return (Map<String, String[]>) request.getParameterMap();
    }

}
