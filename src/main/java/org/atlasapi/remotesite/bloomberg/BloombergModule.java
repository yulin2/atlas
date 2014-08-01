package org.atlasapi.remotesite.bloomberg;

import javax.annotation.PostConstruct;

import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.spreadsheet.ClientCredentials;
import org.atlasapi.spreadsheet.GoogleOAuth;
import org.atlasapi.spreadsheet.GoogleSpreadsheetService;
import org.atlasapi.spreadsheet.SpreadsheetFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;

@Configuration
public class BloombergModule {

    private @Autowired AdapterLog log;
    private @Autowired SimpleScheduler scheduler;
    private @Autowired ContentResolver contentResolver;
    private @Autowired ContentWriter contentWriter;
    
    private @Value("${client.id}") String clientId;
    private @Value("${client.secret}") String clientSecret;
    private @Value("${refresh.token}") String refreshToken;
    private @Value("${access.token}") String accessToken;
    
    @Bean
    public ClientCredentials clientCredentials() {
        return new ClientCredentials(clientId, clientSecret, refreshToken, accessToken);
    }
    
    @Bean
    public GoogleOAuth googleOAuth() {
        return new GoogleOAuth(clientCredentials(), new NetHttpTransport(), new JacksonFactory());
    }
    
    @Bean
    public GoogleSpreadsheetService googleSpreadsheetService() {
        return new GoogleSpreadsheetService(googleOAuth().googleCredentials());
    }
    
    @Bean
    public SpreadsheetFetcher spreadsheetFetcher() {
        return new SpreadsheetFetcher(googleSpreadsheetService().getSpreadsheetService());
    }
    
    @PostConstruct
    public void startBackgroundTasks() {
        log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass()).withDescription("Bloomberg updater"));
        scheduler.schedule(bloombergUpdater().withName("Bloomberg Spreadsheet Updater"), RepetitionRules.NEVER);
    }
    
    private BloombergUpdateTask bloombergUpdater() {
        return new BloombergUpdateTask(spreadsheetFetcher(), 
                new DefaultBloombergDataRowHandler(contentResolver, contentWriter, new BloombergDataRowContentExtractor()), 
                new BloombergAdapter());
    }
    
}
