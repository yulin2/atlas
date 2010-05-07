/* Copyright 2009 Meta Broadcast Ltd

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License. */

package org.uriplay.remotesite.bbc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.jherd.hamcrest.Matchers.hasPropertyValue;
import static org.jherd.hamcrest.Matchers.hasPropertyValueContaining;

import java.util.Set;

import org.jherd.beans.Representation;
import org.jherd.beans.id.IdGenerator;
import org.jherd.beans.id.IdGeneratorFactory;
import org.jherd.beans.id.IntegerIdGenerator;
import org.jherd.remotesite.http.RemoteSiteClient;
import org.jherd.remotesite.timing.RequestTimer;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.joda.time.DateTime;
import org.springframework.core.io.ClassPathResource;
import org.uriplay.media.TransportType;
import org.uriplay.media.entity.Brand;
import org.uriplay.media.entity.Broadcast;
import org.uriplay.media.entity.Encoding;
import org.uriplay.media.entity.Episode;
import org.uriplay.media.entity.Location;
import org.uriplay.media.entity.Playlist;
import org.uriplay.media.entity.Version;
import org.uriplay.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesContainerRef;
import org.uriplay.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesEpisode;
import org.uriplay.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesVersion;
import org.uriplay.remotesite.synd.SyndicationSource;

import com.google.common.collect.Sets;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

/**
 * @author Robert Chatley (robert@metabroadcast.com)
 */
@SuppressWarnings("unchecked")
public class BbcIplayerGraphExtractorTest extends MockObjectTestCase {
	
	static final String FEED_URI = "http://feeds.bbc.co.uk/iplayer/atoz/a/list";
	
	static final String SPOTLIGHT_BRAND_URI = "http://www.bbc.co.uk/programmes/b001";
	
	static final String BRAND_URI = "http://www.bbc.co.uk/programmes/b006v04h";
	static final String EPISODE_2_URI = "http://www.bbc.co.uk/programmes/b00kjbrc";
	static final String EPISODE_2_VERSION = "5";
	static final String EPISODE_2_WEB_ENCODING = "6";
	static final String EPISODE_2_WEB_LOCATION = "7";
	static final String EPISODE_2_BROADCAST = "8";

	static final String ORPHAN_ITEM_URI = "http://www.bbc.co.uk/programmes/b00kfr9s";

	static final RequestTimer TIMER = null;
	
	DateTime tuesday10pm = new DateTime(2009, 04, 21, 22, 00, 00, 00);
	
	IdGeneratorFactory idGeneratorFactory = mock(IdGeneratorFactory.class);
	IdGenerator idGenerator = new IntegerIdGenerator();
	
	RemoteSiteClient<SlashProgrammesRdf> episodeRdfClient = mock(RemoteSiteClient.class, "episodeClient");
	RemoteSiteClient<SlashProgrammesVersionRdf> versionRdfClient = mock(RemoteSiteClient.class, "versionClient");
	
	SlashProgrammesVersion version = new SlashProgrammesVersion().withResourceUri("/programmes/b00k2vtr#programme");
			
	SlashProgrammesEpisode episode = new SlashProgrammesEpisode().inPosition(6)
																 .withVersion(version)
																 .withDescription("Claire Savage investigates a workers sit-in against those that shut the Visteon plant down")
	                                                             .withGenres("/programmes/genres/factual/politics#genre", "/programmes/genres/news#genre")
																 .withTitle("Shutdown");

	SlashProgrammesContainerRef brand = new SlashProgrammesContainerRef().withUri("/programmes/b001#programme");
	SlashProgrammesRdf episodeRdf = new SlashProgrammesRdf().withEpisode(episode).withBrand(brand);
	SlashProgrammesVersionRdf versionRdf = new SlashProgrammesVersionRdf().withLastTransmitted(tuesday10pm, "/bbctwo#service");
	
	BbcIplayerGraphExtractor extractor;
	SyndFeed feed;
	SyndicationSource source;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		feed = createFeed("bbc-one-feed.atom.xml");
		source = new SyndicationSource(feed, FEED_URI, TIMER);
		extractor = new BbcIplayerGraphExtractor(episodeRdfClient, versionRdfClient, idGeneratorFactory);
		
