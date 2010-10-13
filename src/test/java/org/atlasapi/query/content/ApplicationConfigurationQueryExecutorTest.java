package org.atlasapi.query.content;

import java.util.List;

import junit.framework.TestCase;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.content.criteria.attribute.Attributes;
import org.atlasapi.content.criteria.operator.Operator;
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
import org.joda.time.Duration;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.persistence.MongoTestHelper;

public class ApplicationConfigurationQueryExecutorTest extends TestCase {

    private MongoDbBackedContentStore store;
    private MongoDBQueryExecutor mongoQueryExecutor;
    
    private ApplicationConfigurationQueryExecutor queryExecutor;

    @Override
    protected void setUp() throws Exception {
    	super.setUp();
    	
    	store = new MongoDbBackedContentStore(MongoTestHelper.anEmptyTestDatabase());
    	mongoQueryExecutor = new MongoDBQueryExecutor(new MongoRoughSearch(store));
    	
    	queryExecutor = new ApplicationConfigurationQueryExecutor(mongoQueryExecutor);
    }
	
    /* Config: C4
     * Item: C4 (no versions)
     * Result: C4 item (no versions)
     */
    public void testItemWithNoVersionsIsNotFiltered() {
		ApplicationConfiguration config = new ApplicationConfiguration();
		config.setIncludedPublishers(ImmutableSet.of(Publisher.C4));
		
		Episode uglyBettyC4 = new Episode("http://www.channel4.com/uglybetty/one", "c4:ugly-betty-one", Publisher.C4);
		uglyBettyC4.setTitle("Ugly Betty Episode One");
		
		Playlist ubC4 = new Brand("http://www.channel4.com/uglybetty", "c4:ugly-betty", Publisher.C4);
		ubC4.setDescription("blah blah blah");
		ubC4.setTitle("Ugly Betty");
		ubC4.addItem(uglyBettyC4);
		
		store.createOrUpdatePlaylist(ubC4, true);
		store.createOrUpdateItem(uglyBettyC4);
		
		ContentQuery query = ContentQuery.MATCHES_EVERYTHING.copyWithApplicationConfiguration(config);
		
		List<Item> results = queryExecutor.executeItemQuery(query);
		
		assertEquals( 1, results.size());
		for (Item item : results) {
			assertEquals(Publisher.C4, item.getPublisher());
			assertEquals(0, item.getVersions().size());
		}
    }
    
    
    /* Config: C4
     * Item: C4
     * 	versions: C4, Hulu
     * Result: C4 item with only C4 version
     */
	public void testOnlyVersionsWithConfiguredProvidersPassFilter() {
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
		
		assertEquals( 1, results.size());
		for (Item item : results) {
			assertEquals(Publisher.C4, item.getPublisher());
			for (Version version : item.getVersions()) {
				assertEquals(Publisher.C4, version.getProvider());
			}
		}
	}
	
    /* Config: C4
     * Item: C4
     * 	versions: BBC
     * Result: One item
     */
	public void testItemPassesFilterWhereAllVersionsFailFilterWhenQueryDoesntSpecifyVersionOrBelow() {
		ApplicationConfiguration config = new ApplicationConfiguration();
		config.setIncludedPublishers(ImmutableSet.of(Publisher.C4));
		
		Episode uglyBettyC4 = new Episode("http://www.channel4.com/uglybetty/one", "c4:ugly-betty-one", Publisher.C4);
		uglyBettyC4.setTitle("Ugly Betty Episode One");
		Version bbcVersion = new Version();
		bbcVersion.setProvider(Publisher.BBC);
		uglyBettyC4.addVersion(bbcVersion);
		
		store.createOrUpdateItem(uglyBettyC4);
		
		ContentQuery query = ContentQuery.MATCHES_EVERYTHING.copyWithApplicationConfiguration(config);
		
		List<Item> results = queryExecutor.executeItemQuery(query);
		
		assertEquals( 1, results.size() );
		assertEquals( 0, results.get(0).getVersions().size() );
		
		config.setIncludedPublishers(ImmutableSet.of(Publisher.C4, Publisher.BBC));
		query = query.copyWithApplicationConfiguration(config);
		
		results = queryExecutor.executeItemQuery(query);
		
		assertEquals( 1, results.size() );
		assertEquals( 1, results.get(0).getVersions().size() ); 
	}
	
