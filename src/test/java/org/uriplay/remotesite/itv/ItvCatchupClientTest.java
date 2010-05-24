package org.uriplay.remotesite.itv;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.springframework.core.io.ClassPathResource;
import org.uriplay.persistence.system.RemoteSiteClient;

@SuppressWarnings("unchecked")
public class ItvCatchupClientTest extends MockObjectTestCase {

	static String CATCHUP_URI = "http://example.com";
	static String SERIES_FRAGMENT = "http://www.itv.com/_app/Dynamic/CatchUpData.ashx?ViewType=1&Filter=972&moduleID=262033&columnWidth=2";
	
	RemoteSiteClient<Reader> httpClient = mock(RemoteSiteClient.class);

	public void testBindsRetrievedXmlDocumentToObjectModel() throws Exception {
		
		checking(new Expectations() {{ 
			one(httpClient).get(CATCHUP_URI); will(returnValue(itvCatchupXmlDocument()));
			one(httpClient).get(SERIES_FRAGMENT); will(returnValue(itvSeriesHtmlFragment()));
		}});
		
		List<ItvProgramme> programmes = new ItvCatchupClient(httpClient).get(CATCHUP_URI);

		ItvProgramme programme = programmes.get(0);
		
		assertThat(programme.title(), is("A Touch of Frost"));
		assertThat(programme.thumbnail(), is("http://www.itv.com//img/150x113/A-Touch-of-Frost-2054dae8-cf18-4ebb-9b4d-bb16c0139602.jpg"));
		assertThat(programme.programmeId(), is(972));
		
		assertThat(programme.episodes().size(), is(2));
		ItvEpisode firstEpisode = programme.episodes().get(0);
		ItvEpisode secondEpisode = programme.episodes().get(1);
		
		assertThat(firstEpisode.url(), is("http://www.itv.com/ITVPlayer/Video/default.html?ViewType=5&Filter=46505"));
		assertThat(firstEpisode.date(), is("Tue 11 Aug 2009"));
		
		assertThat(secondEpisode.url(), is("http://www.itv.com/ITVPlayer/Video/default.html?ViewType=5&Filter=46306"));
	}
	
	protected Reader itvCatchupXmlDocument() throws IOException {
		return new InputStreamReader(new ClassPathResource("itv-catchup.xml").getInputStream());
	}
	
	protected Reader itvSeriesHtmlFragment() throws IOException {
		return new InputStreamReader(new ClassPathResource("frost.html").getInputStream());
	}
}
