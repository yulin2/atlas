package org.atlasapi.remotesite.btvod;

import static org.atlasapi.remotesite.btvod.BtVodEpisodeParseTest.getContentElementFromFile;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Series;
import org.atlasapi.media.entity.Specialization;
import org.atlasapi.remotesite.btvod.model.BtVodItemData;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

public class BtVodSeriesParseTest {

    private final BtVodContentCreator<Series> seriesCreator = new BtVodSeriesCreator();
    
    @Test
    public void testSeriesParsing() throws ValidityException, ParsingException, IOException {
        BtVodItemDataExtractor dataExtractor = new BtVodItemDataExtractor();
        BtVodItemData data = dataExtractor.extract(getContentElementFromFile("btvod-episode.xml"));
        
        Series series = seriesCreator.extract(data);
        
        // check contents of series
        assertEquals("http://bt.com/title_groups/11225", series.getCanonicalUri());
        assertEquals("Gavin and Stacey: S01", series.getTitle());
        assertThat(series.getSeriesNumber(), is(1));
        assertEquals(ImmutableSet.of(
                "https://preproduction-movida.bebanjo.net/api/title_groups/11225",
                "BBW_Gavin and Stacey_s1"
            ), series.getAliases());
        
        assertEquals(Specialization.TV, series.getSpecialization());
        
        assertEquals(Publisher.BT, series.getPublisher());
    }

}