    /* Config: C4
     * Item: C4
     * 	versions: BBC
     * Result: No items
     */
	public void testItemFailsFilterWhereAllVersionsFailFilterWhenQuerySpecifiesVersionOrBelow() {
		ApplicationConfiguration config = new ApplicationConfiguration();
		config.setIncludedPublishers(ImmutableSet.of(Publisher.C4));
		
		Episode uglyBettyC4 = new Episode("http://www.channel4.com/uglybetty/one", "c4:ugly-betty-one", Publisher.C4);
		uglyBettyC4.setTitle("Ugly Betty Episode One");
		Version bbcVersion = new Version();
		bbcVersion.setDuration(Duration.standardHours(10500));
		bbcVersion.setProvider(Publisher.BBC);
		uglyBettyC4.addVersion(bbcVersion);
		
		store.createOrUpdateItem(uglyBettyC4);
		
		ContentQuery query = new ContentQuery(Attributes.VERSION_DURATION.createQuery(Operators.GREATER_THAN, ImmutableList.of(1))).copyWithApplicationConfiguration(config);
		
		List<Item> results = queryExecutor.executeItemQuery(query);
		
		assertEquals( 0, results.size() );
		
		config.setIncludedPublishers(ImmutableSet.of(Publisher.C4, Publisher.BBC));
		query = query.copyWithApplicationConfiguration(config);
		
		assertEquals( 1, queryExecutor.executeItemQuery(query).size() );
	}
	
	
	
	
    public void testBrandWithNoItemsIsNotFiltered() {
		ApplicationConfiguration config = new ApplicationConfiguration();
		config.setIncludedPublishers(ImmutableSet.of(Publisher.C4));
		
		Brand ubC4 = new Brand("http://www.channel4.com/uglybetty", "c4:ugly-betty", Publisher.C4);
		ubC4.setDescription("blah blah blah");
		ubC4.setTitle("Ugly Betty");
		
		store.createOrUpdatePlaylist(ubC4, true);
		
		ContentQuery query = ContentQuery.MATCHES_EVERYTHING.copyWithApplicationConfiguration(config);
		
		List<Brand> results = queryExecutor.executeBrandQuery(query);
		
		assertEquals( 1, results.size());
		for (Brand brand : results) {
			assertEquals(Publisher.C4, brand.getPublisher());
			assertEquals(0, brand.getItems().size());
		}
    }
    
    public void testBrandWithBadPublisherIsFiltered() {
		ApplicationConfiguration config = new ApplicationConfiguration();
		config.setIncludedPublishers(ImmutableSet.of(Publisher.BBC));
		
		Brand ubC4 = new Brand("http://www.channel4.com/uglybetty", "c4:ugly-betty", Publisher.C4);
		ubC4.setDescription("blah blah blah");
		ubC4.setTitle("Ugly Betty");
		
		store.createOrUpdatePlaylist(ubC4, true);
		
		ContentQuery query = ContentQuery.MATCHES_EVERYTHING.copyWithApplicationConfiguration(config);
		
		List<Brand> results = queryExecutor.executeBrandQuery(query);
		
		assertEquals( 0, results.size());
		
		config.setIncludedPublishers(ImmutableSet.of(Publisher.C4, Publisher.BBC));
		query = query.copyWithApplicationConfiguration(config);
		
		results = queryExecutor.executeBrandQuery(query);
		
		assertEquals( 1, results.size() );
    }
    
