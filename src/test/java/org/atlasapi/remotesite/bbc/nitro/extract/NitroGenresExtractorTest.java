package org.atlasapi.remotesite.bbc.nitro.extract;

import static org.junit.Assert.*;

import java.util.Set;

import org.atlasapi.remotesite.bbc.nitro.v1.NitroGenre;
import org.atlasapi.remotesite.bbc.nitro.v1.NitroGenreGroup;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;


public class NitroGenresExtractorTest {

    private final NitroGenresExtractor extractor
        = new NitroGenresExtractor();
    
    @Test
    public void testExtractsGenresFromGroups() {
        
        ImmutableList<NitroGenreGroup> ggs = ImmutableList.of(genreGroup(genre("Factual"),genre("Life Stories")),
            genreGroup(genre("Factual"), genre("Families &amp; Relationships")), 
            genreGroup(genre("Factual"), genre("History")));
     
        Set<String> extracted = extractor.extract(ggs);
        
        Set<String> expected = ImmutableSet.of(
            "http://www.bbc.co.uk/programmes/genres/factual/lifestories",
            "http://www.bbc.co.uk/programmes/genres/factual/history",
            "http://www.bbc.co.uk/programmes/genres/factual",
            "http://www.bbc.co.uk/programmes/genres/factual/familiesandrelationships");
        
        assertEquals(expected, extracted);
    }

    private NitroGenreGroup genreGroup(NitroGenre... genres) {
        NitroGenreGroup nitroGenreGroup = new NitroGenreGroup();
        nitroGenreGroup.setGenres(ImmutableList.copyOf(genres));
        return nitroGenreGroup;
    }

    private NitroGenre genre(String title) {
        NitroGenre nitroGenre = new NitroGenre();
        nitroGenre.setId(title);
        nitroGenre.setTitle(title);
        return nitroGenre;
    }

}
