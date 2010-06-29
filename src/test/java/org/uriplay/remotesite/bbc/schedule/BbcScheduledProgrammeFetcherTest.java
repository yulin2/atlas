package org.uriplay.remotesite.bbc.schedule;

import java.util.Set;

import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.uriplay.persistence.system.Fetcher;
import org.uriplay.persistence.system.RemoteSiteClient;
import org.uriplay.remotesite.bbc.schedule.ChannelSchedule.Programme;

import com.google.common.collect.Lists;

@SuppressWarnings("unchecked")
public class BbcScheduledProgrammeFetcherTest extends MockObjectTestCase {

	RemoteSiteClient<ChannelSchedule> scheduleClient = mock(RemoteSiteClient.class);
	Fetcher<Set<Object>> uriplayFetcher = mock(Fetcher.class);
	
	ChannelSchedule schedule = new ChannelSchedule();
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		schedule.withProgrammes(Lists.newArrayList(new Programme("episode", "b00abcd"), new Programme("episode", "b00efgh"), new Programme("brand", "b00xyz")));
	}
	
	public void testMakesUriplayFetchExtractedEpisodePid() throws Exception {
		
		BbcScheduledProgrammeUpdater scheduleFetcher = new BbcScheduledProgrammeUpdater(scheduleClient, uriplayFetcher);
		
		checking(new Expectations() {{ 
			one(scheduleClient).get("http://www.bbc.co.uk/bbctwo/programmes/schedules/england/2009/11/05.xml"); will(returnValue(schedule));
			one(uriplayFetcher).fetch("http://www.bbc.co.uk/programmes/b00abcd");
			one(uriplayFetcher).fetch("http://www.bbc.co.uk/programmes/b00efgh");
		}});
		
		scheduleFetcher.setUris(Lists.newArrayList("http://www.bbc.co.uk/bbctwo/programmes/schedules/england/2009/11/05.xml"));

		scheduleFetcher.run();
	}
}
