package org.atlasapi.remotesite.voila;

import static com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES;
import static org.atlasapi.http.HttpBackedRemoteSiteClient.httpRemoteSiteClient;
import static org.atlasapi.http.HttpResponseTransformers.gsonResponseTransformer;
import static org.atlasapi.remotesite.HttpClients.webserviceClient;

import java.text.ParseException;

import javax.annotation.PostConstruct;

import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.persistence.topic.TopicStore;
import org.atlasapi.remotesite.voila.CannonTwitterTopicsUpdater.ContentWordsIdList;
import org.atlasapi.remotesite.voila.ContentWords.ContentWordsList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.net.HostSpecifier;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;

@Configuration
public class VoilaModule {

    private @Value("${cannon.host.name}") String cannonHostName;
    private @Value("${cannon.host.port}") Integer cannonHostPort;
    private @Autowired ContentResolver contentResolver;
    private @Autowired ContentWriter contentWriter;
    private @Autowired TopicStore topicStore;
    private @Autowired SimpleScheduler scheduler;
    private @Autowired AdapterLog log;
    
    @PostConstruct
    public void scheduleTasks() {
        scheduler.schedule(cannonTwitterTopicsUpdater(), RepetitionRules.NEVER);
    }

    @Bean CannonTwitterTopicsUpdater cannonTwitterTopicsUpdater() {
        RemoteSiteClient<ContentWordsIdList> remoteClient = httpRemoteSiteClient(webserviceClient(), gsonResponseTransformer(new GsonBuilder().setFieldNamingPolicy(LOWER_CASE_WITH_UNDERSCORES), ContentWordsIdList.class));
        try {
            return new CannonTwitterTopicsUpdater(remoteClient, HostSpecifier.from(cannonHostName), Optional.fromNullable(cannonHostPort), contentTopicsUpdater(), log);
        } catch (ParseException e) {
            throw Throwables.propagate(e);
        }
    }
    
    @Bean ContentTwitterTopicsUpdater contentTopicsUpdater() {
        RemoteSiteClient<ContentWordsList> remoteClient = httpRemoteSiteClient(webserviceClient(), gsonResponseTransformer(new GsonBuilder().setFieldNamingPolicy(LOWER_CASE_WITH_UNDERSCORES), new TypeToken<ContentWordsList>() {
        }));
        try {
            return new ContentTwitterTopicsUpdater(remoteClient, HostSpecifier.from(cannonHostName), Optional.fromNullable(cannonHostPort), contentResolver, topicStore, contentWriter, log);
        } catch (ParseException e) {
            throw Throwables.propagate(e);
        }
    }
}
