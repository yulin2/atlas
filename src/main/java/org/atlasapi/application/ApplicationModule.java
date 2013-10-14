package org.atlasapi.application;

import org.atlasapi.application.auth.AuthProvidersListWriter;
import org.atlasapi.application.auth.AuthProvidersQueryResultWriter;
import org.atlasapi.application.auth.OAuthRequestListWriter;
import org.atlasapi.application.auth.OAuthRequestQueryResultWriter;
import org.atlasapi.application.auth.OAuthResultListWriter;
import org.atlasapi.application.auth.OAuthResultQueryResultWriter;
import org.atlasapi.application.auth.twitter.TwitterAuthController;
import org.atlasapi.application.auth.www.AuthController;
import org.atlasapi.application.users.MongoUserStore;
import org.atlasapi.application.users.UserStore;
import org.atlasapi.application.www.ApplicationWebModule;
import org.atlasapi.persistence.application.ApplicationPersistenceModule;
import org.atlasapi.persistence.application.SourceRequestStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.social.auth.credentials.CredentialsStore;
import com.metabroadcast.common.social.auth.credentials.MongoDBCredentialsStore;
import com.metabroadcast.common.social.twitter.TwitterApplication;

@Configuration
@Import({ ApplicationPersistenceModule.class, ApplicationWebModule.class })
public class ApplicationModule {
  
    private @Autowired @Qualifier(value = "adminMongo") DatabasedMongo adminMongo;
    private @Autowired SourceRequestStore sourceRequestStore;
    @Value("${twitter.auth.consumerKey}") private String consumerKey;
    @Value("${twitter.auth.consumerSecret}") private String consumerSecret;
    @Value("${local.host.name}") private String host;

    public @Bean
    UserStore userStore() {
        return new MongoUserStore(adminMongo);
    }

    public @Bean
    CredentialsStore credentialsStore() {
        return new MongoDBCredentialsStore(adminMongo);
    }
    
    public @Bean
    AuthController authController() {
        return new AuthController(new AuthProvidersQueryResultWriter(new AuthProvidersListWriter()));
    }
    
    public @Bean TwitterAuthController twitterAuthController() {
        return new TwitterAuthController(new TwitterApplication(consumerKey, consumerSecret), 
                host,
                new OAuthRequestQueryResultWriter(new OAuthRequestListWriter()),
                new OAuthResultQueryResultWriter(new OAuthResultListWriter())
                );
    }
}
