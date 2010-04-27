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
import static org.hamcrest.Matchers.not;
import static org.jherd.hamcrest.Matchers.hasPropertyValue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.jherd.beans.Representation;
import org.jherd.remotesite.Fetcher;
import org.jherd.remotesite.timing.RequestTimer;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.uriplay.media.entity.Location;
import org.uriplay.media.entity.Playlist;
import org.uriplay.remotesite.bbc.Policy;
import org.uriplay.remotesite.youtube.YouTubeGraphExtractor;

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
	
	Fetcher<Representation> fetcher = mock(Fetcher.class);
	
	OpmlGraphExtractor extractor = new OpmlGraphExtractor(fetcher);
	Opml feed;
	OpmlSource source;

	RequestTimer timer = mock(RequestTimer.class);
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		feed = createFeed();
		source = new OpmlSource(feed, OPML_URI, timer);
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
		
		final Representation representation1 = new Representation();
		representation1.addUri(FEED1_URI);
		representation1.addType(FEED1_URI, Playlist.class);
		final Representation representation2 = new Representation();
		representation2.addUri(FEED2_URI);
		representation2.addType(FEED2_URI, Playlist.class);
		
		checking(new Expectations() {{
			one(fetcher).fetch(FEED1_URI, timer); will(returnValue(representation1));
			one(fetcher).fetch(FEED2_URI, timer); will(returnValue(representation2));
			exactly(2).of(timer).nest();
			exactly(2).of(timer).unnest();
		}});
		
		Representation representation = extractor.extractFrom(source);

		assertThat(representation, hasPropertyValue(OPML_URI, "title", "All BBC Podcasts"));
		assertThat(representation, hasPropertyValue(OPML_URI, "description", "All BBC Podcasts"));

		assertEquals(Playlist.class, representation.getType(FEED1_URI));
		assertThat(representation, hasPropertyValue(FEED1_URI, "genres", Sets.newHashSet("http://www.bbc.co.uk/programmes/genres/entertainmentandcomedy")));
		
		assertEquals(Playlist.class, representation.getType(FEED2_URI));
		assertEquals(Playlist.class, representation.getType(OPML_URI));
	}
	
	public void testAppliesRestrictionToBbcLocationsBasedOnAllowAttribute() throws Exception {
		
		final Representation representation1 = new Representation();
		representation1.addUri(FEED1_URI);
		representation1.addType(FEED1_URI, Playlist.class);
		representation1.addUri(LOCATION1_URI);
		representation1.addType(LOCATION1_URI, Location.class);
		final Representation representation2 = new Representation();
		representation2.addUri(FEED2_URI);
		representation2.addType(FEED2_URI, Playlist.class);
		representation2.addUri(LOCATION2_URI);
		representation2.addType(LOCATION2_URI, Location.class);
		
		checking(new Expectations() {{
			one(fetcher).fetch(FEED1_URI, timer); will(returnValue(representation1));
			one(fetcher).fetch(FEED2_URI, timer); will(returnValue(representation2));
			exactly(2).of(timer).nest();
			exactly(2).of(timer).unnest();
		}});
		
		Representation representation = extractor.extractFrom(source);

		assertThat(representation, hasPropertyValue(LOCATION1_URI, "restrictedBy", Policy.SEVEN_DAYS_UK_ONLY));
		assertThat(representation, not(hasPropertyValue(LOCATION2_URI, "restrictedBy", Policy.SEVEN_DAYS)));
		assertThat(representation, not(hasPropertyValue(LOCATION2_URI, "restrictedBy", Policy.SEVEN_DAYS_UK_ONLY)));
	}

}
