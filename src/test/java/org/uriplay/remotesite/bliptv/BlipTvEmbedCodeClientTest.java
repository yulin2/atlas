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


package org.uriplay.remotesite.bliptv;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.jherd.remotesite.http.RemoteSiteClient;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Robert Chatley (robert@metabroadcast.com)
 */
@SuppressWarnings("unchecked")
public class BlipTvEmbedCodeClientTest extends MockObjectTestCase {

	String VIDEO_SOURCE_URI = "http://e.blip.tv/scripts/flash/showplayer.swf?file=http://blip.tv/rss/flash/2289908";
	String EMBED_CODE_SOURCE_URI = "http://blip.tv/players/embed/?posts_id=2289908&players_id=-1&skin=json&callback=DoSomethingActions.playerSelector.gotEmbedCode";
	
	RemoteSiteClient<Reader> httpClient = mock(RemoteSiteClient.class);
	
	public void testTheClient() throws Exception {
		
		checking(new Expectations() {{ 
			one(httpClient).get(EMBED_CODE_SOURCE_URI); will(returnValue(embedCodeJson()));
		}});
		
		BlipTvEmbedCodeClient client = new BlipTvEmbedCodeClient(httpClient);
		
		String embedCode = client.get(VIDEO_SOURCE_URI);
		
		assertThat(embedCode, is("<embed src=\"http://blip.tv/play/AYGS_jcA\" type=\"application/x-shockwave-flash\" width=\"384\" height=\"318\" allowscriptaccess=\"always\" allowfullscreen=\"true\"></embed>"));
	}
	
	protected Reader embedCodeJson() throws IOException {
		return new InputStreamReader(new ClassPathResource("blip-tv-item-embed-code.json").getInputStream());
	}
}
