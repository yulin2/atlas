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

package org.atlasapi.remotesite.bliptv;

import static org.atlasapi.remotesite.Matchers.encodingMatcher;
import static org.atlasapi.remotesite.Matchers.locationMatcher;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import junit.framework.TestCase;

import org.atlasapi.media.TransportType;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Version;
import org.atlasapi.remotesite.Matchers.EncodingMatcher;
import org.atlasapi.remotesite.html.HtmlDescriptionOfItem;
import org.atlasapi.remotesite.html.HtmlDescriptionSource;
import org.hamcrest.Matchers;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.metabroadcast.common.media.MimeType;

/**
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class BlipTvGraphExtractorTest extends TestCase {
	
	static final String ITEM_URI = "http://blip.tv/file/2276955/";
	
	BlipTvGraphExtractor extractor;
	HtmlDescriptionSource source;
	HtmlDescriptionOfItem item = new HtmlDescriptionOfItem();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		source = new HtmlDescriptionSource(item, ITEM_URI);
		extractor = new BlipTvGraphExtractor();
		
		item.setTitle("News News News");
		item.setDescription("The News");
		item.setThumbnail("thumbnail.image");
		item.setVideoSource("videoSource");
		
		item.setLocationUris(Lists.newArrayList("http://blip.tv/1.flv", "http://blip.tv/2.mov"));
	}
	
	@SuppressWarnings("unchecked")
	public void testCreatesEpisodesFromFeedEntries() throws Exception {
		
		Item item = extractor.extract(source);

		assertThat(item.getCanonicalUri(), is(ITEM_URI));
		
		assertThat(item.getTitle(), is("News News News"));
		assertThat(item.getDescription(), is("The News"));
		assertThat(item.getPublisher(), is(Publisher.BLIP));
		assertThat(item.getCurie(), is("blip:2276955"));
		assertThat(item.getThumbnail(), is("thumbnail.image"));

		Version version = Iterables.getOnlyElement(item.getVersions());
		
		EncodingMatcher encoding1 = encodingMatcher()
			.withDataContainerFormat(is(MimeType.VIDEO_XFLV))
			.withLocations(Matchers.<Location>hasItems(
					locationMatcher()
						.withUri(is("http://blip.tv/1.flv"))
						.withTransportType(is(TransportType.DOWNLOAD)),
					locationMatcher()
						.withUri(is(ITEM_URI))
						.withTransportType(is(TransportType.LINK)),
					locationMatcher()
						.withTransportType(is(TransportType.EMBED))));
		
		EncodingMatcher encoding2 = encodingMatcher()
			.withDataContainerFormat(is(MimeType.VIDEO_QUICKTIME))
			.withLocations(Matchers.<Location>hasItems(
					locationMatcher()
						.withTransportType(is(TransportType.DOWNLOAD))
						.withUri(is("http://blip.tv/2.mov"))));
		
		assertThat(version.getManifestedAs(), Matchers.<Encoding>hasItems(encoding1, encoding2));
	}

}
