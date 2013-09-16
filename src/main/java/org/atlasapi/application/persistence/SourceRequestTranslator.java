package org.atlasapi.application.persistence;


import org.atlasapi.application.model.Application;
import org.atlasapi.application.model.SourceRequest;
import org.atlasapi.application.model.UsageType;
import org.atlasapi.media.entity.Publisher;

import com.metabroadcast.common.persistence.mongo.MongoConstants;
import com.metabroadcast.common.persistence.translator.TranslatorUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;


public class SourceRequestTranslator {
    public static final String APPSLUG_KEY = "appSlug";
    public static final String SOURCE_KEY = "publisher";
    public static final String USAGE_TYPE_KEY = "usageType";
    public static final String EMAIL_KEY = "email";
    public static final String APPURL_KEY = "appUrl";
    public static final String REASON_KEY = "reason";
    public static final String APPROVED_KEY = "approved";
    
    public String createKey(SourceRequest sourceRequest) {
        return String.format("%s|%s", sourceRequest.getAppSlug(), sourceRequest.getSource().key());
    }
    
    public String createKey(Application application, Publisher source) {
        return String.format("%s|%s", application.getSlug(), source.key());
    }
    
    public DBObject toDBObject(SourceRequest sourceRequest) {
        DBObject dbo = new BasicDBObject();
        TranslatorUtils.from(dbo, MongoConstants.ID, createKey(sourceRequest));
        TranslatorUtils.from(dbo, APPSLUG_KEY, sourceRequest.getAppSlug());
        TranslatorUtils.from(dbo, SOURCE_KEY, sourceRequest.getSource().key());
        TranslatorUtils.from(dbo, USAGE_TYPE_KEY, sourceRequest.getUsageType().toString());
        TranslatorUtils.from(dbo, EMAIL_KEY, sourceRequest.getEmail());
        TranslatorUtils.from(dbo, APPURL_KEY, sourceRequest.getAppUrl());
        TranslatorUtils.from(dbo, REASON_KEY, sourceRequest.getReason());
        TranslatorUtils.from(dbo, APPROVED_KEY, sourceRequest.isApproved());
        return dbo;
    }
    
    public SourceRequest fromDBObject(DBObject dbo) {
        if (dbo == null) {
            return null;
        }
        return SourceRequest.builder()
                .withAppSlug(TranslatorUtils.toString(dbo, APPSLUG_KEY))
                .withSource(Publisher.fromKey(TranslatorUtils.toString(dbo, SOURCE_KEY)).requireValue())
                .withUsageType(UsageType.valueOf(TranslatorUtils.toString(dbo, USAGE_TYPE_KEY)))
                .withEmail(TranslatorUtils.toString(dbo, EMAIL_KEY))
                .withAppUrl(TranslatorUtils.toString(dbo, APPURL_KEY))
                .withReason(TranslatorUtils.toString(dbo, REASON_KEY))
                .withApproved(TranslatorUtils.toBoolean(dbo, APPROVED_KEY))
                .build();
    }
}