	public void testOnlyItemsWithConfiguredPublisherPassFilter() {
		ApplicationConfiguration config = new ApplicationConfiguration();
		config.setIncludedPublishers(ImmutableSet.of(Publisher.C4));
		
		Episode uglyBettyC4 = new Episode("http://www.channel4.com/uglybetty/one", "c4:ugly-betty-one", Publisher.C4);
		uglyBettyC4.setTitle("Ugly Betty Episode One");
		Episode uglyBettyHulu = new Episode("http://www.hulu.com/uglybetty/one", "hulu:ugly-betty-one", Publisher.HULU);
		uglyBettyHulu.setTitle("Ugly Betty Episode One");
		
		Brand ubC4 = new Brand("http://www.channel4.com/uglybetty", "c4:ugly-betty", Publisher.C4);
		ubC4.setDescription("blah blah blah");
		ubC4.setTitle("Ugly Betty");
		ubC4.addItem(uglyBettyC4);
		ubC4.addItem(uglyBettyHulu);
		
		store.createOrUpdatePlaylist(ubC4, true);
		
		ContentQuery query = ContentQuery.MATCHES_EVERYTHING.copyWithApplicationConfiguration(config);
		
		List<Brand> results = queryExecutor.executeBrandQuery(query);
		
		assertEquals( 1, results.size());
		for (Brand brand : results) {
			assertEquals(Publisher.C4, brand.getPublisher());
			for (Item item : brand.getItems()) {
				assertEquals(Publisher.C4, item.getPublisher());
			}
		}
	}
	
	public void testBrandPassesFilterWhereAllItemsFailFilterWhenQueryDoesntSpecifyItemOrBelow() {
		ApplicationConfiguration config = new ApplicationConfiguration();
		config.setIncludedPublishers(ImmutableSet.of(Publisher.C4));
		
		Episode uglyBettyHulu = new Episode("http://www.hulu.com/uglybetty/one", "hulu:ugly-betty-one", Publisher.HULU);
		uglyBettyHulu.setTitle("Ugly Betty Episode One");
		
		Brand ubC4 = new Brand("http://www.channel4.com/uglybetty", "c4:ugly-betty", Publisher.C4);
		ubC4.setDescription("blah blah blah");
		ubC4.setTitle("Ugly Betty");
		ubC4.addItem(uglyBettyHulu);
		
		store.createOrUpdatePlaylist(ubC4, true);
		
		ContentQuery query = ContentQuery.MATCHES_EVERYTHING.copyWithApplicationConfiguration(config);
		
		List<Brand> results = queryExecutor.executeBrandQuery(query);
		
		assertEquals( 1, results.size() );
		assertEquals( 0, results.get(0).getItems().size() );
		
		config.setIncludedPublishers(ImmutableSet.of(Publisher.C4, Publisher.HULU));
		query = query.copyWithApplicationConfiguration(config);
		
		results = queryExecutor.executeBrandQuery(query);
		
		assertEquals( 1, results.size() );
		assertEquals( 1, results.get(0).getItems().size() ); 
	}
	
	public void testBrandFailsFilterWhereAllItemsFailFilterWhenQuerySpecifiesItemOrBelow() {
		ApplicationConfiguration config = new ApplicationConfiguration();
		config.setIncludedPublishers(ImmutableSet.of(Publisher.C4));
		
		Episode uglyBettyHulu = new Episode("http://www.hulu.com/uglybetty/one", "hulu:ugly-betty-one", Publisher.HULU);
		uglyBettyHulu.setTitle("Ugly Betty Episode One");
		uglyBettyHulu.setGenres(ImmutableSet.of("Yawn"));
		
		Brand ubC4 = new Brand("http://www.channel4.com/uglybetty", "c4:ugly-betty", Publisher.C4);
		ubC4.setDescription("blah blah blah");
		ubC4.setTitle("Ugly Betty");
		ubC4.addItem(uglyBettyHulu);
		
		store.createOrUpdatePlaylist(ubC4, true);
		
		ContentQuery query = new ContentQuery(Attributes.ITEM_GENRE.createQuery(Operators.EQUALS, ImmutableList.of("Yawn"))).copyWithApplicationConfiguration(config);
		
		List<Brand> results = queryExecutor.executeBrandQuery(query);
		
		assertEquals( 0, results.size() );
		
		config.setIncludedPublishers(ImmutableSet.of(Publisher.C4, Publisher.HULU));
		query = query.copyWithApplicationConfiguration(config);
		
		assertEquals( 1, queryExecutor.executeItemQuery(query).size() );
	}
	
