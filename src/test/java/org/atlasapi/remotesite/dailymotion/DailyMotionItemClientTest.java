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

package org.atlasapi.remotesite.dailymotion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.atlasapi.remotesite.html.HtmlDescriptionOfItem;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.springframework.core.io.ClassPathResource;

import com.google.common.collect.Lists;
import com.metabroadcast.common.http.SimpleHttpClient;

public class DailyMotionItemClientTest extends MockObjectTestCase {

	private String URI = "/uri";
	
	private SimpleHttpClient httpClient = mock(SimpleHttpClient.class);
	
	public void testTheClient() throws Exception {
		
		checking(new Expectations() {{ 
			one(httpClient).getContentsOf(URI); will(returnValue(itemHtml()));
		}});
		
		DailyMotionItemClient client = new DailyMotionItemClient(httpClient);
		
		HtmlDescriptionOfItem item = client.get(URI);
		
		assertThat(item.getTitle(), startsWith("Dailymotion - Protesting Iran's election"));
		assertThat(item.getDescription(), startsWith("Zain Verjee reports on demonstrations taking place in London and Tehran"));
		assertThat(item.getKeywords(), is((List<String>) Lists.newArrayList("CNN", "news")));
		assertThat(item.getThumbnail(), is("http://www.dailymotion.com/thumbnail/160x120/video/x9l8e7_protesting-irans-election_news"));
	
		assertThat(item.getVideoSource(), is("http://www.dailymotion.com/swf/x9l8e7"));
		
	}
	
	protected String itemHtml() throws IOException {
		return IOUtils.toString(new ClassPathResource("daily-motion-item.html").getInputStream());
	}

}
