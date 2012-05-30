package org.atlasapi.remotesite.metabroadcast;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.atlasapi.remotesite.metabroadcast.AbstractMetaBroadcastContentUpdater.generateMetaBroadcastUri; 

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Topic;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.topic.TopicQueryResolver;
import org.atlasapi.persistence.topic.TopicStore;
import org.atlasapi.remotesite.metabroadcast.CannonTwitterTopicsClient;
import org.atlasapi.remotesite.metabroadcast.ContentWords;
import org.atlasapi.remotesite.metabroadcast.ContentWords.ContentWordsList;
import org.atlasapi.remotesite.metabroadcast.ContentWords.WordWeighting;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.metabroadcast.common.base.Maybe;


public class MetaBroadcastTwitterTopicsUpdaterTest {
	
	private ContentWriter contentWriterMock = mock(ContentWriter.class);
	private CannonTwitterTopicsClient clientMock = mock(CannonTwitterTopicsClient.class);
	private ContentResolver contentResolverMock = mock(ContentResolver.class);
	private TopicStore topicStoreMock = mock(TopicStore.class);
	private MetaBroadcastTwitterTopicsUpdater cut = new MetaBroadcastTwitterTopicsUpdater(clientMock,
													contentResolverMock, 
													topicStoreMock ,
													mock(TopicQueryResolver.class),
													contentWriterMock, 
													mock(AdapterLog.class));

	@Test
	public void testGetNewContent() {
		Publisher pub = Publisher.VOILA;
		assertTrue(MetaBroadcastTwitterTopicsUpdater.getNewContent(new Brand() , "http://www.example.com/test", "", Publisher.VOILA) instanceof Brand);
		assertTrue(MetaBroadcastTwitterTopicsUpdater.getNewContent(new Episode() , "http://www.example.com/test", "", Publisher.VOILA) instanceof Episode);
		boolean exception = false;
		try {
			MetaBroadcastTwitterTopicsUpdater.getNewContent(new Identified(), "http://www.example.com/test", "", Publisher.VOILA);
		} catch (Exception e) {
			exception = true;
		}
		assertTrue(exception);
		assertTrue(MetaBroadcastTwitterTopicsUpdater.getNewContent(new Brand() , "http://www.example.com/test", "", Publisher.VOILA).getCanonicalUri().equals("http://www.example.com/test"));
	}
	
	@Test
	public void testCreateNewUrls() {
		assertEquals("http://metabroadcast.com/www.bbc.co.uk/programmes/lol123", generateMetaBroadcastUri("http://www.bbc.co.uk/programmes/lol123"));
		assertEquals("http://metabroadcast.com/www.kvwn.tv/programmes/evening-news/", generateMetaBroadcastUri("https://www.kvwn.tv/programmes/evening-news/"));
		assertEquals("http://metabroadcast.com/www.ted.com/talks/a-really-cool-talk-about-something", generateMetaBroadcastUri("www.ted.com/talks/a-really-cool-talk-about-something"));
	}
	
	@Test
	public void testUpdatingContentTopics() {
		
		WordWeighting word1 = new ContentWords.WordWeighting();
		word1.setContent("content");
		word1.setUrl("http://dbpedia.org/resource/content");
		word1.setWeight(42);
		
		ContentWords words = new ContentWords();
		words.setWords(Lists.newArrayList(word1));
		words.setUri("http://www.bbc.co.uk/programmes/b00sz5xg");
		words.setContentId("1337");
		
		ContentWordsList contentWordsList = new ContentWordsList();
		contentWordsList.setResults(Lists.newArrayList(words));
		
		Optional<ContentWordsList> optionalList = Optional.of(contentWordsList);
		when(clientMock.getContentWordsForIds(anyList())).thenReturn(optionalList);
		
		Identified episode = (Episode) MetaBroadcastTwitterTopicsUpdater.getNewContent(new Episode() , "http://www.bbc.co.uk/programmes/b00sz5xg", "", Publisher.VOILA);
		Identified mbEpisode = (Episode) MetaBroadcastTwitterTopicsUpdater.getNewContent(new Episode() , "http://wwww.metabroadcast.com/www.bbc.co.uk/programmes/b00sz5xg", "", Publisher.VOILA);
		
		when(contentResolverMock.findByCanonicalUris(anyList())).thenReturn(ResolvedContent.builder()
				.put("http://www.bbc.co.uk/programmes/b00sz5xg", episode)
				.put("http://wwww.metabroadcast.com/www.bbc.co.uk/programmes/b00sz5xg", mbEpisode).build());
		
		when(topicStoreMock.topicFor("twitter", "http://dbpedia.org/resource/content")).thenReturn(Maybe.just(new Topic(new Long(13371337), "topicnamespacewikipedia", "content")));
		
		
		assertTrue(1 == cut.updateTopics(Lists.newArrayList("http://www.bbc.co.uk/programmes/b00sz5xg")).getProcessed());
		verify(topicStoreMock).write((Topic) anyObject());
	}

	@Test
	public void testCreatingContentUpdatingTopics() {
		
		WordWeighting word1 = new ContentWords.WordWeighting();
		word1.setContent("content");
		word1.setUrl("http://dbpedia.org/resource/content");
		word1.setWeight(42);
		
		ContentWords words = new ContentWords();
		words.setWords(Lists.newArrayList(word1));
		words.setUri("http://www.bbc.co.uk/programmes/b00sz5xg");
		words.setContentId("1337");
		
		ContentWordsList contentWordsList = new ContentWordsList();
		contentWordsList.setResults(Lists.newArrayList(words));
		
		Optional<ContentWordsList> optionalList = Optional.of(contentWordsList);
		when(clientMock.getContentWordsForIds(anyList())).thenReturn(optionalList);
		
		Identified episode = (Episode) MetaBroadcastTwitterTopicsUpdater.getNewContent(new Episode() , "http://www.bbc.co.uk/programmes/b00sz5xg", "", Publisher.VOILA);
		
		when(contentResolverMock.findByCanonicalUris(anyList())).thenReturn(ResolvedContent.builder()
				.put("http://www.bbc.co.uk/programmes/b00sz5xg", episode)
				.put("http://wwww.metabroadcast.com/www.bbc.co.uk/programmes/b00sz5xg", null).build());
		
		when(topicStoreMock.topicFor("twitter", "http://dbpedia.org/resource/content")).thenReturn(Maybe.just(new Topic(new Long(13371337), "topicnamespacewikipedia", "content")));
		
		assertTrue(1 == cut.updateTopics(Lists.newArrayList("http://www.bbc.co.uk/programmes/b00sz5xg")).getProcessed());
		verify(topicStoreMock).write((Topic) anyObject());
	}
	
}
