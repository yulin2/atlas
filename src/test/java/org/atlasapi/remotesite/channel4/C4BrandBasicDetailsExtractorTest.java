package org.atlasapi.remotesite.channel4;

 import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import junit.framework.TestCase;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Publisher;
import org.joda.time.DateTime;

import com.google.common.io.Resources;
import com.metabroadcast.common.time.DateTimeZones;
import com.sun.syndication.feed.atom.Feed;


public class C4BrandBasicDetailsExtractorTest extends TestCase {

	private final C4BrandBasicDetailsExtractor extractor = new C4BrandBasicDetailsExtractor();
	
	public void testExtractingABrand() throws Exception {
		
		AtomFeedBuilder brandFeed = new AtomFeedBuilder(Resources.getResource(getClass(), "ramsays-kitchen-nightmares.atom"));
		
		Brand brand = extractor.extract(brandFeed.build());
		
		assertThat(brand.getCanonicalUri(), is("http://www.channel4.com/programmes/ramsays-kitchen-nightmares"));
		assertThat(brand.getAliases(), hasItem("http://www.channel4.com/programmes/ramsays-kitchen-nightmares/4od"));
		assertThat(brand.getAliases(), hasItem("tag:www.channel4.com,2009:/programmes/ramsays-kitchen-nightmares"));
		assertThat(brand.getCurie(), is("c4:ramsays-kitchen-nightmares"));
		assertThat(brand.getTitle(), is("Ramsay's Kitchen Nightmares"));
		assertThat(brand.getLastUpdated(), is(new DateTime("2010-08-09T15:57:38.852Z", DateTimeZones.UTC)));
		assertThat(brand.getPublisher(), is(Publisher.C4));
		assertThat(brand.getDescription(), startsWith("Gordon Ramsay attempts to transform struggling restaurants with his"));
		assertThat(brand.getThumbnail(), is("http://www.channel4.com/assets/programmes/images/ramsays-kitchen-nightmares/ramsays-kitchen-nightmares_200x113.jpg"));
		assertThat(brand.getImage(), is("http://www.channel4.com/assets/programmes/images/ramsays-kitchen-nightmares/ramsays-kitchen-nightmares_625x352.jpg"));
	}
	
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
