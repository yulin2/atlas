package org.atlasapi.query.content;

import static org.atlasapi.content.criteria.ContentQueryBuilder.query;

import java.util.List;

import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.content.criteria.attribute.Attributes;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;

import com.google.common.collect.Lists;

public class UniqueContentForUriQueryExecutorTest extends MockObjectTestCase {
    private KnownTypeQueryExecutor delegate = mock(KnownTypeQueryExecutor.class);
    private UniqueContentForUriQueryExecutor queryExectuor = new UniqueContentForUriQueryExecutor(delegate); 

    public void testShouldRemoveDuplicateBrand() {
        final ContentQuery query = query().equalTo(Attributes.BRAND_URI, "wikipedia:glee").build();
        
        Brand brand1 = new Brand("http://www.hulu.com/glee", "hulu:glee", Publisher.HULU);
        brand1.addAlias("wikipedia:glee");
        Brand brand2 = new Brand("http://channel4.com/glee", "c4:glee", Publisher.C4);
        brand2.addAlias("wikipedia:glee");
        
        final List<Brand> brands = Lists.newArrayList(brand1, brand2);
        
        checking(new Expectations() {{ 
            one(delegate).executeBrandQuery(query); will(returnValue(brands));
        }});
        
        List<Brand> results = queryExectuor.executeBrandQuery(query);
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(brand1, results.get(0));
    }
    
    public void testShouldRetrieveCanonical() {
        final ContentQuery query = query().equalTo(Attributes.BRAND_URI, "http://channel4.com/glee").build();
        
        Brand brand1 = new Brand("http://www.hulu.com/glee", "hulu:glee", Publisher.HULU);
        brand1.addAlias("wikipedia:glee");
        Brand brand2 = new Brand("http://channel4.com/glee", "c4:glee", Publisher.C4);
        brand2.addAlias("wikipedia:glee");
        
        final List<Brand> brands = Lists.newArrayList(brand1, brand2);
        
        checking(new Expectations() {{ 
            one(delegate).executeBrandQuery(query); will(returnValue(brands));
        }});
        
        List<Brand> results = queryExectuor.executeBrandQuery(query);
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(brand2, results.get(0));
    }
    
    public void testShouldRetrieveCanonicalPutInFirst() {
        final ContentQuery query = query().equalTo(Attributes.BRAND_URI, "hulu:glee").build();
        
        Brand brand1 = new Brand("http://www.hulu.com/glee", "hulu:glee", Publisher.HULU);
        brand1.addAlias("wikipedia:glee");
        Brand brand2 = new Brand("http://channel4.com/glee", "c4:glee", Publisher.C4);
        brand2.addAlias("wikipedia:glee");
        
        final List<Brand> brands = Lists.newArrayList(brand1, brand2);
        
        checking(new Expectations() {{ 
            one(delegate).executeBrandQuery(query); will(returnValue(brands));
        }});
        
        List<Brand> results = queryExectuor.executeBrandQuery(query);
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(brand1, results.get(0));
    }
    
    public void testShouldRemoveDuplicateBrandForLocation() {
        final ContentQuery query = query().equalTo(Attributes.BRAND_URI, "wikipedia:glee").equalTo(Attributes.POLICY_AVAILABLE_COUNTRY, Lists.newArrayList("uk")).build();
        
        Brand brand1 = new Brand("http://www.hulu.com/glee", "hulu:glee", Publisher.HULU);
        brand1.addAlias("wikipedia:glee");
        Brand brand2 = new Brand("http://channel4.com/glee", "c4:glee", Publisher.C4);
        brand2.addAlias("wikipedia:glee");
        
        final List<Brand> brands = Lists.newArrayList(brand1, brand2);
        
        checking(new Expectations() {{ 
            one(delegate).executeBrandQuery(query); will(returnValue(brands));
        }});
        
        List<Brand> results = queryExectuor.executeBrandQuery(query);
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(brand2, results.get(0));
    }
    
    public void testShouldNotRemoveCanoncialBrandForLocation() {
        final ContentQuery query = query().equalTo(Attributes.BRAND_URI, "http://www.hulu.com/glee").equalTo(Attributes.POLICY_AVAILABLE_COUNTRY, Lists.newArrayList("uk")).build();
        
        Brand brand1 = new Brand("http://www.hulu.com/glee", "hulu:glee", Publisher.HULU);
        brand1.addAlias("wikipedia:glee");
        Brand brand2 = new Brand("http://channel4.com/glee", "c4:glee", Publisher.C4);
        brand2.addAlias("wikipedia:glee");
        
        final List<Brand> brands = Lists.newArrayList(brand1, brand2);
        
        checking(new Expectations() {{ 
            one(delegate).executeBrandQuery(query); will(returnValue(brands));
        }});
        
        List<Brand> results = queryExectuor.executeBrandQuery(query);
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(brand1, results.get(0));
    }
    
    public void testShouldRemoveDuplicateItemForLocation() {
        final ContentQuery query = query().equalTo(Attributes.ITEM_URI, "wikipedia:glee").equalTo(Attributes.POLICY_AVAILABLE_COUNTRY, Lists.newArrayList("uk")).build();
        
        Item item1 = new Item("http://www.hulu.com/glee", "hulu:glee", Publisher.HULU);
        item1.addAlias("wikipedia:glee");
        Item item2 = new Item("http://channel4.com/glee", "c4:glee", Publisher.C4);
        item2.addAlias("wikipedia:glee");
        
        final List<Item> items = Lists.newArrayList(item1, item2);
        
        checking(new Expectations() {{ 
            one(delegate).executeItemQuery(query); will(returnValue(items));
        }});
        
        List<Item> results = queryExectuor.executeItemQuery(query);
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(item2, results.get(0));
    }
    
    public void testShouldWorkWithNoResults() {
        final ContentQuery query = query().equalTo(Attributes.BRAND_URI, "wikipedia:glee").build();
        
        final List<Brand> brands = Lists.newArrayList();
        
        checking(new Expectations() {{ 
            one(delegate).executeBrandQuery(query); will(returnValue(brands));
        }});
        
        List<Brand> results = queryExectuor.executeBrandQuery(query);
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }
    
    public void testShouldWorkReturnJustOne() {
        final ContentQuery query = query().equalTo(Attributes.BRAND_URI, "wikipedia:glee").build();
        
        Brand brand1 = new Brand("http://www.hulu.com/glee", "hulu:glee", Publisher.HULU);
        brand1.addAlias("wikipedia:glee");
        
        final List<Brand> brands = Lists.newArrayList(brand1);
        
        checking(new Expectations() {{ 
            one(delegate).executeBrandQuery(query); will(returnValue(brands));
        }});
        
        List<Brand> results = queryExectuor.executeBrandQuery(query);
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(brand1, results.get(0));
    }
}
