package org.atlasapi.codec;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CodecModule {

    @Bean
    public CodecController codecController() {
        return new CodecController();
    }
    
}
