package org.atlasapi.remotesite.rovi.processing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Film;
import org.atlasapi.media.entity.Item;
import org.atlasapi.remotesite.rovi.model.RoviShowType;
import org.junit.Test;


public class ContentCreatorTest {

    @Test
    public void testContentCreation() {
        Content content;
        
        content = ContentCreator.createContent(RoviShowType.MOVIE);
        assertThat(content, is(Film.class));
        
        content = ContentCreator.createContent(RoviShowType.OTHER);
        assertThat(content, is(Item.class));
        
        content = ContentCreator.createContent(RoviShowType.SERIES_EPISODE);
        assertThat(content, is(Episode.class));

        content = ContentCreator.createContent(RoviShowType.SERIES_MASTER);
        assertThat(content, is(Brand.class));
    }
    
    @Test
    public void testHasTheRightType() {
        Content content;
        
        content = new Film();
        assertTrue(ContentCreator.hasCorrectType(content, RoviShowType.MOVIE));
        
        content = new Brand();
        assertTrue(ContentCreator.hasCorrectType(content, RoviShowType.SERIES_MASTER));
        
        content = new Episode();
        assertTrue(ContentCreator.hasCorrectType(content, RoviShowType.SERIES_EPISODE));

        content = new Item();
        assertTrue(ContentCreator.hasCorrectType(content, RoviShowType.OTHER));
        
        content = new Episode();
        assertFalse(ContentCreator.hasCorrectType(content, RoviShowType.MOVIE));
        
        content = new Episode();
        assertFalse(ContentCreator.hasCorrectType(content, RoviShowType.OTHER));
    }
    
}