	public void testBrandPassesFilterWhereAllItemsFailFilterWhenQueryDoesntSpecifyVersionOrBelow() {
		ApplicationConfiguration config = new ApplicationConfiguration();
		config.setIncludedPublishers(ImmutableSet.of(Publisher.C4));
		
		Episode uglyBettyHulu = new Episode("http://www.hulu.com/uglybetty/one", "hulu:ugly-betty-one", Publisher.C4);
		uglyBettyHulu.setTitle("Ugly Betty Episode One");
		Version bbcVersion = new Version();
		bbcVersion.setProvider(Publisher.BBC);
		uglyBettyHulu.addVersion(bbcVersion);
	
		Brand ubC4 = new Brand("http://www.channel4.com/uglybetty", "c4:ugly-betty", Publisher.C4);
		ubC4.setDescription("blah blah blah");
		ubC4.setTitle("Ugly Betty");
		ubC4.addItem(uglyBettyHulu);
		
		store.createOrUpdatePlaylist(ubC4, true);
		
		ContentQuery query = ContentQuery.MATCHES_EVERYTHING.copyWithApplicationConfiguration(config);
		
		List<Brand> results = queryExecutor.executeBrandQuery(query);
		
		assertEquals( 1, results.size() );
		assertEquals( 1, results.get(0).getItems().size() );
		assertEquals( 0, results.get(0).getItems().get(0).getVersions().size() );
		
		config.setIncludedPublishers(ImmutableSet.of(Publisher.C4, Publisher.BBC));
		query = query.copyWithApplicationConfiguration(config);
		
		results = queryExecutor.executeBrandQuery(query);
		
		assertEquals( 1, results.size() );
		assertEquals( 1, results.get(0).getItems().size() ); 
		assertEquals( 1, results.get(0).getItems().get(0).getVersions().size() );
	}
	
	public void testBrandFailsFilterWhereAllItemsFailFilterWhenQueryDoesSpecifyVersionOrBelow() {
		ApplicationConfiguration config = new ApplicationConfiguration();
		config.setIncludedPublishers(ImmutableSet.of(Publisher.C4));
		
		Episode uglyBettyHulu = new Episode("http://www.hulu.com/uglybetty/one", "hulu:ugly-betty-one", Publisher.C4);
		uglyBettyHulu.setTitle("Ugly Betty Episode One");
		Version bbcVersion = new Version();
		bbcVersion.setProvider(Publisher.BBC);
		bbcVersion.setDuration(Duration.standardHours(1000));
		uglyBettyHulu.addVersion(bbcVersion);
	
		Brand ubC4 = new Brand("http://www.channel4.com/uglybetty", "c4:ugly-betty", Publisher.C4);
		ubC4.setDescription("blah blah blah");
		ubC4.setTitle("Ugly Betty");
		ubC4.addItem(uglyBettyHulu);
		
		store.createOrUpdatePlaylist(ubC4, true);
		
		ContentQuery query = new ContentQuery(Attributes.VERSION_DURATION.createQuery(Operators.GREATER_THAN, ImmutableList.of(1))).copyWithApplicationConfiguration(config);
		
		List<Brand> results = queryExecutor.executeBrandQuery(query);
		
		assertEquals( 0, results.size() );
		
		config.setIncludedPublishers(ImmutableSet.of(Publisher.C4, Publisher.BBC));
		query = query.copyWithApplicationConfiguration(config);
		
		results = queryExecutor.executeBrandQuery(query);
		
		assertEquals( 1, results.size() );
		assertEquals( 1, results.get(0).getItems().size() ); 
		assertEquals( 1, results.get(0).getItems().get(0).getVersions().size() );
	}
	
	
	
	
    public void testPlaylistWithNoItemsIsNotFiltered() {
		ApplicationConfiguration config = new ApplicationConfiguration();
		config.setIncludedPublishers(ImmutableSet.of(Publisher.C4));
		
		Playlist ubC4 = new Playlist("http://www.channel4.com/uglybetty", "c4:ugly-betty", Publisher.C4);
		ubC4.setGenres(ImmutableSet.of("Fun"));
		ubC4.setDescription("blah blah blah");
		ubC4.setTitle("Ugly Betty");
		
		store.createOrUpdatePlaylist(ubC4, true);
		
		ContentQuery query = new ContentQuery(Attributes.PLAYLIST_GENRE.createQuery(Operators.EQUALS, ImmutableSet.of("Fun"))).copyWithApplicationConfiguration(config);
		
		List<Playlist> results = queryExecutor.executePlaylistQuery(query);
		
		assertEquals( 1, results.size());
		for (Playlist playlist : results) {
			assertEquals(Publisher.C4, playlist.getPublisher());
			assertEquals(0, playlist.getItems().size());
		}
    }
    
