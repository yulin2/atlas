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
import static org.hamcrest.Matchers.startsWith;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.jherd.remotesite.http.RemoteSiteClient;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.springframework.core.io.ClassPathResource;
import org.uriplay.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesContainerRef;
import org.uriplay.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesEpisode;

import com.google.common.collect.Iterables;

/**
 * Unit test for {@link BbcSlashProgrammesEpisodeRdfClient}.
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
@SuppressWarnings("unchecked")
public class BbcSlashProgrammesEpisodeRdfClientTest extends MockObjectTestCase {
	
	String URI = "http://example.com";
	
	RemoteSiteClient<Reader> httpClient = mock(RemoteSiteClient.class);

	public void testBindsRetrievedXmlDocumentToObjectModel() throws Exception {
		
		checking(new Expectations() {{ 
			one(httpClient).get(URI); will(returnValue(xmlDocument()));
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

	protected Reader xmlDocument() throws IOException {
		return new InputStreamReader(new ClassPathResource("top-gear-rdf.xml").getInputStream());
	}
}
