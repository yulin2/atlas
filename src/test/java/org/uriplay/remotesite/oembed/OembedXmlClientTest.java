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

package org.uriplay.remotesite.oembed;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.springframework.core.io.ClassPathResource;
import org.uriplay.feeds.OembedItem;

import com.metabroadcast.common.http.SimpleHttpClient;

/**
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class OembedXmlClientTest extends MockObjectTestCase {
	
	private final String URI = "http://example.com";
	
	private final SimpleHttpClient httpClient = mock(SimpleHttpClient.class);

	public void testBindsRetrievedXmlDocumentToObjectModel() throws Exception {
		
		checking(new Expectations() {{ 
			one(httpClient).getContentsOf(URI); will(returnValue(xmlDocument()));
		}});
		
		OembedItem oembed = new OembedXmlClient(httpClient).get(URI);
		
		assertThat(oembed.title(), is("Meet the office"));
		assertThat(oembed.height(), is(380));
		assertThat(oembed.width(), is(504));
		assertThat(oembed.providerUrl(), is("http://vimeo.com/"));
		assertThat(oembed.thumbnailUrl(), is("http://90.media.vimeo.com/d1/5/38/21/85/thumbnail-38218529.jpg"));
		assertThat(oembed.embedCode(), startsWith("<object"));
	}

	protected String xmlDocument() throws IOException {
		return IOUtils.toString(new ClassPathResource("vimeo-oembed.xml").getInputStream());
	}

}
