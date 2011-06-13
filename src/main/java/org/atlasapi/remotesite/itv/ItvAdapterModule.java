package org.atlasapi.remotesite.itv;

import org.atlasapi.persistence.content.ContentWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ItvAdapterModule {
    
    @Autowired ContentWriter writer;
    
    @Bean ItvMercuryBrandAdapter itvBrandAdapter() {
        return new ItvMercuryBrandAdapter(writer);
    }
    
    @Bean ItvMercuryEpisodeAdapter itvEpisodeAdapter() {
        return new ItvMercuryEpisodeAdapter();
    }
    
}
