package org.atlasapi.remotesite.channel4;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.system.RemoteSiteClient;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.HttpResponse;
import com.metabroadcast.common.http.HttpStatusCodeException;
import com.sun.syndication.feed.atom.Feed;

public class C4BrandExtractorTest extends TestCase {

	private final AtomFeedBuilder series3Feed = new AtomFeedBuilder(Resources.getResource(getClass(), "ramsays-kitchen-nightmares-series-3.atom"));
	private final AtomFeedBuilder series4Feed = new AtomFeedBuilder(Resources.getResource(getClass(), "ramsays-kitchen-nightmares-series-4.atom"));
	private final AtomFeedBuilder brandFeed = new AtomFeedBuilder(Resources.getResource(getClass(), "ramsays-kitchen-nightmares.atom"));
	private final AtomFeedBuilder epsiodeGuideFeed = new AtomFeedBuilder(Resources.getResource(getClass(), "ramsays-kitchen-nightmares-episode-guide.atom"));
	private final AtomFeedBuilder fourOdFeed = new AtomFeedBuilder(Resources.getResource(getClass(), "ramsays-kitchen-nightmares-4od.atom"));
	private final AtomFeedBuilder epgFeed = new AtomFeedBuilder(Resources.getResource(getClass(), "ramsays-kitchen-nightmares-epg.atom"));

	private final RemoteSiteClient<Feed> feedClient = new StubC4AtomClient()
		.respondTo("http://api.channel4.com/programmes/ramsays-kitchen-nightmares.atom", brandFeed.build())
		.respondTo("http://api.channel4.com/programmes/ramsays-kitchen-nightmares/4od.atom", fourOdFeed.build())
		.respondTo("http://api.channel4.com/programmes/ramsays-kitchen-nightmares/episode-guide.atom", epsiodeGuideFeed.build())
		.respondTo("http://api.channel4.com/programmes/ramsays-kitchen-nightmares/episode-guide/series-3.atom", series3Feed.build())
		.respondTo("http://api.channel4.com/programmes/ramsays-kitchen-nightmares/episode-guide/series-4.atom", series4Feed.build())
		.respondTo("http://api.channel4.com/programmes/ramsays-kitchen-nightmares/epg.atom", epgFeed.build());
	
	public void testExtractingABrand() throws Exception {
		Brand brand = new C4AtomBackedBrandAdapter(feedClient).fetch("http://www.channel4.com/programmes/ramsays-kitchen-nightmares");
		
		assertThat(brand.getCanonicalUri(), is("http://www.channel4.com/programmes/ramsays-kitchen-nightmares"));

		Item firstItem = brand.getItems().get(0);
		
		assertThat(firstItem.getCanonicalUri(), is("http://www.channel4.com/programmes/ramsays-kitchen-nightmares/episode-guide/series-3/episode-1"));

		assertThat(firstItem.getAliases(), is((Set<String>) ImmutableSet.of("http://www.channel4.com/programmes/ramsays-kitchen-nightmares/4od#2921983", "tag:www.channel4.com,2009:/programmes/ramsays-kitchen-nightmares/episode-guide/series-3/episode-1")));
		
		Version firstItemVersion = Iterables.getOnlyElement(firstItem.getVersions());
		
		assertThat(firstItemVersion.getDuration(), is(2949));

		Encoding firstItemEncoding = Iterables.getOnlyElement(firstItemVersion.getManifestedAs());
		Location firstItemLocation = Iterables.getOnlyElement(firstItemEncoding.getAvailableAt());
		assertThat(firstItemLocation.getUri(), is("http://www.channel4.com/programmes/ramsays-kitchen-nightmares/4od#2921983"));
		
		Episode episodeNotOn4od = (Episode) find("http://www.channel4.com/programmes/ramsays-kitchen-nightmares/episode-guide/series-3/episode-5", brand.getItems());
		assertThat(episodeNotOn4od.getVersions().size(), is(0));
	}
	
	public void testThatBroadcastIsExtractedFromEpg() throws Exception {
	    Brand brand = new C4AtomBackedBrandAdapter(feedClient).fetch("http://www.channel4.com/programmes/ramsays-kitchen-nightmares");
	    
	    boolean found = false;
	    for (Item item: brand.getItems()) {
	        if (item.getCanonicalUri().equals("http://www.channel4.com/programmes/ramsays-kitchen-nightmares/episode-guide/series-4/episode-5")) {
	            assertFalse(item.getVersions().isEmpty());
	            Version version = item.getVersions().iterator().next();
	            
	            assertEquals(2, version.getBroadcasts().size());
	            for (Broadcast broadcast: version.getBroadcasts()) {
	                if (broadcast.getBroadcastDuration() == 60*55) {
	                    assertTrue(broadcast.getAliases().contains("tag:www.channel4.com,2009:slot/39861"));
	                    found = true;
	                } else {
	                    assertTrue(broadcast.getAliases().isEmpty());
	                }
	            }
	        }
	    }
	    
	    assertTrue(found);
	}
	
