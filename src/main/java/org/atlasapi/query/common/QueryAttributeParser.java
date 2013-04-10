package org.atlasapi.query.common;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.atlasapi.content.criteria.AttributeQuery;
import org.atlasapi.content.criteria.AttributeQuerySet;
import org.atlasapi.content.criteria.attribute.Attribute;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class QueryAttributeParser {
    
    private final Splitter valueSplitter = Splitter.on(',').omitEmptyStrings().trimResults();
    
    private final AttributeLookupTree attributesLookup;
    private final Map<Attribute<?>, ? extends QueryAtomParser<String, ?>> parsers;
    private final ReplacementSuggestion replacementSuggestion;
    private final ImmutableSet<String> ignoredParameters;
    
    public QueryAttributeParser(Iterable<? extends QueryAtomParser<String, ?>> attributeParsers) {
        this(attributeParsers, ImmutableSet.<String>of());
    }

    public QueryAttributeParser(Iterable<? extends QueryAtomParser<String, ?>> attributeParsers, Iterable<String> ignoredParameters) {
        this.parsers = Maps.uniqueIndex(attributeParsers, new Function<QueryAtomParser<String, ?>, Attribute<?>>(){
            @Override
            public Attribute<?> apply(QueryAtomParser<String, ?> input) {
                return input.getAttribute();
            }});
        this.attributesLookup = initLookup(parsers.keySet());
        this.replacementSuggestion = new ReplacementSuggestion(
            attributesLookup.allKeys(), "Invalid parameters: ", " (did you mean %s?)");
        this.ignoredParameters = ImmutableSet.copyOf(ignoredParameters);
    }
    
    public QueryAttributeParser copyWithIgnoredParameters(Iterable<String> ignored) {
        return new QueryAttributeParser(this.parsers.values(), ignored);
    }

    private AttributeLookupTree initLookup(Set<Attribute<?>> attributes) {
        AttributeLookupTree lookup = new AttributeLookupTree();
        for (Attribute<?> attribute : attributes) {
            lookup.put(attribute);
        }
        return lookup;
    }

    public AttributeQuerySet parse(HttpServletRequest request) throws QueryParseException {
        return new AttributeQuerySet(parseListQuery(request));
    }

    private Iterable<? extends AttributeQuery<?>> parseListQuery(HttpServletRequest request)
            throws QueryParseException {
        ImmutableSet.Builder<AttributeQuery<?>> operands = ImmutableSet.builder();
        LinkedList<String> invalidParams = Lists.newLinkedList();
        for(Entry<String, String[]> param : getParameterMap(request).entrySet()) {
            if (ignoredParameters.contains(param.getKey())) {
                continue;
            }
            Optional<Attribute<?>> attribute = attributesLookup.attributeFor(param.getKey());
            if (attribute.isPresent()) {
                QueryAtomParser<String, ?> parser = parsers.get(attribute.get());
                operands.add(parser.parse(param.getKey(), splitVals(param.getValue())));
            } else {
                invalidParams.add(param.getKey());
            }
        }
        if (invalidParams.isEmpty()) {
            return operands.build();
        }
        throw new InvalidParameterException(replacementSuggestion.forInvalid(invalidParams));
    }

    private Iterable<String> splitVals(String[] value) {
        return FluentIterable.from(Arrays.asList(value))
            .transformAndConcat(new Function<String, Iterable<String>>(){
                @Override
                public Iterable<String> apply(String input) {
                    return valueSplitter.split(input);
                }
        });
    }

    @SuppressWarnings("unchecked")
    private Map<String, String[]> getParameterMap(HttpServletRequest request) {
        return (Map<String, String[]>) request.getParameterMap();
    }

}
