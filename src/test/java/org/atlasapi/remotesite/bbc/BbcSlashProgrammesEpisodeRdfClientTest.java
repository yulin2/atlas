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
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesContainerRef;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesDescription;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesEpisode;
import org.springframework.core.io.ClassPathResource;

import com.google.common.collect.Iterables;
import com.metabroadcast.common.http.FixedResponseHttpClient;
import com.metabroadcast.common.http.SimpleHttpClient;

/**
 * Unit test for {@link BbcSlashProgrammesRdfClient}.
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class BbcSlashProgrammesEpisodeRdfClientTest extends TestCase {
	
	private static final String URI = "http://example.com";
	
	private static final SimpleHttpClient topGearHttpClient = new FixedResponseHttpClient(URI, topGearXmlDocument());
	private static final SimpleHttpClient brightonHttpClient = new FixedResponseHttpClient(URI, brightonXmlDocument());

	public void testBindsRetrievedXmlDocumentToObjectModel() throws Exception {
		
		SlashProgrammesRdf description = new BbcSlashProgrammesRdfClient<SlashProgrammesRdf>(topGearHttpClient, SlashProgrammesRdf.class).get(URI);
		
		SlashProgrammesDescription desc = description.description();
		assertThat(desc, is(notNullValue()));
		
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

	private static String topGearXmlDocument()  {
		try {
            return IOUtils.toString(new ClassPathResource("top-gear-rdf.xml").getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	}
	
    public void testBindsRetrievedXmlDocumentToObjectModelWithPersonPlaceAndThing() throws Exception {

        SlashProgrammesRdf description = new BbcSlashProgrammesRdfClient<SlashProgrammesRdf>(brightonHttpClient, SlashProgrammesRdf.class).get(URI);

        SlashProgrammesDescription desc = description.description();
        assertThat(desc, is(notNullValue()));

        SlashProgrammesContainerRef brand = description.brand();
        assertThat(brand.uri(), is("http://www.bbc.co.uk/programmes/b006qjds"));

        SlashProgrammesEpisode ep = description.episode();
        assertThat(ep.title(), is("Bus trip from Brighton to Eastbourne"));
        assertThat(ep.description(), startsWith("Sandi Toksvig takes a bus"));
        assertThat(ep.genres().size(), is(1));
        assertThat(Iterables.getOnlyElement(ep.genres()).resourceUri(), is("/programmes/genres/factual/travel#genre"));

        assertThat(ep.versions().size(), is(1));
        assertThat(ep.versions().get(0).resourceUri(), is("/programmes/b0144nxy#programme"));
        
        assertThat(Iterables.getOnlyElement(ep.subjects()).resourceUri(), is("/programmes/topics/public_transport#subject"));
        assertThat(Iterables.getOnlyElement(ep.people()).resourceUri(), is("/programmes/topics/sandi_toksvig#person"));
        assertThat(Iterables.get(ep.places(), 0).resourceUri(), isOneOf("/programmes/topics/brighton#place","/programmes/topics/eastbourne#place"));

    }

    private static String brightonXmlDocument() {
        try {
            return IOUtils.toString(new ClassPathResource("brighton-to-eastbourne-rdf.xml").getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
