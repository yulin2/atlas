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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.atlasapi.remotesite.bbc.BbcSlashProgrammesEpisodeRdfClient;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesContainerRef;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesEpisode;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.springframework.core.io.ClassPathResource;

import com.google.common.collect.Iterables;
import com.metabroadcast.common.http.SimpleHttpClient;

/**
 * Unit test for {@link BbcSlashProgrammesEpisodeRdfClient}.
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class BbcSlashProgrammesEpisodeRdfClientTest extends MockObjectTestCase {
	
	private static final String URI = "http://example.com";
	
	private final SimpleHttpClient httpClient = mock(SimpleHttpClient.class);

	public void testBindsRetrievedXmlDocumentToObjectModel() throws Exception {
		
		checking(new Expectations() {{ 
			one(httpClient).getContentsOf(URI); will(returnValue(xmlDocument()));
		}});
		
		SlashProgrammesRdf description = new BbcSlashProgrammesEpisodeRdfClient(httpClient).get(URI);
		
		SlashProgrammesContainerRef brand = description.brand();
		assertThat(brand.uri(), is("http://www.bbc.co.uk/programmes/b006mj59"));
		
		
		SlashProgrammesEpisode ep = description.episode();
		assertThat(ep.title(), is("Episode 6"));
		assertThat(ep.description(), startsWith("Motoring news and views from the usual team."));
		assertThat(ep.episodeNumber(), is(6));
		assertThat(ep.genres().size(), is(1));
		assertThat(Iterables.getOnlyElement(ep.genres()).resourceUri(), is("/programmes/genres/factual/carsandmotors#genre"));
		
		assertThat(ep.versions().size(), is(1));
		assertThat(ep.versions().get(0).resourceUri(), is("/programmes/b00g2ggy#programme"));

	}

	protected String xmlDocument() throws IOException {
		return IOUtils.toString(new ClassPathResource("top-gear-rdf.xml").getInputStream());
	}
}