    public void testPlaylistWithBadPublisherIsFiltered() {
		ApplicationConfiguration config = new ApplicationConfiguration();
		config.setIncludedPublishers(ImmutableSet.of(Publisher.BBC));
		
		Playlist ubC4 = new Playlist("http://www.channel4.com/uglybetty", "c4:ugly-betty", Publisher.C4);
		ubC4.setDescription("blah blah blah");
		ubC4.setTitle("Ugly Betty");
		
		store.createOrUpdatePlaylist(ubC4, true);
		
		ContentQuery query = ContentQuery.MATCHES_EVERYTHING.copyWithApplicationConfiguration(config);
		
		List<Playlist> results = queryExecutor.executePlaylistQuery(query);
		
		assertEquals( 0, results.size());
		
		config.setIncludedPublishers(ImmutableSet.of(Publisher.C4, Publisher.BBC));
		query = query.copyWithApplicationConfiguration(config);
		
		results = queryExecutor.executePlaylistQuery(query);
		
		assertEquals( 1, results.size() );
    }
    
	public void testOnlyBrandsWithConfiguredPublisherPassFilter() {
		ApplicationConfiguration config = new ApplicationConfiguration();
		config.setIncludedPublishers(ImmutableSet.of(Publisher.C4));
		
		Playlist ubC4 = new Playlist("http://www.channel4.com/uglybetty", "c4:ugly-betty", Publisher.C4);
		ubC4.setGenres(ImmutableSet.of("Fun"));
		ubC4.setDescription("blah blah blah");
		ubC4.setTitle("Ugly Betty");
		
		Brand c4playlist = new Brand("http://www.channel4.com/uglybetty/one", "c4:ugly-betty-one", Publisher.C4);
		ubC4.addPlaylist(c4playlist);
		Brand huluPlaylist = new Brand("http://www.hulu.com/uglybetty/one", "hulu:ugly-betty-one", Publisher.HULU);
		ubC4.addPlaylist(huluPlaylist);
		
		store.createOrUpdatePlaylist(ubC4, true);
		
		ContentQuery query = new ContentQuery(Attributes.PLAYLIST_GENRE.createQuery(Operators.EQUALS, ImmutableSet.of("Fun"))).copyWithApplicationConfiguration(config);
		
		List<Playlist> results = queryExecutor.executePlaylistQuery(query);
		
		assertEquals( 1, results.size());
		for (Playlist playlist : results) {
			assertEquals(Publisher.C4, playlist.getPublisher());
			for (Playlist pl : playlist.getPlaylists()) {
				assertEquals(Publisher.C4, pl.getPublisher());
			}
		}
	}
	
