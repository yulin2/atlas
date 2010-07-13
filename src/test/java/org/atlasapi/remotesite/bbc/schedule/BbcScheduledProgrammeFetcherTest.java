package org.atlasapi.remotesite.bbc.schedule;

import org.atlasapi.media.entity.Content;
import org.atlasapi.persistence.system.Fetcher;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.bbc.schedule.ChannelSchedule.Programme;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;

import com.google.common.collect.Lists;

@SuppressWarnings("unchecked")
public class BbcScheduledProgrammeFetcherTest extends MockObjectTestCase {

	RemoteSiteClient<ChannelSchedule> scheduleClient = mock(RemoteSiteClient.class);
	Fetcher<Content> fetcher = mock(Fetcher.class);
	
	ChannelSchedule schedule = new ChannelSchedule();
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		schedule.withProgrammes(Lists.newArrayList(new Programme("episode", "b00abcd"), new Programme("episode", "b00efgh"), new Programme("brand", "b00xyz")));
	}
	
	public void testFetchExtractedEpisodePid() throws Exception {
		
		BbcScheduledProgrammeUpdater scheduleFetcher = new BbcScheduledProgrammeUpdater(scheduleClient, fetcher);
		
		checking(new Expectations() {{ 
			one(scheduleClient).get("http://www.bbc.co.uk/bbctwo/programmes/schedules/england/2009/11/05.xml"); will(returnValue(schedule));
			one(fetcher).fetch("http://www.bbc.co.uk/programmes/b00abcd");
			one(fetcher).fetch("http://www.bbc.co.uk/programmes/b00efgh");
		}});
		
		scheduleFetcher.setUris(Lists.newArrayList("http://www.bbc.co.uk/bbctwo/programmes/schedules/england/2009/11/05.xml"));

		scheduleFetcher.run();
	}
}
