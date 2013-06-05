package org.atlasapi.remotesite.bbc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesDescription;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesSameAs;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;

import com.google.common.collect.Iterables;
import com.metabroadcast.common.http.FixedResponseHttpClient;
import com.metabroadcast.common.http.SimpleHttpClient;

public class BbcSlashProgrammesTopicRdfClientTest extends TestCase {

	private static final String URI = "http://example.com";
	
	private static final SimpleHttpClient httpClient = new FixedResponseHttpClient(URI, xmlDocument());

    @Test
	public void testBindsRetrievedXmlDocumentToObjectModel() throws Exception {
		
		SlashProgrammesRdf description = new BbcSlashProgrammesRdfClient<SlashProgrammesRdf>(httpClient, SlashProgrammesRdf.class).get(URI);
		
		SlashProgrammesDescription desc = description.description();
		assertThat(desc, is(notNullValue()));
		
		SlashProgrammesSameAs sameAs = Iterables.getOnlyElement(desc.getSameAs());
		assertThat(sameAs.resourceUri(), is("http://dbpedia.org/resource/Brighton"));

	}

	private static String xmlDocument()  {
		try {
            return IOUtils.toString(new ClassPathResource("brighton-rdf.xml").getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	}
}