	public void testOnlySubPlaylistsWithConfiguredPublisherPassFilter() {
		ApplicationConfiguration config = new ApplicationConfiguration();
		config.setIncludedPublishers(ImmutableSet.of(Publisher.C4));
		
		Playlist ubC4 = new Playlist("http://www.channel4.com/uglybetty", "c4:ugly-betty", Publisher.C4);
		ubC4.setGenres(ImmutableSet.of("Fun"));
		ubC4.setDescription("blah blah blah");
		ubC4.setTitle("Ugly Betty");
		
		Playlist c4playlist = new Playlist("http://www.channel4.com/uglybetty/one", "c4:ugly-betty-one", Publisher.C4);
		c4playlist.setGenres(ImmutableSet.of("Fun"));
		ubC4.addPlaylist(c4playlist);
		Brand huluPlaylist = new Brand("http://www.hulu.com/uglybetty/one", "hulu:ugly-betty-one", Publisher.HULU);
		c4playlist.setGenres(ImmutableSet.of("Fun"));
		c4playlist.addPlaylist(huluPlaylist);
		
		store.createOrUpdatePlaylist(ubC4, true);
		
		ContentQuery query = new ContentQuery(Attributes.PLAYLIST_GENRE.createQuery(Operators.EQUALS, ImmutableSet.of("Fun"))).copyWithApplicationConfiguration(config);
		
		List<Playlist> results = queryExecutor.executePlaylistQuery(query);
		
		assertEquals( 2, results.size());
		for (Playlist playlist : results) {
			checkPlaylistPublishers(playlist);
		}
	}
	
	private void checkPlaylistPublishers(Playlist playlist) {
		assertEquals(Publisher.C4, playlist.getPublisher());
		for (Playlist pl : playlist.getPlaylists()) {
			checkPlaylistPublishers(pl);
		}
	}
	
	public void testPlaylistPassesFilterWhereAllSubPlaylistItemsFailFilterWhenQueryDoesntSpecifyItemOrBelow() {
		ApplicationConfiguration config = new ApplicationConfiguration();
		config.setIncludedPublishers(ImmutableSet.of(Publisher.C4));
		
		Playlist c4playlist = new Playlist("http://www.channel4.com/uglybetty", "c4:ugly-betty", Publisher.C4);
		c4playlist.setGenres(ImmutableSet.of("Fun"));
		c4playlist.setDescription("blah blah blah");
		c4playlist.setTitle("Ugly Betty");
		
		Brand c4brand = new Brand("http://www.channel4.com/uglybetty/one", "c4:ugly-betty-one", Publisher.C4);
		
		Episode c4episode = new Episode("http://www.hulu.com/uglybetty/one", "hulu:ugly-betty-one", Publisher.BBC);
		c4episode.setTitle("Ugly Betty Episode One");
		
		Version bbcVersion = new Version();
		bbcVersion.setProvider(Publisher.BBC);

		c4episode.addVersion(bbcVersion);
		c4brand.addItem(c4episode);
		c4playlist.addPlaylist(c4brand);
		
		store.createOrUpdatePlaylist(c4playlist, true);
		
		ContentQuery query = new ContentQuery(Attributes.PLAYLIST_GENRE.createQuery(Operators.EQUALS, ImmutableSet.of("Fun"))).copyWithApplicationConfiguration(config);
		
		List<Playlist> results = queryExecutor.executePlaylistQuery(query);
		
		assertEquals( 1, results.size() );
		assertEquals( 1, results.get(0).getPlaylists().size() );
		assertEquals( 0, results.get(0).getPlaylists().get(0).getItems().size() );
//		assertEquals( 0, results.get(0).getPlaylists().get(0).getItems().get(0).getVersions().size() );
		
		config.setIncludedPublishers(ImmutableSet.of(Publisher.C4, Publisher.BBC));
		query = query.copyWithApplicationConfiguration(config);
		
		results = queryExecutor.executePlaylistQuery(query);
		
		assertEquals( 1, results.size() );
		assertEquals( 1, results.get(0).getPlaylists().size() );
		assertEquals( 1, results.get(0).getPlaylists().get(0).getItems().size() );
		assertEquals( 1, results.get(0).getPlaylists().get(0).getItems().get(0).getVersions().size() );
	}
	
