package org.atlasapi.remotesite.netflix;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.util.Set;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Series;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;

public class NetflixSeriesParseTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testSeriesParsing() {
        Document netflixData;
        try {
            netflixData = new Builder().build(new ClassPathResource("netflix-episode.xml").getInputStream());
        } catch (Exception e) {
            fail("Exception " + e + " was thrown while opening the test file");
            // will never reach here;
            return;
        }
        
        Element rootElement = netflixData.getRootElement();
        
        NetflixContentExtractor<Episode> episodeExtractor = new NetflixEpisodeExtractor();
        NetflixContentExtractor<Series> seriesExtractor = new NetflixSeriesExtractor();
        NetflixXmlElementContentExtractor extractor = new NetflixXmlElementContentExtractor(Mockito.mock(NetflixContentExtractor.class), Mockito.mock(NetflixContentExtractor.class), episodeExtractor, seriesExtractor);

        assertThat(rootElement.getChildElements().size(), is(1));
        
        Set<? extends Content> contents = extractor.extract(rootElement.getChildElements().get(0));
        
        assertFalse(contents.isEmpty());
        assertThat(contents.size(), is(2));
        
        Series series= null;
        for (Content content : contents) {
            if (content instanceof Series) {
                series = (Series) content;
                break;
            }
        }
        
        // check that an Series was in the contents list
        assertFalse(series == null);
        
        assertThat(series.getCanonicalUri(), equalTo("http://gb.netflix.com/seasons/70136130-4"));
        assertThat(series.getTitle(), equalTo("Season 4"));
        assertThat(series.getSeriesNumber(), is(4));
    }

}
