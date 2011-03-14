package org.atlasapi.equiv.tasks;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.Mockery;

import junit.framework.TestCase;

public class EquivCleanerTest extends TestCase {
    
    private final Mockery context = new Mockery();
    private final ContentResolver resolver = context.mock(ContentResolver.class);
    private final ContentWriter writer = context.mock(ContentWriter.class);
    
    private final EquivCleaner cleaner = new EquivCleaner(resolver, writer);

    public void testCleanEmptyEquivalences() {

        Brand brand = new Brand("testUri", "testCurie", Publisher.PA);
        
        context.checking(new Expectations(){{
            never(resolver).findByCanonicalUri(with(any(String.class)));
            never(writer).createOrUpdate(with(any(Item.class)));
            never(writer).createOrUpdate(with(any(Container.class)),with(false));
        }});
        
        cleaner.cleanEquivalences(brand);
        
        context.assertIsSatisfied();
    }
    
    public void testCleanEquivalences() {
        
        final Brand one = new Brand("testUri1", "testCurie1", Publisher.PA);
        final Brand two = new Brand("testUri2", "testCurie2", Publisher.PA);
        
        one.addEquivalentTo(two);
        two.addEquivalentTo(one);
        
        context.checking(new Expectations(){{
            one(resolver).findByCanonicalUri("testUri2"); will(returnValue(two));
            one(writer).createOrUpdate(with(updatedBrand()), with(false));
        }});
        
        cleaner.cleanEquivalences(one);
        
        assertThat(one.getEquivalentTo().isEmpty(), is(equalTo(true)));
        
        context.assertIsSatisfied();
        
    }

    private Matcher<Brand> updatedBrand() {
        return new TypeSafeMatcher<Brand>() {

            @Override
            public void describeTo(Description arg0) {
            }

            @Override
            public boolean matchesSafely(Brand updatedBrand) {
                return updatedBrand.getCanonicalUri().equals("testUri2") && !updatedBrand.getEquivalentTo().contains("testUri1");
            }
        };
    }
}
