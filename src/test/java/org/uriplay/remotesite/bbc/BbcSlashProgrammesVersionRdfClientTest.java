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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.springframework.core.io.ClassPathResource;
import org.uriplay.persistence.system.RemoteSiteClient;
import org.uriplay.remotesite.bbc.SlashProgrammesVersionRdf.BbcBroadcast;

/**
 * Unit test for {@link BbcSlashProgrammesVersionRdfClient}.
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
@SuppressWarnings("unchecked")
public class BbcSlashProgrammesVersionRdfClientTest extends MockObjectTestCase {
	
	String URI = "http://example.com";
	
	RemoteSiteClient<Reader> httpClient = mock(RemoteSiteClient.class);

	public void testBindsRetrievedXmlDocumentToObjectModel() throws Exception {
		
		checking(new Expectations() {{ 
			one(httpClient).get(URI); will(returnValue(xmlDocument()));
		}});
		
		SlashProgrammesVersionRdf version = new BbcSlashProgrammesVersionRdfClient(httpClient).get(URI);
		
		assertThat(version.firstBroadcastSlots().size(), is(10));
		assertThat(version.repeatBroadcastSlots().size(), is(8));
		
		BbcBroadcast firstBroadcast = version.firstBroadcastSlots().get(0);
		assertThat(firstBroadcast.broadcastTime(), is("2007-11-25T20:00:00Z"));
		assertThat(firstBroadcast.broadcastOn(), is("/bbctwo#service"));
		assertThat(firstBroadcast.broadcastDuration(), is(3600));
		assertThat(version.firstBroadcastSlots().get(0).scheduleDate(), is("2007-11-25"));
	}

	protected Reader xmlDocument() throws IOException {
		return new InputStreamReader(new ClassPathResource("top-gear-version-rdf.xml").getInputStream());
	}

}
