/* Copyright 2009 British Broadcasting Corporation
   Copyright 2009 Meta Broadcast Ltd

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License. */

package org.uriplay.remotesite.synd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.jherd.remotesite.Fetcher;
import org.jherd.remotesite.timing.RequestTimer;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.uriplay.media.entity.Countries;
import org.uriplay.media.entity.Country;
import org.uriplay.media.entity.Encoding;
import org.uriplay.media.entity.Item;
import org.uriplay.media.entity.Location;
import org.uriplay.media.entity.Playlist;
import org.uriplay.media.entity.Version;
import org.uriplay.remotesite.youtube.YouTubeGraphExtractor;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sun.syndication.feed.opml.Attribute;
import com.sun.syndication.feed.opml.Opml;
import com.sun.syndication.feed.opml.Outline;

/**
 * Unit test for {@link YouTubeGraphExtractor}
 *
 * @author Robert Chatley (robert@metabroadcast.com)
 */
@SuppressWarnings("unchecked")
public class OpmlGraphExtractorTest extends MockObjectTestCase {

	static final String OPML_URI = "http://downloads.bbc.co.uk/podcasts/all.opml";
	static final String FEED1_URI = "http://downloads.bbc.co.uk/someshow/rss.xml";
	static final String FEED2_URI = "http://feed1.com/rss.xml";
	static final String LOCATION1_URI = "http://downloads.bbc.co.uk/a.mp3";
	static final String LOCATION2_URI = "http://feed1.com/a.mp3";
	
	Fetcher<Object> fetcher = mock(Fetcher.class);
	
	OpmlGraphExtractor extractor = new OpmlGraphExtractor(fetcher);
	Opml feed;
	OpmlSource source;

	RequestTimer timer = mock(RequestTimer.class);

	private Playlist fetchedFeed1;
	private Playlist fetchedFeed2;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		feed = createFeed();
		source = new OpmlSource(feed, OPML_URI, timer);
		
		fetchedFeed1 = new Playlist();
		fetchedFeed1.setCanonicalUri(FEED1_URI);
		fetchedFeed1.addItem(itemWithLocation(LOCATION1_URI));
		
		fetchedFeed2 = new Playlist();
		fetchedFeed2.setCanonicalUri(FEED2_URI);
		fetchedFeed2.addItem(itemWithLocation(LOCATION2_URI));
	}

	private Item itemWithLocation(String locationUri) {
		Location location = new Location();
		location.setUri(locationUri);
		
		Encoding encoding = new Encoding();
		encoding.addAvailableAt(location);
		
		Version version = new Version();
		version.addManifestedAs(encoding);
		
		Item item = new Item();
		item.addVersion(version);
		
		return item;
	}

	private Opml createFeed() throws MalformedURLException {
		Opml opml = new Opml();
		opml.setTitle("All BBC Podcasts");
		Outline outline1 = new Outline("feed1", new URL(FEED1_URI), new URL("http:feed1.com"));
		addGenreAttributesTo(outline1);
		opml.setOutlines(Lists.newArrayList(
				outline1,
				new Outline("feed2", new URL(FEED2_URI), new URL("http:feed2.com"))
				));
		return opml;
	}

	private void addGenreAttributesTo(Outline outline) {
		List<Attribute> outlineAttributes = outline.getAttributes();
		outlineAttributes.add(new Attribute("bbcgenres", "Entertainment|Comedy & Quizzes"));
		outlineAttributes.add(new Attribute("allow", "uk"));
		outline.setAttributes(outlineAttributes);
	}
	
	public void testFetchesAllContainedPodcastsAndMergesTheirRepresentations() throws Exception {
		
		checking(new Expectations() {{
			one(fetcher).fetch(FEED1_URI, timer); will(returnValue(fetchedFeed1));
			one(fetcher).fetch(FEED2_URI, timer); will(returnValue(fetchedFeed2));
			exactly(2).of(timer).nest();
			exactly(2).of(timer).unnest();
		}});
		
		Playlist playlist = extractor.extract(source);
		assertThat(playlist.getCanonicalUri(), is(OPML_URI));
		
		assertThat(playlist.getTitle(), is("All BBC Podcasts"));
		assertThat(playlist.getDescription(), is("All BBC Podcasts"));

		List<Playlist> feeds = playlist.getPlaylists();
		
		Playlist feed1 = Iterables.get(feeds, 0);
		assertThat(feed1.getCanonicalUri(), is(FEED1_URI));
		assertThat(feed1.getGenres(), is((Set<String>) Sets.newHashSet("http://www.bbc.co.uk/programmes/genres/entertainmentandcomedy")));
		
		Playlist feed2 = Iterables.get(feeds, 1);
		assertThat(feed2.getCanonicalUri(), is(FEED2_URI));
	}
	
	public void testAppliesRestrictionToBbcLocationsBasedOnAllowAttribute() throws Exception {
				
		checking(new Expectations() {{
			one(fetcher).fetch(FEED1_URI, timer); will(returnValue(fetchedFeed1));
			one(fetcher).fetch(FEED2_URI, timer); will(returnValue(fetchedFeed2));
			exactly(2).of(timer).nest();
			exactly(2).of(timer).unnest();
		}});
		
		Playlist playlist = extractor.extract(source);

		assertThat(locationByUri(LOCATION1_URI, playlist.getPlaylists().get(0).getItems()).getPolicy().getAvailableCountries(), is((Set<Country>) Sets.newHashSet(Countries.GB)));
		assertThat(locationByUri(LOCATION2_URI, playlist.getPlaylists().get(1).getItems()).getPolicy(), is(nullValue()));
	}
	

	private static Location locationByUri(String uri, Iterable<Item> items) {
		for (Item item : items) {
			for (Version version : item.getVersions()) {
				for (Encoding encoding : version.getManifestedAs()) {
					for (Location location : encoding.getAvailableAt()) {
						if (uri.equals(location.getUri())) {
							return location;
						}
					}
				}
			}
		}
		throw new NoSuchElementException();
	}
}
