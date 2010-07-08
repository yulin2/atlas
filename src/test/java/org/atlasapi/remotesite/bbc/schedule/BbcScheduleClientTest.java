package org.atlasapi.remotesite.bbc.schedule;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.atlasapi.remotesite.bbc.schedule.BbcScheduleClient;
import org.atlasapi.remotesite.bbc.schedule.ChannelSchedule;
import org.atlasapi.remotesite.bbc.schedule.ChannelSchedule.Programme;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.springframework.core.io.ClassPathResource;

import com.metabroadcast.common.http.SimpleHttpClient;

/**
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class BbcScheduleClientTest extends MockObjectTestCase {
	
	String URI = "http://www.bbc.co.uk/bbctwo/programmes/schedules/england/2009/11/05.xml";
	
	private SimpleHttpClient httpClient = mock(SimpleHttpClient.class);

	public void testBindsRetrievedXmlDocumentToObjectModel() throws Exception {
		
		checking(new Expectations() {{ 
			one(httpClient).getContentsOf(URI); will(returnValue(xmlDocument()));
		}});
		
		ChannelSchedule result = new BbcScheduleClient(httpClient).get(URI);
		
		List<Programme> programmes = result.programmes();
		
		assertThat(programmes.size(), is(1));
		Programme programme = programmes.get(0);
		assertThat(programme.pid(), is("b00ntcgg"));
		assertTrue(programme.isEpisode());

	}

	private String xmlDocument() throws IOException {
		return IOUtils.toString(new ClassPathResource("bbc-schedule.xml").getInputStream());
	}

}
