package org.atlasapi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.metabroadcast.common.webapp.properties.ContextConfigurer;

// README This class has the ony purpose to isolate and have the configurer load first, otherwise some subtle conflicts happen.
@Configuration
public class AtlasModule {

    @Bean
    public ContextConfigurer config() {
        ContextConfigurer c = new ContextConfigurer();
        c.init();
        return c;
    }
}
