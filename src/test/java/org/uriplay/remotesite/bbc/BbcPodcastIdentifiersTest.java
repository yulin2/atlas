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

package org.uriplay.remotesite.bbc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import junit.framework.TestCase;

import com.sun.syndication.feed.synd.SyndEntryImpl;

/**
 * @author Robert Chatley (robert@metabroadcast.com)
 * @author Chris Jackson
 */
public class BbcPodcastIdentifiersTest extends TestCase {

	@SuppressWarnings("serial")
	BbcPodcastIdentifiers identifiers = new BbcPodcastIdentifiers(
			new SyndEntryImpl() {{ setLink("http://downloads.bbc.co.uk/podcasts/radio4/bh/bh_20090125-0900.mp3"); }});

	public void testGeneratesEpisodeUri() throws Exception {
		
		assertThat(identifiers.episodeUri(), is("http://uriplay.org/bh/bh_20090125-0900"));
	}
	
	public void testGeneratesVersionUri() throws Exception {
		
		assertThat(identifiers.versionUri(), is("http://uriplay.org/bh/bh_20090125-0900/main"));
	}
	
	public void testGeneratesEncodingUri() throws Exception {
		
		assertThat(identifiers.encodingUri(), is("http://uriplay.org/bh/bh_20090125-0900/main/main"));
	}
	
	@SuppressWarnings("serial")
	public void testMoyles() throws Exception {
		
		BbcPodcastIdentifiers moyles = new BbcPodcastIdentifiers(
				new SyndEntryImpl() {{ setLink("http://downloads.bbc.co.uk/podcasts/radio1/moylesen/moylesen_20090130-0630.m4a"); }});
		
		assertThat(moyles.episodeUri(), is("http://uriplay.org/moylesen/moylesen_20090130-0630"));

	}
}
