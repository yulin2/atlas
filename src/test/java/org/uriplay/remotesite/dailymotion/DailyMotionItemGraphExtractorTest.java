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

package org.uriplay.remotesite.dailymotion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

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
 * @author John Ayres (john@metabroadcast.com)
 */
public class DailyMotionItemGraphExtractorTest extends MockObjectTestCase {
	
	static final String ITEM_URI = "http://www.dailymotion.com/video/xbqomc_dont-do-anything_fun";
	
	static final String VERSION_URI = "1";
	static final String ENCODING_URI = "2";
	static final String LOCATION_URI = "3";

	DailyMotionItemGraphExtractor extractor;
	HtmlDescriptionSource source;
	HtmlDescriptionOfItem item = new HtmlDescriptionOfItem();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		source = new HtmlDescriptionSource(item, ITEM_URI);
		extractor = new DailyMotionItemGraphExtractor();
		
		item.setTitle("News News News");
		item.setDescription("The News");
		item.setThumbnail("thumbnail.image");
		item.setVideoSource("videoSource");
	}
	
	public void testCreatesEpisodesFromFeedEntries() throws Exception {
		
		Item item = extractor.extract(source);

		assertThat(item.getTitle(), is("News News News"));
		assertThat(item.getDescription(), is("The News"));
		
		assertThat(item.getPublisher(), is("dailymotion.com"));
		assertThat(item.getCurie(), is("dm:xbqomc_dont-do-anything_fun"));
		
		assertThat(item.getThumbnail(), is("thumbnail.image"));

		Version version = Iterables.getOnlyElement(item.getVersions());
		
		Encoding encoding = Iterables.getOnlyElement(version.getManifestedAs());

		Location embedLocation = locationByType(TransportType.EMBED, encoding.getAvailableAt());
		assertThat(embedLocation.getTransportType(), is(TransportType.EMBED));
		assertThat(embedLocation.getTransportSubType(), is("html"));
		
		Location linkLocation = locationByType(TransportType.LINK, encoding.getAvailableAt());
		assertThat(linkLocation.getTransportType(), is(TransportType.LINK));
		assertThat(linkLocation.getUri(), is("http://www.dailymotion.com/video/xbqomc_dont-do-anything_fun"));
	}
	
	private Location locationByType(TransportType transportType, Set<Location> availableAt) {
		for (Location location : availableAt) {
			if (transportType.equals(location.getTransportType())) {
				return location;
			}
		}
		fail("Location with transport type: " + transportType + " not found");
		return null;
	}
}
