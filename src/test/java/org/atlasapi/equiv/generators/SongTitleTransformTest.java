package org.atlasapi.equiv.generators;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import org.junit.Test;

public class SongTitleTransformTest {

    private final SongTitleTransform sanitizer = new SongTitleTransform();
    
    @Test
    public void testIsIdentityForSongsWithoutFeaturedArtists() {
        assertThat(sanitizer.apply("Sultans of Swing"),
            is("Sultans of Swing"));
        assertThat(sanitizer.apply("Undefeatablatoration"),
            is("Undefeatablatoration"));
    }
    
    @Test
    public void testRemovesFeaturedArtists() {
        assertThat(sanitizer.apply("No Church in the Wild (feat. Frank Ocean)"),
            is("No Church in the Wild"));
        assertThat(sanitizer.apply("No Church in the Wild (feat Frank Ocean)"),
            is("No Church in the Wild"));
        assertThat(sanitizer.apply("No Church in the Wild feat. Frank Ocean"),
            is("No Church in the Wild"));
        assertThat(sanitizer.apply("No Church in the Wild feat Frank Ocean"),
            is("No Church in the Wild"));
        assertThat(sanitizer.apply("No Church in the Wild (Feat Frank Ocean)"),
            is("No Church in the Wild"));
        assertThat(sanitizer.apply("No Church in the Wild (Feat. Frank Ocean)"),
            is("No Church in the Wild"));
        assertThat(sanitizer.apply("No Church in the Wild Feat Frank Ocean"),
            is("No Church in the Wild"));
        assertThat(sanitizer.apply("No Church in the Wild Feat. Frank Ocean"),
            is("No Church in the Wild"));
        assertThat(sanitizer.apply("No Church in the Wild featuring Frank Ocean"),
            is("No Church in the Wild"));
        assertThat(sanitizer.apply("No Church in the Wild Featuring Frank Ocean"),
            is("No Church in the Wild"));
        assertThat(sanitizer.apply("No Church in the Wild (featuring Frank Ocean)"),
            is("No Church in the Wild"));
        assertThat(sanitizer.apply("No Church in the Wild (Featuring Frank Ocean)"),
            is("No Church in the Wild"));
    }
    
    @Test
    public void testRemovesTranscibers() {
        assertThat(sanitizer.apply("Grande Valse Brillante in D major Op 18 (transcribed Carl Davidov)"),
            is("Grande Valse Brillante in D major Op 18"));
    }

    @Test
    public void testExtractsFeaturedArtists() {
        assertThat(sanitizer.extractFeaturedArtists("No Church in the Wild (feat. Frank Ocean)"),
            is("Frank Ocean"));
    }
    
}
