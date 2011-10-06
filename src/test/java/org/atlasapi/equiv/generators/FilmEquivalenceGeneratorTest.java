package org.atlasapi.equiv.generators;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import junit.framework.TestCase;

public class FilmEquivalenceGeneratorTest extends TestCase {


    private final FilmEquivalenceGenerator generator = new FilmEquivalenceGenerator(null);

    public void testPerfectMatch() {
        assertThat(generator.match("equal", "equal"), is(1.0));
    }
    
    public void testDifferentMatch() {
        assertThat(generator.match("equal", "different"), is(0.0));
    }
    
    public void testShortDifferentMatch() {
        assertThat(generator.match("e", "d"), is(0.0));
    }
    
    public void testOneLetterMore() {
        assertThat(generator.match("downweighthis", "downweighthiss"), is(closeTo(0.1, 0.0000001)));
    }
    
    public void testShortOneLetterMore() {
        assertThat(generator.match("ds", "d"), is(closeTo(0.1, 0.0000001)));
    }

    public void testSymmetry() {
        assertEquals(generator.match("downweighthiss", "downweighthis"),generator.match("downweighthis", "downweighthiss"));
    }

    public void testDoubleLength() {
        assertThat(generator.match("equals", "equalsequals"),is(closeTo(0.0, 0.0000001)));
    }

    public void testDoubleLengthSymmetry() {
        assertThat(generator.match("equalsequals", "equals"),is(closeTo(0.0, 0.0000001)));
    }

    public void testMoreThanDoubleIsntNegative() {
        assertThat(generator.match("equal", "equalequals"),is(closeTo(0.0, 0.0000001)));
    }
    
}
