package org.atlasapi.application;

import org.atlasapi.application.users.MongoUserStore;
import org.atlasapi.application.users.UserStore;
import org.atlasapi.application.www.ApplicationWebModule;
import org.atlasapi.persistence.application.ApplicationPersistenceModule;
import org.atlasapi.persistence.application.SourceRequestStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.social.auth.credentials.CredentialsStore;
import com.metabroadcast.common.social.auth.credentials.MongoDBCredentialsStore;

@Configuration
@Import({ ApplicationPersistenceModule.class, ApplicationWebModule.class })
public class ApplicationModule {
  
    private @Autowired @Qualifier(value = "adminMongo") DatabasedMongo adminMongo;
    //private @Autowired ViewResolver viewResolver;
    private @Autowired SourceRequestStore sourceRequestStore;

    public @Bean
    UserStore userStore() {
        return new MongoUserStore(adminMongo);
    }

    public @Bean
    CredentialsStore credentialsStore() {
        return new MongoDBCredentialsStore(adminMongo);
    }
}
