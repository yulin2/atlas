package org.atlasapi.remotesite.getty;

import javax.annotation.PostConstruct;

import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.topic.TopicStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;
import com.metabroadcast.common.security.UsernameAndPassword;

@Configuration
public class GettyModule {

    private @Autowired SimpleScheduler scheduler;
    private @Autowired ContentResolver contentResolver;
    private @Autowired ContentWriter contentWriter;
    private @Autowired @Qualifier("topicStore") TopicStore topicStore;
    
    @Value("${getty.client.id}") private String clientId;
    @Value("${getty.client.secret}") private String clientSecret;
    @Value("${getty.pagination}") private String gettyPagination;
    @Value("${iris.pagination}") private String irisPagination;
    @Value("${iris.user}") private String irisUser;
    @Value("${iris.pswd}") private String irisPswd;
    @Value("${iris.url}") private String irisUrl;
    
    @PostConstruct
    public void startBackgroundTasks() {
        scheduler.schedule(gettyUpdater().withName("Getty Updater"), RepetitionRules.NEVER);
    }
    
    private GettyUpdateTask gettyUpdater() {
        return new GettyUpdateTask(new GettyAdapter(), 
                new DefaultGettyDataHandler(contentResolver, contentWriter, new GettyContentExtractor(topicStore)), 
                new GettyTokenFetcher(clientId, clientSecret), 
                new GettyVideoFetcher(Integer.valueOf(gettyPagination)),
                new IrisKeywordsFetcher(new UsernameAndPassword(irisUser, irisPswd), irisUrl),
                Integer.valueOf(gettyPagination), Integer.valueOf(irisPagination));
    }
    
}
