package org.uriplay.remotesite.freebase;

import java.util.List;

import junit.framework.TestCase;

import org.uriplay.media.entity.Brand;
import org.uriplay.media.entity.Equiv;

public class FreebaseBrandEquivalenceGeneratorTest extends TestCase {
    private FreebaseBrandEquivalenceGenerator generator = new FreebaseBrandEquivalenceGenerator();
    
    public void testShouldRetrieveWikipediaEquivForGlee() {
        Brand brand = new Brand("http://www.hulu.com/glee", "hulu:glee");
        brand.setTitle("Glee");
        
        List<Equiv> equivs = generator.equivalent(brand);
        assertNotNull(equivs);
        assertFalse(equivs.isEmpty());
        
        for (Equiv equiv: equivs) {
            assertEquals(brand.getCanonicalUri(), equiv.left());
            assertNotSame(equiv.left(), equiv.right());
            assertTrue(equiv.right().startsWith(FreebaseBrandEquivalenceGenerator.WIKIPEDIA) || equiv.right().startsWith(FreebaseBrandEquivalenceGenerator.HULU));
        }
        System.out.println(equivs);
    }
    
    public void testShouldRetrieveWikipediaEquivForCDWM() {
        Brand brand = new Brand("http://www.channel4.com/programmes/come-dine-with-me", "c4:glee");
        brand.setTitle("Come Dine With Me");
        
        List<Equiv> equivs = generator.equivalent(brand);
        assertNotNull(equivs);
        assertFalse(equivs.isEmpty());
        
        for (Equiv equiv: equivs) {
            assertEquals(brand.getCanonicalUri(), equiv.left());
            assertTrue(equiv.right().startsWith(FreebaseBrandEquivalenceGenerator.WIKIPEDIA));
        }
        System.out.println(equivs);
    }
}
