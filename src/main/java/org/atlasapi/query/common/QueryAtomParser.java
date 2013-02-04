package org.atlasapi.query.common;

import static com.google.common.base.Preconditions.checkNotNull;

import org.atlasapi.content.criteria.AttributeQuery;
import org.atlasapi.content.criteria.attribute.Attribute;
import org.atlasapi.content.criteria.operator.Operator;
import org.atlasapi.content.criteria.operator.Operators;


public class QueryAtomParser<I, O> {

    private final Attribute<O> attribute;
    private final AttributeCoercer<I, O> coercer;

    public QueryAtomParser(Attribute<O> attribute, AttributeCoercer<I, O> coercer) {
        this.attribute = checkNotNull(attribute);
        this.coercer = checkNotNull(coercer);
    }
    
    public String getAttributeName() {
        return attribute.externalName();
    }
    
    public AttributeQuery<O> parse(String operatorName, Iterable<I> rawValues) {
        return attribute.createQuery(operator(operatorName), parse(rawValues));
    }

    private Operator operator(String operatorName) {
        return Operators.lookup(operatorName);
    }

    private Iterable<O> parse(Iterable<I> rawValues) {
        return coercer.apply(rawValues);
    }
    
}