	public void testThatWhenTheEpisodeGuideReturnsABadStatusCodeSeries1IsAssumed() throws Exception {
	    HttpResponse response = new HttpResponse("error", 403).withFinalUrl("http://www.channel4.com/programmes/ramsays-kitchen-nightmares/episode-guide/series-3.atom");
		RemoteSiteClient<Feed> feedClient = new StubC4AtomClient()
			.respondTo("http://api.channel4.com/programmes/ramsays-kitchen-nightmares.atom", brandFeed.build())
			.respondTo("http://api.channel4.com/programmes/ramsays-kitchen-nightmares/episode-guide.atom", new HttpStatusCodeException(response))
			.respondTo("http://api.channel4.com/programmes/ramsays-kitchen-nightmares/episode-guide/series-1.atom", series3Feed.build());

		Brand brand = new C4AtomBackedBrandAdapter(feedClient).fetch("http://www.channel4.com/programmes/ramsays-kitchen-nightmares");
		assertThat(brand.getItems().size(), is(greaterThan(1)));
	}
	
	public void testThatWhenTheEpisodeGuideReturnsABadStatusCodeSeries3IsReturned() throws Exception {
	    HttpResponse response = new HttpResponse("error", 403).withFinalUrl("http://www.channel4.com/programmes/ramsays-kitchen-nightmares/episode-guide/series-3.atom");
        RemoteSiteClient<Feed> feedClient = new StubC4AtomClient()
            .respondTo("http://api.channel4.com/programmes/ramsays-kitchen-nightmares.atom", brandFeed.build())
            .respondTo("http://api.channel4.com/programmes/ramsays-kitchen-nightmares/episode-guide.atom", new HttpStatusCodeException(response))
            .respondTo("http://api.channel4.com/programmes/ramsays-kitchen-nightmares/episode-guide/series-3.atom", series4Feed.build());

        Brand brand = new C4AtomBackedBrandAdapter(feedClient).fetch("http://www.channel4.com/programmes/ramsays-kitchen-nightmares");
        assertThat(brand.getItems().size(), is(greaterThan(1)));
    }
	
	public void testThatWhenTheEpisodeGuideRedirectsToSeries1TheSeriesIsRead() throws Exception {
		RemoteSiteClient<Feed> feedClient = new StubC4AtomClient()
			.respondTo("http://api.channel4.com/programmes/ramsays-kitchen-nightmares.atom", brandFeed.build())
			.respondTo("http://api.channel4.com/programmes/ramsays-kitchen-nightmares/episode-guide.atom", series3Feed.build());

		Brand brand = new C4AtomBackedBrandAdapter(feedClient).fetch("http://www.channel4.com/programmes/ramsays-kitchen-nightmares");
		assertThat(brand.getItems().size(), is(greaterThan(1)));
	}
	
	private static class StubC4AtomClient implements RemoteSiteClient<Feed> {

		private Map<String, Object> respondsTo = Maps.newHashMap();

		@Override
		public Feed get(String uri) throws Exception {
			// Remove API key
			uri = removeQueryString(uri);
			Object response = respondsTo.get(uri);
			if (response == null) {
				throw new HttpStatusCodeException(404, "Not found: " + uri);
			} else if (response instanceof HttpException) {
			    throw (HttpException) response;
			}
			return (Feed) response;
		}

		private String removeQueryString(String url) throws MalformedURLException {
			String queryString = "?" + new URL(url).getQuery();
			return url.replace(queryString, "");
		}
		
		StubC4AtomClient respondTo(String url, Feed feed) {
			respondsTo.put(url, feed);
			return this;
		}
		
		StubC4AtomClient respondTo(String url, HttpException exception) {
		    respondsTo.put(url, exception);
		    return this;
		}
	}
	
	private final <T extends Content> T find(String uri, Iterable<T> episodes) {
		for (T episode : episodes) {
			if (episode.getCanonicalUri().equals(uri)) {
				return episode;
			}
		}
		throw new IllegalStateException("Not found");
	}
}
