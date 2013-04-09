package org.atlasapi.remotesite.redux;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.Map;

import org.atlasapi.remotesite.redux.model.BaseReduxProgramme;
import org.atlasapi.remotesite.redux.model.FullReduxProgramme;
import org.atlasapi.remotesite.redux.model.PaginatedBaseProgrammes;
import org.atlasapi.remotesite.redux.model.ReduxMedia;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.greaterThan;

import com.google.common.net.HostSpecifier;
import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.query.Selection;

public class ReduxClientTest {

	public static final String exampleDiskref = "5864860735858853248";
	public static final String TEST_REDUX_HOST = "devapi.bbcredux.com";
	
	private HttpBackedReduxClient reduxClient;
	
	@Before
	public void setup() throws ParseException {
		reduxClient = HttpBackedReduxClient.reduxClientForHost(HostSpecifier.from(TEST_REDUX_HOST)).build();
	}
	
	@Test
	public void testCanGetLatest() throws HttpException, Exception {
		PaginatedBaseProgrammes pbp = reduxClient.latest(Selection.ALL);
		int first = pbp.getFirst();
		int last = pbp.getLast();
		assertThat(first, lessThan(last));
		BaseReduxProgramme programme = pbp.getResults().get(0);
		assertNotNull(programme.getDiskref());
		assertNotNull(programme.getTitle());
		System.out.println("Got programme "+programme.getTitle()+" : "+programme.getDiskref());
	}

	@Test
	public void testCanGetProgramme() throws HttpException, Exception {
		FullReduxProgramme programme = reduxClient.programmeFor(exampleDiskref);
		assertThat(programme.getTitle(), is("The Dare Devil"));
		Map<String, ReduxMedia>mediaMap = programme.getMedia();
		assertNotNull("Media map should not be null", mediaMap);
		assertThat(mediaMap.size(), greaterThan(0));
	}
	
	@Test
	public void testCachesMediaTypes() throws HttpException, Exception {
		Map<String, ReduxMedia>mediaMap = reduxClient.cacheMediaFormats();
		assertNotNull("Media map should not be null", mediaMap);
		assertThat(mediaMap.size(), greaterThan(0));
		for(String key : mediaMap.keySet()) {
			System.out.println("Key "+key+" gets "+mediaMap.get(key));
		}
	}
}
