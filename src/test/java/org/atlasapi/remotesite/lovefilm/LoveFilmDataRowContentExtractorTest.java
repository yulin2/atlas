package org.atlasapi.remotesite.lovefilm;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.content.Content;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Film;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Specialization;
import org.atlasapi.remotesite.lovefilm.LoveFilmData.LoveFilmDataRow;
import org.junit.Ignore;
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
        assertThat(episode.getDescription(), is("Rimmer's alter ego, Ace, arrives on Starbug badly wounded."));
        assertEquals(Specialization.TV, episode.getSpecialization());
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
        assertEquals(Specialization.TV, episode.getSpecialization());
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
        assertEquals(Specialization.TV, episode.getSpecialization());
    }
    
    /* TODO
     * This has been disabled as the example below now has a series in the latest lovefilm data.
     * It may well be that this case no longer exists. The data in human-planet-e08.csv has not
     * been changed
     */
    @Ignore
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
        assertEquals(Specialization.TV, episode.getSpecialization());
    }
    
    @Test
    public void testExtractsEpisodeWithoutSequenceTitle() throws IOException {
        
        LoveFilmDataRow row = rowFromFile("blackadder-special.csv");
        
        Optional<Content> extracted = extractor.extract(row);
        
        assertTrue(extracted.isPresent());
        Content content = extracted.get();
        
        assertThat(content, is(Item.class));
        Item episode = (Item) content;
        
        assertThat(episode.getContainer().getUri(), endsWith("177351"));
        assertThat(episode.getTitle(), is("Blackadder's Christmas Carol"));
        assertEquals(Specialization.TV, episode.getSpecialization());
    }

    /* TODO
     * Check whether this case still applies, and if so find an appropriate case to support it. The data 
     * in blackadder-specials.csv has not been updated with the new columns
     */
    @Ignore
    @Test
    public void testExtractsTopLevelSeasonAsBrand() throws IOException {
        
        LoveFilmDataRow row = rowFromFile("blackadder-specials.csv");
        
        Optional<Content> extracted = extractor.extract(row);
        
        assertTrue(extracted.isPresent());
        Content content = extracted.get();
        
        assertThat(content, is(Brand.class));
        Brand episode = (Brand) content;
        
        assertThat(episode.getTitle(), is("Blackadder - Special"));
        assertEquals(Specialization.TV, episode.getSpecialization());
    }

    @Test
    public void testExtractsMovieAsFilm() throws IOException {
        
        LoveFilmDataRow row = rowFromFile("battle-royale.csv");
        
        Optional<Content> extracted = extractor.extract(row);
        
        assertTrue(extracted.isPresent());
        Content content = extracted.get();
        
        assertThat(content, is(Film.class));
        Film film = (Film) content;
        
        assertThat(film.getTitle(), is("Battle Royale"));
        assertThat(film.getCanonicalUri(), endsWith("168818"));
        assertEquals(Specialization.FILM, film.getSpecialization());
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
