package org.atlasapi.remotesite.archiveorg;

import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ArchiveOrgAdapterModule {
    
    @Autowired private AdapterLog log;
    
    public @Bean ArchiveOrgItemAdapter archiveOrgItemAdapter() {
        return new ArchiveOrgItemAdapter(HttpClients.webserviceClient(), log);
    }
}
