package org.atlasapi.remotesite.itv.whatson;

import javax.annotation.PostConstruct;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.google.common.collect.FluentIterable;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;
import com.metabroadcast.common.social.http.HttpClients;

@Configuration
public class ItvWhatsOnModule {
    private @Autowired SimpleScheduler scheduler;
    private @Autowired @Qualifier("contentResolver") ContentResolver contentResolver;
    private @Autowired @Qualifier("contentWriter") ContentWriter contentWriter;
    private @Value("${itv.whaton.schedule.url}") String feedUrl;
    
    private @Autowired AdapterLog log;
    
    @PostConstruct
    public void startBackgroundTasks() {
        scheduler.schedule(itvWhatsOnUpdater(), RepetitionRules.NEVER);
        log.record(new AdapterLogEntry(Severity.INFO).withDescription("ITV What's On Schedule task installed.").withSource(getClass()));
    }
    
    @Bean
    public SimpleHttpClient httpClient() {
        return HttpClients.webserviceClient();
    }
    
    @Bean
    public RemoteSiteClient<FluentIterable<ItvWhatsOnEntry>> itvWhatsOnClient() {
        return new ItvWhatsOnClient(httpClient());
    }
    
    @Bean
    public ItvWhatsOnUpdater itvWhatsOnUpdater() {
        return new ItvWhatsOnUpdater(feedUrl, itvWhatsOnClient());
    }

}
