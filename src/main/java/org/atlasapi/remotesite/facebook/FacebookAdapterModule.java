package org.atlasapi.remotesite.facebook;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.metabroadcast.common.social.actions.facebook.AuthTokenExpiredListener;
import com.metabroadcast.common.social.auth.credentials.AuthToken;
import com.metabroadcast.common.social.facebook.FacebookInteracter;

@Configuration
public class FacebookAdapterModule {

    @Bean
    public FacebookAdapter facebookAdapter() {
        FacebookInteracter interacter = new FacebookInteracter(new AuthTokenExpiredListener() {
            @Override
            public void expired(AuthToken accessToken) {
            }
        });
        return new FacebookAdapter(interacter, null);
    }
    
}
