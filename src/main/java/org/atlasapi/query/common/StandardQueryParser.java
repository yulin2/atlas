package org.atlasapi.query.common;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.atlasapi.content.criteria.AttributeQuerySet;
import org.atlasapi.media.common.Id;

import com.metabroadcast.common.ids.NumberToShortStringCodec;

public class StandardQueryParser<T> implements QueryParser<T> {

    private final NumberToShortStringCodec idCodec;
    private final QueryAttributeParser attributeParser;
    private final QueryContextParser queryContextParser;

    private final Pattern singleResourcePattern;

    public StandardQueryParser(String resourceName, QueryAttributeParser attributeParser,
                            NumberToShortStringCodec idCodec,
                            QueryContextParser queryContextParser) {
        this.attributeParser = checkNotNull(attributeParser);
        this.queryContextParser = checkNotNull(queryContextParser);
        this.idCodec = checkNotNull(idCodec);
        this.singleResourcePattern = Pattern.compile(checkNotNull(resourceName) + "/([^.]+)(\\..*)?$");
    }

    @Override
    public Query<T> parse(HttpServletRequest request) throws QueryParseException {
        Id singleId = tryExtractSingleId(request);
        return singleId != null ? singleQuery(request, singleId) 
                                : listQuery(request);
    }

    private Id tryExtractSingleId(HttpServletRequest request) {
        Matcher matcher = singleResourcePattern.matcher(request.getRequestURI());
        return matcher.find() ? Id.valueOf(idCodec.decode(matcher.group(1)))
                              : null;
    }
    
    private Query<T> singleQuery(HttpServletRequest request, Id singleId) throws QueryParseException {
        return Query.singleQuery(singleId, queryContextParser.parseSingleContext(request));
    }

    private Query<T> listQuery(HttpServletRequest request) throws QueryParseException {
        AttributeQuerySet querySet = attributeParser.parse(request);
        return Query.listQuery(querySet,
            queryContextParser.parseListContext(request));
    }
    
}
