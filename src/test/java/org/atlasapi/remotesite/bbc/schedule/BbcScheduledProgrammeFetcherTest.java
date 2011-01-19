package org.atlasapi.remotesite.bbc.schedule;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.DefinitiveContentWriter;
import org.atlasapi.persistence.logging.NullAdapterLog;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.bbc.BbcProgrammeAdapter;
import org.atlasapi.remotesite.bbc.schedule.ChannelSchedule.Programme;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;

import com.google.common.collect.Lists;

@SuppressWarnings("unchecked")
public class BbcScheduledProgrammeFetcherTest extends MockObjectTestCase {

	RemoteSiteClient<ChannelSchedule> scheduleClient = mock(RemoteSiteClient.class);
	BbcProgrammeAdapter fetcher = new BbcProgrammeAdapter(new NullAdapterLog());
	ContentResolver localFetcher = mock(ContentResolver.class);
	DefinitiveContentWriter writer = mock(DefinitiveContentWriter.class);
	
	ChannelSchedule schedule = new ChannelSchedule();
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		schedule.withProgrammes(Lists.newArrayList(new Programme("episode", "b00abcd"), new Programme("episode", "b00efgh"), new Programme("brand", "b00xyz")));
	}
	
	public void testFetchExtractedEpisodePid() throws Exception {
	    
//	    final Episode containedInBrand = new Episode("containedInBrandUri", "containedInBrandCurie", Publisher.BBC);
//        
//	    final Brand brand = new Brand("brandUri", "brandCurie", Publisher.BBC);
//        brand.addItem(containedInBrand);
//        
//        final Episode replacingEpisode = new Episode("containedInBrandUri", "replacingCurie", Publisher.BBC);
//        replacingEpisode.setBrand(brand);
//        
//        final Episode withoutBrand = new Episode("episodeWithoutBrandUri", "episodeWithoutBrandCurie", Publisher.BBC);
//		
//		BbcScheduledProgrammeUpdater scheduleFetcher = new BbcScheduledProgrammeUpdater(scheduleClient, localFetcher, fetcher, writer, Lists.newArrayList("http://www.bbc.co.uk/bbctwo/programmes/schedules/england/2009/11/05.xml"), new NullAdapterLog());
//		
//		checking(new Expectations() {{ 
//			one(scheduleClient).get("http://www.bbc.co.uk/bbctwo/programmes/schedules/england/2009/11/05.xml"); will(returnValue(schedule));
////			one(fetcher).fetch("http://www.bbc.co.uk/programmes/b00abcd"); will(returnValue(replacingEpisode));
////			one(fetcher).fetch("http://www.bbc.co.uk/programmes/b00efgh"); will(returnValue(withoutBrand));
//			one(localFetcher).findByUri("brandUri"); will(returnValue(brand));
//			one(writer).createOrUpdateDefinitivePlaylist(with(any(Brand.class)));
//			one(writer).createOrUpdateDefinitiveItem(with(any(Item.class)));
//		}});
//
//		scheduleFetcher.run();
//		
//		assertThat(brand.getItems().size(), is(equalTo(1)));
//		assertThat(brand.getItems().get(0).getCurie(), is(equalTo("replacingCurie")));
	}
}
