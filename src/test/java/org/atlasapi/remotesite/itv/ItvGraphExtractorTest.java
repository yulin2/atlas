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

package org.atlasapi.remotesite.itv;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.atlasapi.media.TransportType;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.Version;
import org.atlasapi.remotesite.itv.ItvBrandSource;
import org.atlasapi.remotesite.itv.ItvEpisode;
import org.atlasapi.remotesite.itv.ItvGraphExtractor;
import org.atlasapi.remotesite.itv.ItvProgramme;
import org.jmock.integration.junit3.MockObjectTestCase;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class ItvGraphExtractorTest extends MockObjectTestCase {
	
	
	static final String CATCHUP_URI = "http://www.itv.com/_data/xml/CatchUpData/CatchUp360/CatchUpMenu.xml";

	static final String BRAND_URI = "http://www.itv.com/ITVPlayer/Programmes/default.html?ViewType=1&Filter=2773";
	static final String EPISODE1_URI = "http://www.itv.com/ITVPlayer/Video/default.html?ViewType=5&Filter=100109";

	static final String THUMBNAIL_URL = "http://itv.com/images/a.jpg";

	ItvGraphExtractor extractor;

	ItvProgramme programme;

	ItvBrandSource source;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		programme = new ItvProgramme(BRAND_URI).withThumbnail(THUMBNAIL_URL);
		programme.addEpisode(new ItvEpisode("14 Aug", "latest episode", EPISODE1_URI));
		
		source = new ItvBrandSource(Lists.newArrayList(programme), CATCHUP_URI);
		
		extractor = new ItvGraphExtractor();
		
	}
	
	public void testCreatesEpisodesFromFeedEntries() throws Exception {
		
		Brand brand = Iterables.getOnlyElement(extractor.extract(source));

		assertThat(brand.getCanonicalUri(), is(BRAND_URI));
		assertThat(brand.getPublisher(), is("itv.com"));
		assertThat(brand.getCurie(), is("itv:1-2773"));

		Episode episode = (Episode) Iterables.getOnlyElement(brand.getItems());
		assertThat(episode.getCanonicalUri(), is(EPISODE1_URI));
		
		assertThat(episode.getDescription(), is("latest episode"));
		assertThat(episode.getPublisher(), is("itv.com"));
		assertThat(episode.getCurie(), is("itv:5-100109"));
		assertThat(episode.getIsLongForm(), is(true));
		assertThat(episode.getThumbnail(), is(THUMBNAIL_URL));
		
		Version version = Iterables.getOnlyElement(episode.getVersions());
		Encoding encoding = Iterables.getOnlyElement(version.getManifestedAs());
		Location location = Iterables.getOnlyElement(encoding.getAvailableAt());

		assertThat(location.getUri(), is(EPISODE1_URI));
		assertThat(location.getTransportType(), is(TransportType.LINK));
		assertThat(location.getAvailable(), is(true));
	}
}
