package org.atlasapi.remotesite.hulu;

import static org.atlasapi.remotesite.hulu.HuluItemAdapter.basicHuluItemAdapter;
import static org.atlasapi.remotesite.hulu.HuluItemAdapter.brandFetchingHuluItemAdapter;

import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HuluAdapterModule {
    
    private @Autowired ContentWriter writer;
    private @Autowired AdapterLog log;

    public @Bean HuluClient huluClient() {
        return new HttpBackedHuluClient(HttpClients.webserviceClient(), log);
    }
    
    public @Bean
    HuluItemAdapter huluItemAdapter() {
        return brandFetchingHuluItemAdapter(huluClient(), huluBrandAdapter());
    }

    public @Bean WritingHuluBrandAdapter huluBrandAdapter() {
        return new WritingHuluBrandAdapter(huluClient(), basicHuluItemAdapter(huluClient()), writer, log);
    }
}
