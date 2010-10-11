package org.atlasapi.query.content;

import java.util.List;

import junit.framework.TestCase;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.content.criteria.attribute.Attributes;
import org.atlasapi.content.criteria.operator.Operators;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Playlist;
import org.atlasapi.media.entity.Publisher;
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
}
