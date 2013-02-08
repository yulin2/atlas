package org.atlasapi.query.common;

import static org.junit.Assert.assertTrue;

import org.atlasapi.content.criteria.AtomicQuery;
import org.atlasapi.content.criteria.AtomicQuerySet;
import org.atlasapi.content.criteria.IdAttributeQuery;
import org.atlasapi.content.criteria.QueryVisitorAdapter;
import org.atlasapi.content.criteria.attribute.Attributes;
import org.atlasapi.media.common.Id;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;
import com.metabroadcast.common.servlet.StubHttpServletRequest;


public class QueryAttributeParserTest {

    private NumberToShortStringCodec idCodec = SubstitutionTableNumberCodec.lowerCaseOnly();
    private final QueryAttributeParser parser = new QueryAttributeParser(ImmutableMap.of(
        Attributes.ID, AttributeCoercers.idCoercer(idCodec),
        Attributes.ALIASES_NAMESPACE, AttributeCoercers.stringCoercer(),
        Attributes.ALIASES_VALUE, AttributeCoercers.stringCoercer()
    ));
    
    @Test
    public void testParseQueryWithIdAttribute() {
        final Id id1 = Id.valueOf(1234);
        final Id id2 = Id.valueOf(1235);
        
        AtomicQuerySet queries = parser.parse(request().withParam("id",String.format("%s,%s", 
            idCodec.encode(id1.toBigInteger()), idCodec.encode(id2.toBigInteger())
        )));
        
        AtomicQuery idAttributeQuery = Iterables.getOnlyElement(queries);
        
        assertTrue(idAttributeQuery.accept(new QueryVisitorAdapter<Boolean>() {
            @Override
            public Boolean visit(IdAttributeQuery query) {
                return query.getValue().containsAll(ImmutableList.of(id1, id2));
            }
            
            @Override
            protected Boolean defaultValue(AtomicQuery query) {
                return false;
            }
        }));
    }
    
    private StubHttpServletRequest request() {
        return new StubHttpServletRequest();
    }
}
