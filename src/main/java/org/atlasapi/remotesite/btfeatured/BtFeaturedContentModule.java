package org.atlasapi.remotesite.btfeatured;

import javax.annotation.PostConstruct;

import nu.xom.Builder;

import org.atlasapi.persistence.content.ContentGroupResolver;
import org.atlasapi.persistence.content.ContentGroupWriter;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.metabroadcast.common.http.SimpleHttpClientBuilder;
import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.RepetitionRules.Daily;
import com.metabroadcast.common.scheduling.SimpleScheduler;

/**
 * Creates and schedules a BTFeaturedContentUpdater to ingest BT Featured Content on a daily basis
 * 
 * @author andrewtoone
 *
 */
@Configuration
public class BtFeaturedContentModule {

    private final static Daily DAILY = RepetitionRules.daily(new LocalTime(4, 15, 0)); // TODO: When should this be, should it be configurable?

    private @Autowired SimpleScheduler scheduler;
    private @Autowired ContentGroupResolver groupResolver;
    private @Autowired ContentResolver contentResolver;
    private @Autowired ContentGroupWriter groupWriter;
    private @Autowired ContentWriter contentWriter;   
    
    private @Value("${btfeatured.rootDocumentUri}") String rootDocumentUri;
    private @Value("${btfeatured.productBaseUri}") String productBaseUri;
 
    @PostConstruct
    public void startBackgroundTasks() {
        scheduler.schedule(btFeaturedContentUpdater(groupResolver, groupWriter, contentResolver, contentWriter).withName("BT Featured Content Updater"), DAILY);
    }

    private BtFeaturedContentUpdater btFeaturedContentUpdater(ContentGroupResolver groupResolver, ContentGroupWriter groupWriter, ContentResolver contentResolver, ContentWriter contentWriter) {
        BtFeaturedNodeFactory factory = new BtFeaturedNodeFactory();
        
        BtFeaturedClient client = new BtFeaturedClient(new SimpleHttpClientBuilder().build(), new Builder(factory));
        BtFeaturedElementHandler handler = new BtFeaturedElementHandler();
        
        BtFeaturedContentUpdater updater = new BtFeaturedContentUpdater(client, handler, groupResolver, groupWriter, contentResolver, contentWriter, productBaseUri, rootDocumentUri);
        
        return updater;
    }

}
