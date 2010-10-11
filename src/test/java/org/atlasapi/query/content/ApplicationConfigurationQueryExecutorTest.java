package org.atlasapi.query.content;

import java.util.List;

import junit.framework.TestCase;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.content.criteria.attribute.Attributes;
import org.atlasapi.content.criteria.operator.Operators;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Playlist;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.mongo.MongoDBQueryExecutor;
import org.atlasapi.persistence.content.mongo.MongoDbBackedContentStore;
import org.atlasapi.persistence.content.mongo.MongoRoughSearch;
import org.atlasapi.persistence.testing.DummyContentData;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.persistence.MongoTestHelper;

public class ApplicationConfigurationQueryExecutorTest extends TestCase {

    private DummyContentData data = new DummyContentData();

    private MongoDbBackedContentStore store;
    private MongoDBQueryExecutor mongoQueryExecutor;
    
    private ApplicationConfigurationQueryExecutor queryExecutor;

    @Override
    protected void setUp() throws Exception {
    	super.setUp();
    	
    	store = new MongoDbBackedContentStore(MongoTestHelper.anEmptyTestDatabase());
    	mongoQueryExecutor = new MongoDBQueryExecutor(new MongoRoughSearch(store));
    	
    	store.createOrUpdatePlaylist(data.eastenders, true);
    	store.createOrUpdatePlaylist(data.apprentice, true);
    	store.createOrUpdatePlaylist(data.newsNight, true);
    	store.createOrUpdatePlaylist(data.ER, true);
    	
    	store.createOrUpdateItem(data.englishForCats);
    	store.createOrUpdateItem(data.eggsForBreakfast);
    	store.createOrUpdateItem(data.everyoneNeedsAnEel);
    	
    	store.createOrUpdatePlaylist(data.goodEastendersEpisodes, false);
    	store.createOrUpdatePlaylist(data.mentionedOnTwitter, false);
    	
    	queryExecutor = new ApplicationConfigurationQueryExecutor(mongoQueryExecutor);
    }

	public void testExecuteItemQuery() {
		ApplicationConfiguration config = new ApplicationConfiguration();
		config.setIncludedPublishers(ImmutableSet.of(Publisher.BBC));
		
		ContentQuery query = ContentQuery.MATCHES_EVERYTHING.copyWithApplicationConfiguration(config);
		
		List<Item> results = queryExecutor.executeItemQuery(query);
		
		assertTrue( results.size() > 0);
		for (Item item : results) {
			assertEquals(Publisher.BBC, item.getPublisher());
		}
	}

	public void testExecutePlaylistQuery() {
		ApplicationConfiguration config = new ApplicationConfiguration();
		config.setIncludedPublishers(ImmutableSet.of(Publisher.BBC));
		
		ContentQuery query = new ContentQuery(Attributes.PLAYLIST_TITLE.createQuery(Operators.EQUALS, ImmutableList.of("EastEnders: the best bits"))).copyWithApplicationConfiguration(config);
		
		List<Playlist> results = queryExecutor.executePlaylistQuery(query);
		
		assertTrue( results.size() > 0);
		for (Playlist playlist : results) {
			assertEquals(Publisher.BBC, playlist.getPublisher());
		}
	}

	public void testExecuteBrandQuery() {
		ApplicationConfiguration config = new ApplicationConfiguration();
		config.setIncludedPublishers(ImmutableSet.of(Publisher.BBC));
		
		ContentQuery query = ContentQuery.MATCHES_EVERYTHING.copyWithApplicationConfiguration(config);
		
		List<Brand> results = queryExecutor.executeBrandQuery(query);
		
		assertTrue( results.size() > 0);
		for (Brand brand : results) {
			assertEquals(Publisher.BBC, brand.getPublisher());
		}
	}
	
	public void testAllVersionAreOfRightPublisher() {
		ApplicationConfiguration config = new ApplicationConfiguration();
		config.setIncludedPublishers(ImmutableSet.of(Publisher.C4));
		
		Episode uglyBettyC4 = new Episode("http://www.channel4.com/uglybetty/one", "c4:ugly-betty-one", Publisher.C4);
		uglyBettyC4.setTitle("Ugly Betty Episode One");
		Version c4Version = new Version();
		c4Version.setProvider(Publisher.C4);
		Version huluVersion = new Version();
		huluVersion.setProvider(Publisher.HULU);
		uglyBettyC4.setVersions(ImmutableSet.of(c4Version, huluVersion));
		
		Playlist ubC4 = new Brand("http://www.channel4.com/uglybetty", "c4:ugly-betty", Publisher.C4);
		ubC4.setDescription("blah blah blah");
		ubC4.setTitle("Ugly Betty");
		ubC4.addItem(uglyBettyC4);
		
		store.createOrUpdatePlaylist(ubC4, true);
		
		store.createOrUpdateItem(uglyBettyC4);
		
		ContentQuery query = ContentQuery.MATCHES_EVERYTHING.copyWithApplicationConfiguration(config);
		
		List<Item> results = queryExecutor.executeItemQuery(query);
		
		assertTrue( results.size() > 0);
		for (Item item : results) {
			assertEquals(Publisher.C4, item.getPublisher());
			for (Version version : item.getVersions()) {
				assertEquals(Publisher.C4, version.getProvider());
			}
		}
	}
}
