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

package org.uriplay.remotesite.ted;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.Set;

import org.jmock.integration.junit3.MockObjectTestCase;
import org.uriplay.media.TransportType;
import org.uriplay.media.entity.Encoding;
import org.uriplay.media.entity.Item;
import org.uriplay.media.entity.Location;
import org.uriplay.media.entity.Version;
import org.uriplay.remotesite.html.HtmlDescriptionOfItem;
import org.uriplay.remotesite.html.HtmlDescriptionSource;

import com.google.common.collect.Iterables;

/**
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class TedTalkGraphExtractorTest extends MockObjectTestCase {
	
	static final String ITEM_URI = "http://www.ted.com/talks/elizabeth_gilbert_on_genius.html";
	
	TedTalkGraphExtractor extractor;
	HtmlDescriptionSource source;
	HtmlDescriptionOfItem item = new HtmlDescriptionOfItem();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		source = new HtmlDescriptionSource(item, ITEM_URI);
		extractor = new TedTalkGraphExtractor();
		
		item.setTitle("News News News");
		item.setDescription("The News");
		item.setThumbnail("thumbnail.image");
		item.setVideoSource("videoSource");
		item.setFlashFile("http://video.ted.com/talks/embed/RayKurzweil_2005.flv");
	}
	
	public void testCreatesEpisodesFromFeedEntries() throws Exception {
		
		Item item = extractor.extractFrom(source);

		assertThat(item.getCanonicalUri(), is(ITEM_URI));
		assertThat(item.getTitle(), is("News News News"));
		assertThat(item.getDescription(), is("The News"));
		assertThat(item.getPublisher(), is("ted.com"));
		assertThat(item.getCurie(), is("ted:elizabeth_gilbert_on_genius"));
		assertThat(item.getIsLongForm(), is(true));
		assertThat(item.getThumbnail(), is("thumbnail.image"));

		Version version = Iterables.getOnlyElement(item.getVersions());
		Encoding encoding = Iterables.getOnlyElement(version.getManifestedAs());
		
		assertThat(encoding.getAvailableAt().size(), is(2));
		
		Location embedLocation = locationByType("embedobject", encoding.getAvailableAt());
		assertThat(embedLocation.getUri(), is("videoSource"));
		assertThat(embedLocation.getTransportType(), is(TransportType.EMBEDOBJECT));
		assertThat(embedLocation.getTransportSubType(), is("html"));
		assertThat(embedLocation.getEmbedCode(), containsString("vu=http://video.ted.com/talks/embed/RayKurzweil_2005.flv"));
		
		Location linkLocation =  locationByType("htmlembed", encoding.getAvailableAt());
		assertThat(linkLocation.getUri(), is(ITEM_URI));
		assertThat(linkLocation.getTransportType(), is(TransportType.HTMLEMBED));
	}

	private Location locationByType(String transportType, Set<Location> availableAt) {
		for (Location location : availableAt) {
			if (transportType.equals(location.getTransportType())) {
				return location;
			}
		}
		fail("Location with transport type: " + transportType + " not found");
		return null;
	}
}
