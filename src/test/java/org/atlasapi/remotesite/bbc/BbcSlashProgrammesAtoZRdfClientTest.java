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

import org.apache.commons.io.IOUtils;
import org.atlasapi.remotesite.bbc.atoz.BbcSlashProgrammesAtoZRdfClient;
import org.atlasapi.remotesite.bbc.atoz.SlashProgrammesAtoZRdf;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.springframework.core.io.ClassPathResource;

import com.metabroadcast.common.http.SimpleHttpClient;

/**
 * Unit test for {@link BbcSlashProgrammesAtoZRdfClient}.
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class BbcSlashProgrammesAtoZRdfClientTest extends MockObjectTestCase {
	
	private static final String URI = "http://example.com";
	
	private final SimpleHttpClient httpClient = mock(SimpleHttpClient.class);

	public void testBindsRetrievedXmlDocumentToObjectModel() throws Exception {
		
		checking(new Expectations() {{ 
			one(httpClient).getContentsOf(URI); will(returnValue(xmlDocument()));
		}});
		
		SlashProgrammesAtoZRdf atoz = new BbcSlashProgrammesAtoZRdfClient(httpClient).get(URI);
		assertNotNull(atoz);
		assertNotNull(atoz.programmeIds());
		assertFalse(atoz.programmeIds().isEmpty());
		for (String programme: atoz.programmeIds()) {
		    assertNotNull(programme);
		}
	}

	protected String xmlDocument() throws IOException {
		return IOUtils.toString(new ClassPathResource("all.rdf.xml").getInputStream());
	}
}
