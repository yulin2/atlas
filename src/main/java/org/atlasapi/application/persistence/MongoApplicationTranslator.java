package org.atlasapi.application.persistence;

import org.atlasapi.application.ApplicationCredentialsTranslator;
import org.atlasapi.application.model.Application;

import com.metabroadcast.common.persistence.mongo.MongoConstants;
import com.metabroadcast.common.persistence.translator.TranslatorUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;


public class MongoApplicationTranslator {
    public static final String APPLICATION_SLUG_KEY = MongoConstants.ID;
    public static final String APPLICATION_TITLE_KEY = "title";
    public static final String APPLICATION_CREATED_KEY = "created";
    public static final String APPLICATION_CREDENTIALS_KEY = "credentials";
    public static final String APPLICATION_CONFIG_KEY = "configuration";
    
    private final ApplicationCredentialsTranslator credentialsTranslator = new ApplicationCredentialsTranslator();
    private final ApplicationSourcesTranslator sourcesTranslator = new ApplicationSourcesTranslator();
    
    public DBObject toDBObject(Application application) {
        // TODO
        DBObject dbo = new BasicDBObject();
        return null;
    }
    
    public Application fromDBObject(DBObject dbo) {
        if (dbo == null) {
            return null;
        }
        
        String applicationSlug = TranslatorUtils.toString(dbo, APPLICATION_SLUG_KEY);
        if(applicationSlug == null){
            return null;
        }
        
        return Application.builder()
                .withId(applicationSlug)
                .withTitle(TranslatorUtils.toString(dbo, APPLICATION_TITLE_KEY))
                .withCreated(TranslatorUtils.toDateTime(dbo, APPLICATION_CREATED_KEY))
                .withCredentials(credentialsTranslator.fromDBObject(TranslatorUtils.toDBObject(dbo, APPLICATION_CREDENTIALS_KEY)))
                .withSources(sourcesTranslator.fromDBObject(TranslatorUtils.toDBObject(dbo, APPLICATION_CONFIG_KEY)))
                .build();
    }
}
