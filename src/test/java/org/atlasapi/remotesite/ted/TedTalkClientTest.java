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

package org.atlasapi.remotesite.ted;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.atlasapi.remotesite.html.HtmlDescriptionOfItem;
import org.atlasapi.remotesite.ted.TedTalkClient;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.springframework.core.io.ClassPathResource;

import com.metabroadcast.common.http.SimpleHttpClient;

/**
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class TedTalkClientTest extends MockObjectTestCase {

	String URI = "/uri";
	
	private final SimpleHttpClient httpClient = mock(SimpleHttpClient.class);
	
	public void testTheClient() throws Exception {
		
		checking(new Expectations() {{ 
			one(httpClient).getContentsOf(URI); will(returnValue(itemHtml()));
		}});
		
		TedTalkClient client = new TedTalkClient(httpClient);
		
		HtmlDescriptionOfItem item = client.get(URI);
		
		assertThat(item.getTitle(), startsWith("Ray Kurzweil on how technology will transform us"));
		assertThat(item.getDescription(), startsWith("TED Talks Inventor, entrepreneur and visionary Ray Kurzweil"));
	
		assertThat(item.getVideoSource(), is("http://www.ted.com/talks/download/video/5067/talk/38"));
		
		assertThat(item.getFlashFile(), is("http://video.ted.com/talks/dynamic/RayKurzweil_2005-high.flv"));
		assertThat(item.getThumbnail(), is("http://images.ted.com/images/ted/193_389x292.jpg"));
		
	}
	
	protected String itemHtml() throws IOException {
		return IOUtils.toString(new ClassPathResource("ted-talk-page.html").getInputStream());
	}

}
