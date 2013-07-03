package org.atlasapi.remotesite.thesun;

import nu.xom.Builder;
import nu.xom.Document;

import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.HttpClients;
import org.atlasapi.remotesite.channel4.XmlClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.metabroadcast.common.http.RequestLimitingSimpleHttpClient;
import com.metabroadcast.common.http.SimpleHttpClient;

@Configuration
public class TheSunModule {
    private @Autowired @Qualifier("contentResolver") ContentResolver contentResolver;
    private @Autowired @Qualifier("contentWriter") ContentWriter contentWriter;
    
    @Bean 
    public RemoteSiteClient<Document> theSunTvPicksClient() {
        return new XmlClient(requestLimitedHttpClient(), new Builder(new TheSunTvPicksElementFactory()));
    }
    
    @Bean 
    protected SimpleHttpClient requestLimitedHttpClient() {
        return new RequestLimitingSimpleHttpClient(HttpClients.webserviceClient(), 4);
    }
}
