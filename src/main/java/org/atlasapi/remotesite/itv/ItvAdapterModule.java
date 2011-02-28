package org.atlasapi.remotesite.itv;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ItvAdapterModule {
    
    @Bean ItvMercuryBrandAdapter itvBrandAdapter() {
        return new ItvMercuryBrandAdapter();
    }
    
    @Bean ItvMercuryEpisodeAdapter itvEpisodeAdapter() {
        return new ItvMercuryEpisodeAdapter();
    }
    
}
