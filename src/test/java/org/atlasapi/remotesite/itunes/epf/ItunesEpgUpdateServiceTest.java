package org.atlasapi.remotesite.itunes.epf;

import java.io.File;
import java.util.concurrent.Executor;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Series;
import org.atlasapi.persistence.content.ContentWriter;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.jmock.lib.concurrent.DeterministicExecutor;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;

public class ItunesEpgUpdateServiceTest extends MockObjectTestCase {
    
    private final ContentWriter writer = mock(ContentWriter.class);
    private File artist;
    private File artistCollection;
    private File collection;

    private static final String LINE_END = ((char)2)+"\n";
    private final String NO_SUFFIX = null;
    
    protected void setUp() throws Exception {
        artist = File.createTempFile("artist", NO_SUFFIX);
        Files.write(Joiner.on((char)1).join(ImmutableList.of(
                "1320832802897","102225079","The Office","1","http://itunes.apple.com/artist/the-office/id102225079?uo=5","2" + LINE_END
        )), artist, Charsets.UTF_8);
        
        artistCollection = File.createTempFile("artistCollection", NO_SUFFIX);
        Files.write(Joiner.on((char)1).join(ImmutableList.of(
                "1320832802897","102225079","102772946","1","1" + LINE_END
        )), artistCollection, Charsets.UTF_8);
        
        collection = File.createTempFile("collection", NO_SUFFIX);
        Files.write(Joiner.on((char)1).join(ImmutableList.of(
                "1320832802897","102772946","The Office, Season 1","","","","","http://itunes.apple.com/tv-season/the-office-season-1/id102772946?uo=5",
                "http://a1311.phobos.apple.com/us/r1000/037/Features/c8/1b/65/dj.jxnmyfbk.227x170-99.jpg","2005 03 24","1971 05 27","","NBCUniversal","2005 NBC Universal","2005 NBC Universal","4","0","6" + LINE_END
        )), collection, Charsets.UTF_8);
        
    }
    
    public void testUpdaterWritesBrandsFromArtistsFile() {
        
        final DeterministicExecutor executor = new DeterministicExecutor();
        
        ItunesEpgUpdateService service = new ItunesEpgUpdateService(artist, writer) {
            protected Executor executor() {
                return executor;
            };
        };
        
        checking(new Expectations(){{
            one(writer).createOrUpdate(with(brand("http://itunes.apple.com/artist/id102225079")));
            one(writer).createOrUpdate(with(series("http://itunes.apple.com/tv-season/id102772946")));
        }});
        
        service.start();
        
        executor.runUntilIdle();
        
    }

    private Matcher<Brand> brand(final String uri) {
        return new TypeSafeMatcher<Brand>() {

            @Override
            public void describeTo(Description desc) {
                desc.appendValue("Container with " + uri);
            }

            @Override
            public boolean matchesSafely(Brand container) {
                return container.getCanonicalUri().equals(uri);
            }
        };
    }
    
    private Matcher<Series> series(final String uri) {
        return new TypeSafeMatcher<Series>() {

            @Override
            public void describeTo(Description desc) {
                desc.appendValue("Container with " + uri);
            }

            @Override
            public boolean matchesSafely(Series container) {
                return container.getCanonicalUri().equals(uri);
            }
        };
    }
}
