package org.atlasapi.remotesite.talktalk;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.atlasapi.remotesite.talktalk.vod.bindings.GenreListType;
import org.atlasapi.remotesite.talktalk.vod.bindings.GenreType;
import org.atlasapi.remotesite.talktalk.vod.bindings.ItemDetailType;
import org.junit.Test;

import com.google.common.collect.Iterables;


public class TalkTalkGenresExtractorTest {
    
    private final TalkTalkGenresExtractor extractor = new TalkTalkGenresExtractor();
    
    @Test
    public void testExtractingGenres() {
        ItemDetailType detail = new ItemDetailType();
        GenreListType genreList = new GenreListType();
        GenreType genreType = new GenreType();
        genreType.setGenreCode("DRAMA");
        genreType.setGenreDescription("Drama");
        genreList.getGenre().add(genreType);
        detail.getGenreList().add(genreList);
        
        Iterable<String> extracted = extractor.extract(detail);
        
        assertThat(Iterables.getOnlyElement(extracted), is("http://talktalk.net/genres/DRAMA"));
    }
    
}
