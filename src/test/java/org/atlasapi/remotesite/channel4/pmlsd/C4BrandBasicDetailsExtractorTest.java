package org.atlasapi.remotesite.channel4.pmlsd;

 import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import junit.framework.TestCase;

import org.atlasapi.genres.AtlasGenre;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.remotesite.channel4.pmlsd.C4AtomApi;
import org.atlasapi.remotesite.channel4.pmlsd.C4BrandBasicDetailsExtractor;
import org.atlasapi.remotesite.channel4.pmlsd.C4Module;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.io.Resources;
import com.metabroadcast.common.time.Clock;
import com.metabroadcast.common.time.DateTimeZones;
import com.metabroadcast.common.time.TimeMachine;
import com.sun.syndication.feed.atom.Feed;

@RunWith(MockitoJUnitRunner.class)
public class C4BrandBasicDetailsExtractorTest extends TestCase {

    private final Clock clock = new TimeMachine(new DateTime(DateTimeZones.UTC));
    
    private C4BrandBasicDetailsExtractor extractor;
	
    @Before
    public void setUp() {
        extractor = new C4BrandBasicDetailsExtractor(new C4AtomApi(new C4DummyChannelResolver()), clock);
    }

    @Test
	public void testExtractingABrand() throws Exception {
		
		AtomFeedBuilder brandFeed = new AtomFeedBuilder(Resources.getResource(getClass(), "ramsays-kitchen-nightmares.atom"));
		
		Brand brand = extractor.extract(brandFeed.build());
		
		assertThat(brand.getCanonicalUri(), is("http://www.channel4.com/programmes/ramsays-kitchen-nightmares"));
		// TODO new alias
		assertThat(brand.getAliasUrls(), hasItem("http://www.channel4.com/programmes/ramsays-kitchen-nightmares/4od"));
		assertThat(brand.getAliasUrls(), hasItem("tag:www.channel4.com,2009:/programmes/ramsays-kitchen-nightmares"));
		assertThat(brand.getCurie(), is("c4:ramsays-kitchen-nightmares"));
		assertThat(brand.getTitle(), is("Ramsay's Kitchen Nightmares"));
		assertThat(brand.getLastUpdated(), is(clock.now()));
		assertThat(brand.getPublisher(), is(C4Module.SOURCE));
		assertThat(brand.getDescription(), startsWith("Gordon Ramsay attempts to transform struggling restaurants with his"));
		assertThat(brand.getThumbnail(), is("http://www.channel4.com/assets/programmes/images/ramsays-kitchen-nightmares/ramsays-kitchen-nightmares_200x113.jpg"));
		assertThat(brand.getImage(), is("http://www.channel4.com/assets/programmes/images/ramsays-kitchen-nightmares/ramsays-kitchen-nightmares_625x352.jpg"));
		assertThat(brand.getGenres(), hasItems(
		        "http://www.channel4.com/programmes/tags/food",
		        AtlasGenre.LIFESTYLE.getUri()
		));
	}
    
    @Test
    public void testExtractingXboxPlatformBrand() throws Exception {
        
        AtomFeedBuilder brandFeed = new AtomFeedBuilder(Resources.getResource(getClass(), "brasseye-xbox.atom"));
        
        Brand brand = extractor.extract(brandFeed.build());
        
        assertThat(brand.getCanonicalUri(), is("http://www.channel4.com/programmes/brass-eye"));
        assertThat(brand.getAliasUrls(), hasItem("http://www.channel4.com/programmes/brass-eye/4od"));
        assertThat(brand.getAliasUrls(), hasItem("tag:www.channel4.com,2009:/programmes/brass-eye"));
        assertThat(brand.getCurie(), is("c4:brass-eye"));
        assertThat(brand.getTitle(), is("Brass Eye"));
        assertThat(brand.getLastUpdated(), is(clock.now()));
        assertThat(brand.getPublisher(), is(C4Module.SOURCE));
        assertThat(brand.getDescription(), startsWith("Anarchic spoof news comedy, fronted by Chris Morris"));
        assertThat(brand.getThumbnail(), is("http://www.channel4.com/assets/programmes/images/brass-eye/brass-eye_200x113.jpg"));
        assertThat(brand.getImage(), is("http://www.channel4.com/assets/programmes/images/brass-eye/brass-eye_625x352.jpg"));
        assertThat(brand.getGenres(), hasItems(
                "http://www.channel4.com/programmes/tags/comedy",
                AtlasGenre.COMEDY.getUri()
        ));
    }

    @Test
	public void testThatNonBrandPagesAreRejected() throws Exception {
		checkIllegalArgument("an id");
		checkIllegalArgument("tag:www.channel4.com,2009:/programmes/ramsays-kitchen-nightmares/video");
		checkIllegalArgument("tag:www.channel4.com,2009:/programmes/ramsays-kitchen-nightmares/episode-guide");
		checkIllegalArgument("tag:www.channel4.com,2009:/programmes/ramsays-kitchen-nightmares/episode-guide/series-1");
	}

	private void checkIllegalArgument(String feedId) {
		Feed feed = new Feed();
		feed.setId(feedId);
		try {
			extractor.extract(feed);
			fail("ID " + feedId + " should not be accepted");
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(), startsWith("Not a brand feed"));
		}
	}
}
