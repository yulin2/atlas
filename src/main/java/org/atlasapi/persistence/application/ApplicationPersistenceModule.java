package org.atlasapi.persistence.application;

import org.atlasapi.persistence.application.ApplicationStore;
import org.atlasapi.persistence.application.MongoApplicationStore;
import org.atlasapi.persistence.application.MongoSourceRequestStore;
import org.atlasapi.persistence.application.SourceRequestStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;

@Configuration
public class ApplicationPersistenceModule {
    
    private @Autowired @Qualifier(value = "adminMongo") DatabasedMongo adminMongo; 

    @Bean
    @Qualifier(value = "deerApplicationsStore")
    protected ApplicationStore deerApplicationsStore() {
        return new MongoApplicationStore(adminMongo);
    }
    
    @Bean
    protected SourceRequestStore sourceRequestStore() {
        return new MongoSourceRequestStore(adminMongo);
    }
    
}
