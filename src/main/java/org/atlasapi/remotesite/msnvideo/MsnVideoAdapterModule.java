package org.atlasapi.remotesite.msnvideo;

import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

public class MsnVideoAdapterModule {

    private @Autowired AdapterLog log;
    
    public @Bean MsnVideoItemAdapter msnVideoItemAdapter() {
        return new MsnVideoItemAdapter(HttpClients.screenScrapingClient(), log);
    }
    
    public @Bean MsnVideoBrandAdapter msnVideoBrandAdapter() {
        return new MsnVideoBrandAdapter(HttpClients.screenScrapingClient(), log, msnVideoItemAdapter());
    }
}
