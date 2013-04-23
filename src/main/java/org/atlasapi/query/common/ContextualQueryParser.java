package org.atlasapi.query.common;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.atlasapi.content.criteria.AttributeQuery;
import org.atlasapi.content.criteria.AttributeQuerySet;
import org.atlasapi.content.criteria.attribute.Attribute;
import org.atlasapi.content.criteria.operator.Operators;
import org.atlasapi.media.common.Id;
import org.atlasapi.query.common.Query.ListQuery;
import org.atlasapi.query.common.Query.SingleQuery;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.ids.NumberToShortStringCodec;

public class ContextualQueryParser<C, R> {

    private final NumberToShortStringCodec idCodec;
    private final Attribute<Id> contextResouceAttribute;
    private final ContextualQueryContextParser queryContextParser;
    private final QueryAttributeParser attributeParser;

    private final Pattern contextResourcePattern;

    public ContextualQueryParser(String contextResourceName,
        Attribute<Id> contextResouceAttribute,
        String resourceName,
        NumberToShortStringCodec idCodec,
        QueryAttributeParser attributeParser, ContextualQueryContextParser queryContextParser) {
        this.attributeParser = checkNotNull(attributeParser);
        this.queryContextParser = checkNotNull(queryContextParser);
        this.idCodec = checkNotNull(idCodec);
        this.contextResourcePattern = contextResourcePattern(resourceName, contextResourceName);
        this.contextResouceAttribute = contextResouceAttribute;
    }

    private Pattern contextResourcePattern(String resourceName, String contextResourceName) {
        return Pattern.compile(contextResourceName + "/([^/]+)/" + resourceName + "(\\..*)?$");
    }
    
    public ContextualQuery<C, R> parse(HttpServletRequest request)
            throws QueryParseException {
        QueryContext context = queryContextParser.parseContext(request);
        SingleQuery<C> contextQuery = contextQuery(request, context);
        return new ContextualQuery<C, R>(
            contextQuery, resourceQuery(request, contextQuery.getOnlyId(), context), context);
    }

    private ListQuery<R> resourceQuery(HttpServletRequest request, Id contextId, QueryContext context)
            throws QueryParseException {
        AttributeQuerySet querySet = attributeParser.parse(request);
        querySet = querySet.copyWith(contextAttributeQuery(contextId));
        return Query.listQuery(querySet, context);
    }

    private AttributeQuery<Id> contextAttributeQuery(Id contextId) {
        return contextResouceAttribute.createQuery(Operators.EQUALS, ImmutableList.of(contextId));
    }

    private SingleQuery<C> contextQuery(HttpServletRequest request, QueryContext context) {
        return Query.singleQuery(contextId(request), context);
    }
    
    private Id contextId(HttpServletRequest request) {
        Matcher matcher = contextResourcePattern.matcher(request.getRequestURI());
        if (matcher.find()) {
            return Id.valueOf(idCodec.decode(matcher.group(1)));
        }
        throw new IllegalArgumentException(contextResourcePattern
            + " couldn't extract context ID from " + request.getRequestURI());
    }

}