	public void testPlaylistFailsFilterWhereAllSubPlaylistsFailFilterWhenQueryDoesSpecifyBrandOrBelow() {
		ApplicationConfiguration config = new ApplicationConfiguration();
		config.setIncludedPublishers(ImmutableSet.of(Publisher.C4));
		
		Playlist c4playlist = new Playlist("http://www.channel4.com/uglybetty", "c4:ugly-betty", Publisher.C4);
		c4playlist.setGenres(ImmutableSet.of("Fun"));
		c4playlist.setDescription("blah blah blah");
		c4playlist.setTitle("Ugly Betty");
		
		Brand hulubrand = new Brand("http://www.channel4.com/uglybetty/one", "c4:ugly-betty-one", Publisher.HULU);
		hulubrand.setGenres(ImmutableSet.of("Joy"));
		
		Episode huluEpisode = new Episode("http://www.hulu.com/uglybetty/one", "hulu:ugly-betty-one", Publisher.HULU);
		huluEpisode.setTitle("Ugly Betty Episode One");
		
		Version huluVersion = new Version();
		huluVersion.setDuration(Duration.standardHours(1000));
		huluVersion.setProvider(Publisher.HULU);

		huluEpisode.addVersion(huluVersion);
		hulubrand.addItem(huluEpisode);
		c4playlist.addPlaylist(hulubrand);
		
		store.createOrUpdatePlaylist(c4playlist, true);
		
		ContentQuery query = new ContentQuery(Attributes.BRAND_GENRE.createQuery(Operators.EQUALS, ImmutableSet.of("Joy"))).copyWithApplicationConfiguration(config);
		
		List<Playlist> results = queryExecutor.executePlaylistQuery(query);
		
		assertEquals( 0, results.size() );
		
		config.setIncludedPublishers(ImmutableSet.of(Publisher.C4, Publisher.HULU));
		query = query.copyWithApplicationConfiguration(config);
		
		results = queryExecutor.executePlaylistQuery(query);
		
		assertEquals( 1, results.size() );
		assertEquals( 1, results.get(0).getPlaylists().get(0).getItems().size() );
	}
	
	public void testPlaylistFailsFilterWhereAllSubPlaylistItemsFailFilterWhenQueryDoesSpecifyItemOrBelow() {
		ApplicationConfiguration config = new ApplicationConfiguration();
		config.setIncludedPublishers(ImmutableSet.of(Publisher.C4));
		
		Playlist c4playlist = new Playlist("http://www.channel4.com/uglybetty", "c4:ugly-betty", Publisher.C4);
		c4playlist.setGenres(ImmutableSet.of("Fun"));
		c4playlist.setDescription("blah blah blah");
		c4playlist.setTitle("Ugly Betty");
		
		Brand c4brand = new Brand("http://www.channel4.com/uglybetty/one", "c4:ugly-betty-one", Publisher.C4);
		
		Episode huluEpisode = new Episode("http://www.hulu.com/uglybetty/one", "hulu:ugly-betty-one", Publisher.HULU);
		huluEpisode.setTitle("Ugly Betty Episode One");
		
		Version huluVersion = new Version();
		huluVersion.setDuration(Duration.standardHours(1000));
		huluVersion.setProvider(Publisher.HULU);

		huluEpisode.addVersion(huluVersion);
		c4brand.addItem(huluEpisode);
		c4playlist.addPlaylist(c4brand);
		
		store.createOrUpdatePlaylist(c4playlist, true);
		
		ContentQuery query = new ContentQuery(Attributes.VERSION_DURATION.createQuery(Operators.GREATER_THAN, ImmutableSet.of(1))).copyWithApplicationConfiguration(config);
		
		List<Playlist> results = queryExecutor.executePlaylistQuery(query);
		
		assertEquals( 0, results.size() );
		
		config.setIncludedPublishers(ImmutableSet.of(Publisher.C4, Publisher.HULU));
		query = query.copyWithApplicationConfiguration(config);
		
		results = queryExecutor.executePlaylistQuery(query);
		
		assertEquals( 2, results.size() );
		assertEquals( 1, results.get(0).getItems().size() );
	}
}
