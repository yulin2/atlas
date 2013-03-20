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

package org.atlasapi.remotesite.bbc;

import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.atlasapi.remotesite.bbc.atoz.BbcSlashProgrammesAtoZRdfClient;
import org.atlasapi.remotesite.bbc.atoz.SlashProgrammesAtoZRdf;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;

import com.google.common.io.Resources;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.http.SimpleHttpRequest;

/**
 * Unit test for {@link BbcSlashProgrammesAtoZRdfClient}.
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
@RunWith(JMock.class)
public class BbcSlashProgrammesAtoZRdfClientTest extends TestCase {
	
	private static final String URI = "http://example.com";

    private final Mockery context = new Mockery();
	private final SimpleHttpClient httpClient = context.mock(SimpleHttpClient.class);

	@Test
	public void testBindsRetrievedXmlDocumentToObjectModel() throws Exception {
		
		context.checking(new Expectations() {{ 
			one(httpClient).get(with(request(URI))); will(returnValue(xmlDocument()));
		}});
		
		SlashProgrammesAtoZRdf atoz = new BbcSlashProgrammesAtoZRdfClient(httpClient).get(URI);
		assertNotNull(atoz);
		assertNotNull(atoz.programmeIds());
		assertFalse(atoz.programmeIds().isEmpty());
		for (String programme: atoz.programmeIds()) {
		    assertNotNull(programme);
		}
	}

	protected SlashProgrammesAtoZRdf xmlDocument() throws Exception {
        return (SlashProgrammesAtoZRdf) JAXBContext.newInstance(SlashProgrammesAtoZRdf.class)
                .createUnmarshaller()
                .unmarshal(Resources.getResource("all.rdf.xml"));
	}
	
	private Matcher<SimpleHttpRequest<?>> request(final String uri) {
	    return new TypeSafeMatcher<SimpleHttpRequest<?>>() {

            @Override
            public void describeTo(Description desc) {
                desc.appendText("request with uri " + uri);
            }

            @Override
            public boolean matchesSafely(SimpleHttpRequest<?> req) {
                return req.getUrl().equals(uri);
            }
        };
	}
}
