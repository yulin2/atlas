package org.atlasapi.query.v4.topic;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.atlasapi.content.criteria.AtomicQuerySet;
import org.atlasapi.content.criteria.attribute.Attribute;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.metabroadcast.common.ids.NumberToShortStringCodec;

public class AttributeQueryParser {
    
    private ImmutableMap<String, Attribute<?>> attributes;
    private Map<Attribute<?>, AttributeCoercer<String, ?>> coercers;

    public AttributeQueryParser(Map<Attribute<?>, AttributeCoercer<String,?>> attributes,
                                NumberToShortStringCodec idCodec) {
        this.attributes = Maps.uniqueIndex(attributes.keySet(), new Function<Attribute<?>, String>(){
            @Override
            public String apply(Attribute<?> input) {
                return input.externalName();
        }});
        this.coercers = attributes;
    }

    public AtomicQuerySet parse(HttpServletRequest request) {
        return null;
    }

}
