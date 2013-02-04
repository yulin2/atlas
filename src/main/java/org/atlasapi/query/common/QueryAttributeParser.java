package org.atlasapi.query.common;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.atlasapi.content.criteria.AtomicQuery;
import org.atlasapi.content.criteria.AtomicQuerySet;
import org.atlasapi.content.criteria.attribute.Attribute;
import org.atlasapi.content.criteria.operator.Operator;
import org.atlasapi.content.criteria.operator.Operators;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;

public class QueryAttributeParser {
    
    private final Splitter operandSplitter = Splitter.on('.').omitEmptyStrings().trimResults();
    private final Splitter valueSplitter = Splitter.on(',').omitEmptyStrings().trimResults();
    
    private ImmutableMap<String, Attribute<?>> attributes;
    private Map<Attribute<?>, AttributeCoercer<String, ?>> coercers;

    public QueryAttributeParser(Map<? extends Attribute<?>, ? extends AttributeCoercer<String,?>> attributes) {
        this.attributes = ImmutableMap.copyOf(Maps.uniqueIndex(attributes.keySet(), new Function<Attribute<?>, String>(){
            @Override
            public String apply(Attribute<?> input) {
                return input.externalName();
        }}));
        this.coercers = ImmutableMap.copyOf(attributes);
    }

    public AtomicQuerySet parse(HttpServletRequest request) {
        return new AtomicQuerySet(parseListQuery(request));
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
