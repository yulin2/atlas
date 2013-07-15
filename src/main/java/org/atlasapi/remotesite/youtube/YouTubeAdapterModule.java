package org.atlasapi.remotesite.youtube;

import org.atlasapi.media.entity.Identified;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class YouTubeAdapterModule {

    public @Bean SiteSpecificAdapter<? extends Identified> youTubeAdapter() {
        return new YouTubeAdapter();
    }

    public @Bean SiteSpecificAdapter<? extends Identified> youTubeFeedAdapter() {
        return new YouTubeFeedAdapter();
    }

}
