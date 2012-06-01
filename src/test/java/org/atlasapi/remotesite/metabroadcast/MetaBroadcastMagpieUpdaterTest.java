package org.atlasapi.remotesite.metabroadcast;

import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Topic;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.topic.TopicQueryResolver;
import org.atlasapi.persistence.topic.TopicStore;
import org.jets3t.service.S3Service;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.security.AWSCredentials;
import org.junit.Ignore;
import org.mockito.Mockito;

public class MetaBroadcastMagpieUpdaterTest {

	private String s3access = "AKIAJT7DIIIS7IOCOWRQ";
	private String s3secret = "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";
	private String s3Bucket = "magpie-assets";
	private ContentWriter contentWriterMock = mock(ContentWriter.class);
	private ContentResolver contentResolverMock = mock(ContentResolver.class);
	private TopicStore topicStoreMock = mock(TopicStore.class);
	
	@Ignore("Calls s3, only run locally")
	public void testUpdatingContentTopics() throws Exception {
	    S3Service s3service = new RestS3Service(new AWSCredentials(s3access, s3secret));
	    MetaBroadcastMagpieUpdater updater = new MetaBroadcastMagpieUpdater( 
                contentResolverMock, topicStoreMock, mock(TopicQueryResolver.class),
                contentWriterMock, s3service, s3Bucket, "",
                mock(AdapterLog.class));
	    
		// These depend on what the magpie JSON returns
		Identified episode = (Episode) updater.getNewContent(new Episode() , "http://www.bbc.co.uk/programmes/b00sz5xg", "");
		Identified mbEpisode = (Episode) updater.getNewContent(new Episode() , "http://metabroadcast.com/www.bbc.co.uk/programmes/b00sz5xg", "");
		
		when(contentResolverMock.findByCanonicalUris(anyList())).thenReturn(ResolvedContent.builder()
				.put("http://www.bbc.co.uk/programmes/b00sz5xg", episode)
				.put("http://wwww.metabroadcast.com/www.bbc.co.uk/programmes/b00sz5xg", mbEpisode).build());
		
		
		updater.updateTopics(new ArrayList<String>());
		verify(topicStoreMock, Mockito.atLeastOnce()).write((Topic) anyObject());
	}
}