		checking(new Expectations() {{
			allowing(idGeneratorFactory).create(); will(returnValue(idGenerator));
		}});
	}
	
	public void testCreatesEpisodesFromFeedEntries() throws Exception {
		
		checking(new Expectations() {{ 
			atLeast(1).of(episodeRdfClient).get("http://www.bbc.co.uk/programmes/b00kjbrc.rdf"); will(returnValue(episodeRdf));
			allowing(episodeRdfClient).get(with(startsWith("http://www.bbc.co.uk/programmes/"))); will(returnValue(episodeRdf));
			atLeast(1).of(versionRdfClient).get("http://www.bbc.co.uk/programmes/b00k2vtr.rdf"); will(returnValue(versionRdf));
			allowing(versionRdfClient).get(with(startsWith("http://www.bbc.co.uk/programmes/"))); will(returnValue(versionRdf));
		}});
		
		Representation representation = extractor.extractFrom(source);

		assertEquals(Episode.class, representation.getType(EPISODE_2_URI));
		assertThat(representation, hasPropertyValue(EPISODE_2_URI, "title", "Shutdown"));
		assertThat(representation, hasPropertyValue(EPISODE_2_URI, "description", "Claire Savage investigates a workers sit-in against those that shut the Visteon plant down"));
		assertThat(representation, hasPropertyValue(EPISODE_2_URI, "episodeNumber", 6));
		assertThat(representation, hasPropertyValue(EPISODE_2_URI, "publisher", "bbc.co.uk"));
		assertThat(representation, hasPropertyValue(EPISODE_2_URI, "containedIn", Sets.newHashSet(SPOTLIGHT_BRAND_URI)));
		Set<String> expectedGenres = bbcGenreUris("factual/politics", "news");
		expectedGenres.addAll(uriplayGenreUris("news", "factual"));
		assertThat(representation, hasPropertyValue(EPISODE_2_URI, "genres", expectedGenres));
		assertThat(representation, hasPropertyValue(EPISODE_2_URI, "isLongForm", true));
		
		assertThat(representation, hasPropertyValue(EPISODE_2_URI, "thumbnail", "http://www.bbc.co.uk/iplayer/images/episode/b00kjbrc_150_84.jpg"));
		
		assertEquals(Version.class, representation.getType(EPISODE_2_VERSION));
		assertThat(representation, hasPropertyValue(EPISODE_2_URI, "versions", Sets.newHashSet(EPISODE_2_VERSION)));
		assertThat(representation, hasPropertyValue(EPISODE_2_VERSION, "manifestedAs", Sets.newHashSet(EPISODE_2_WEB_ENCODING)));
		assertThat(representation, hasPropertyValue(EPISODE_2_VERSION, "transmissionTime", tuesday10pm));
		assertThat(representation, hasPropertyValue(EPISODE_2_VERSION, "broadcasts", Sets.newHashSet(EPISODE_2_BROADCAST)));
		assertThat(representation.getAnonymous(), hasItem(EPISODE_2_VERSION));
		
		assertEquals(Broadcast.class, representation.getType(EPISODE_2_BROADCAST));
		assertThat(representation, hasPropertyValue(EPISODE_2_BROADCAST, "broadcastOn", "http://www.bbc.co.uk/bbctwo"));
		
		assertEquals(Encoding.class, representation.getType(EPISODE_2_WEB_ENCODING));
		assertThat(representation, hasPropertyValue(EPISODE_2_WEB_ENCODING, "availableAt", Sets.newHashSet(EPISODE_2_WEB_LOCATION)));
		assertThat(representation.getAnonymous(), hasItem(EPISODE_2_WEB_ENCODING));
		
		assertEquals(Location.class, representation.getType(EPISODE_2_WEB_LOCATION));
		assertThat(representation, hasPropertyValue(EPISODE_2_WEB_LOCATION, "uri", "http://www.bbc.co.uk/iplayer/episode/b00kjbrc"));
		assertThat(representation, hasPropertyValue(EPISODE_2_WEB_LOCATION, "transportType", TransportType.HTMLEMBED));
		assertThat(representation, hasPropertyValue(EPISODE_2_WEB_LOCATION, "available", true));

		assertEquals(Brand.class, representation.getType(BRAND_URI));
		assertThat(representation, hasPropertyValue(BRAND_URI, "title", "Spotlight"));
		assertThat(representation, hasPropertyValue(BRAND_URI, "items", Sets.newHashSet(EPISODE_2_URI)));
		
		assertEquals(Episode.class, representation.getType(ORPHAN_ITEM_URI));

		assertEquals(Playlist.class, representation.getType(FEED_URI));
		assertThat(representation, hasPropertyValueContaining(FEED_URI, "playlists", BRAND_URI));
		assertThat(representation, hasPropertyValueContaining(FEED_URI, "items", ORPHAN_ITEM_URI));
		
		// playlist should only directly-include orphan items
		assertThat(representation, not(hasPropertyValueContaining(FEED_URI, "items", EPISODE_2_URI)));
	}

	private Set<String> bbcGenreUris(String... genres) {
		Set<String> uris = Sets.newHashSetWithExpectedSize(genres.length);
		for (String genre : genres) {
			uris.add("http://www.bbc.co.uk/programmes/genres/" + genre);
		}
		return uris;
	}
	
	private Set<String> uriplayGenreUris(String... genres) {
		Set<String> uris = Sets.newHashSetWithExpectedSize(genres.length);
		for (String genre : genres) {
			uris.add("http://uriplay.org/genres/uriplay/" + genre);
		}
		return uris;
	}
	
	SyndFeed createFeed(String filename) throws Exception {
		SyndFeedInput input = new SyndFeedInput();
		SyndFeed feed = input.build(new XmlReader(new ClassPathResource(filename).getInputStream()));
		return feed; 
	}
}
