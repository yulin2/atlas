package org.atlasapi.application.persistence;

import org.atlasapi.application.ApplicationCredentialsTranslator;
import org.atlasapi.application.model.Application;
import org.atlasapi.media.common.Id;

import com.metabroadcast.common.persistence.mongo.MongoConstants;
import com.metabroadcast.common.persistence.translator.TranslatorUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;


public class MongoApplicationTranslator {
    public static final String SLUG_KEY = MongoConstants.ID;
    public static final String DEER_ID_KEY = "deerId";
    public static final String TITLE_KEY = "title";
    public static final String CREATED_KEY = "created";
    public static final String CREDENTIALS_KEY = "credentials";
    public static final String CONFIG_KEY = "configuration";
    
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
        
        Long applicationId = TranslatorUtils.toLong(dbo, DEER_ID_KEY);
        if(DEER_ID_KEY == null){
            return null;
        }
        
        return Application.builder()
                .withId(Id.valueOf(applicationId))
                .withTitle(TranslatorUtils.toString(dbo, TITLE_KEY))
                .withCreated(TranslatorUtils.toDateTime(dbo, CREATED_KEY))
                .withCredentials(credentialsTranslator.fromDBObject(TranslatorUtils.toDBObject(dbo, CREDENTIALS_KEY)))
                .withSources(sourcesTranslator.fromDBObject(TranslatorUtils.toDBObject(dbo, CONFIG_KEY)))
                .build();
    }
}