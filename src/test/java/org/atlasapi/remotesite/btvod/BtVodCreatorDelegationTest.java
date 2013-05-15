package org.atlasapi.remotesite.btvod;

import static org.atlasapi.remotesite.btvod.BtVodEpisodeParseTest.getContentElementFromFile;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Set;

import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Film;
import org.atlasapi.media.entity.Series;
import org.junit.Test;

import com.google.common.collect.Iterables;

public class BtVodCreatorDelegationTest {
    
    private final BtVodContentExtractor contentExtractor;
    
    public BtVodCreatorDelegationTest() {
        contentExtractor = new BtVodContentExtractor(new BtVodFilmCreator(), new BtVodEpisodeCreator(), new BtVodSeriesCreator(), new BtVodItemDataExtractor());    
    }

    @Test
    public void testCreatorDelegationForEpisodeData() throws ValidityException, ParsingException, IOException {
        Set<? extends Content> contents = contentExtractor.extract(getContentElementFromFile("btvod-episode.xml"));
        
        assertThat(contents.size(), is(2));
        
        boolean foundEpisode = false;
        boolean foundSeries = false;
        
        for (Content content : contents) {
            if (content instanceof Episode) {
                foundEpisode = true;
            }
            if (content instanceof Series) {
                foundSeries = true;
            }
        }
        
        assertTrue(foundEpisode);
        assertTrue(foundSeries);
    }

    @Test
    public void testCreatorDelegationForFilmData() throws ValidityException, ParsingException, IOException {
        Set<? extends Content> contents = contentExtractor.extract(getContentElementFromFile("btvod-film.xml"));
        
        Content content = Iterables.getOnlyElement(contents);
        assertTrue(content instanceof Film);
    }
}
