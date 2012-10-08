package org.atlasapi.remotesite.lovefilm;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;
import org.atlasapi.remotesite.lovefilm.LoveFilmData.LoveFilmDataRow;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.io.Resources;

public class LoveFilmDataRowContentExtractorTest {

    private final LoveFilmDataRowContentExtractor extractor = new LoveFilmDataRowContentExtractor();
    
    @Test
    public void testExtractsSerialEpisodeWithEpisodeTitle() throws IOException {
        
        LoveFilmDataRow row = rowFromFile("red-dwarf-s07-e02.csv");
        
        Optional<Content> extracted = extractor.extract(row);
        
        assertTrue(extracted.isPresent());
        Content content = extracted.get();
        
        assertThat(content, is(Episode.class));
        Episode episode = (Episode) content;
        
        assertThat(episode.getContainer().getUri(), endsWith("shows/179260"));
        assertThat(episode.getSeriesRef().getUri(), endsWith("seasons/182909"));
        assertThat(episode.getEpisodeNumber(), is(2));
        assertThat(episode.getTitle(), is("Stoke Me a Clipper"));
    }
    
    @Test
    public void testExtractsSerialEpisodeWithoutEpisodeTitle() throws IOException {
        
        LoveFilmDataRow row = rowFromFile("secret-diary-s02-e01.csv");
        
        Optional<Content> extracted = extractor.extract(row);
        
        assertTrue(extracted.isPresent());
        Content content = extracted.get();
        
        assertThat(content, is(Episode.class));
        Episode episode = (Episode) content;
        
        assertThat(episode.getContainer().getUri(), endsWith("shows/180765"));
        assertThat(episode.getSeriesRef().getUri(), endsWith("seasons/180775"));
        assertThat(episode.getEpisodeNumber(), is(1));
        assertThat(episode.getTitle(), is("Episode 1"));
    }
    
    @Test
    public void testExtractsEpisodeTitleWithHyphenatedBrandTitle() throws IOException {
        
        LoveFilmDataRow row = rowFromFile("mscl-s01-e16.csv");
        
        Optional<Content> extracted = extractor.extract(row);
        
        assertTrue(extracted.isPresent());
        Content content = extracted.get();
        
        assertThat(content, is(Episode.class));
        Episode episode = (Episode) content;
        
        assertThat(episode.getContainer().getUri(), endsWith("shows/188387"));
        assertThat(episode.getSeriesRef().getUri(), endsWith("seasons/188388"));
        assertThat(episode.getEpisodeNumber(), is(16));
        assertThat(episode.getTitle(), is("Resolutions"));
    }
    
    @Test
    public void testExtractsNonSerialEpisode() throws IOException {
        
        LoveFilmDataRow row = rowFromFile("human-planet-e08.csv");
        
        Optional<Content> extracted = extractor.extract(row);
        
        assertTrue(extracted.isPresent());
        Content content = extracted.get();
        
        assertThat(content, is(Episode.class));
        Episode episode = (Episode) content;
        
        assertThat(episode.getContainer().getUri(), endsWith("181684"));
        assertThat(episode.getSeriesRef(), is(nullValue()));
        assertThat(episode.getEpisodeNumber(), is(8));
        assertThat(episode.getGenres(), hasItem("http://lovefilm.com/genres/specialinterest"));
        assertThat(episode.getTitle(), is("Cities Surviving the Urban Jungle"));
    }
    
    @Test
    public void testExtractsEpisodeWithoutSequenceTitle() throws IOException {
        
        LoveFilmDataRow row = rowFromFile("blackadder-special.csv");
        
        Optional<Content> extracted = extractor.extract(row);
        
        assertTrue(extracted.isPresent());
        Content content = extracted.get();
        
        assertThat(content, is(Item.class));
        Item episode = (Item) content;
        
        assertThat(episode.getContainer().getUri(), endsWith("190046"));
        assertThat(episode.getTitle(), is("Blackadder's Christmas Carol"));
    }

    @Test
    public void testExtractsTopLevelSeasonAsBrand() throws IOException {
        
        LoveFilmDataRow row = rowFromFile("blackadder-specials.csv");
        
        Optional<Content> extracted = extractor.extract(row);
        
        assertTrue(extracted.isPresent());
        Content content = extracted.get();
        
        assertThat(content, is(Brand.class));
        Brand episode = (Brand) content;
        
        assertThat(episode.getTitle(), is("Blackadder - Special"));
    }

    private LoveFilmDataRow rowFromFile(String filename) throws IOException {
        URL testFile = Resources.getResource(getClass(), filename);
        LoveFilmData data = new LoveFilmData(Resources.newReaderSupplier(testFile, Charsets.UTF_8));
        return data.processData(new LoveFilmDataProcessor<LoveFilmDataRow>() {

            private LoveFilmDataRow row;

            @Override
            public boolean process(LoveFilmDataRow row) {
                this.row = row;
                return false;
            }

            @Override
            public LoveFilmDataRow getResult() {
                return row;
            }
            
        });
    }

}
