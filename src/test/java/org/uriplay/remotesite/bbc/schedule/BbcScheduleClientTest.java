package org.uriplay.remotesite.bbc.schedule;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import org.jherd.remotesite.http.RemoteSiteClient;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.springframework.core.io.ClassPathResource;
import org.uriplay.remotesite.bbc.schedule.ChannelSchedule.Programme;

/**
 * @author Robert Chatley (robert@metabroadcast.com)
 */
@SuppressWarnings("unchecked")
public class BbcScheduleClientTest extends MockObjectTestCase {
	
	String URI = "http://www.bbc.co.uk/bbctwo/programmes/schedules/england/2009/11/05.xml";
	
	RemoteSiteClient<Reader> httpClient = mock(RemoteSiteClient.class);

	public void testBindsRetrievedXmlDocumentToObjectModel() throws Exception {
		
		checking(new Expectations() {{ 
			one(httpClient).get(URI); will(returnValue(xmlDocument()));
		}});
		
		ChannelSchedule result = new BbcScheduleClient(httpClient).get(URI);
		
		List<Programme> programmes = result.programmes();
		
		assertThat(programmes.size(), is(1));
		Programme programme = programmes.get(0);
		assertThat(programme.pid(), is("b00ntcgg"));
		assertTrue(programme.isEpisode());

	}

	protected Reader xmlDocument() throws IOException {
		return new InputStreamReader(new ClassPathResource("bbc-schedule.xml").getInputStream());
	}

}
