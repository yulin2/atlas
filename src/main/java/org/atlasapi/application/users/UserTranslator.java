package org.atlasapi.application.users;

import org.atlasapi.media.entity.Publisher;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.persistence.mongo.MongoConstants;
import com.metabroadcast.common.persistence.translator.TranslatorUtils;
import com.metabroadcast.common.social.model.translator.UserRefTranslator;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class UserTranslator {
    
    private final UserRefTranslator userTranslator;

    public UserTranslator(UserRefTranslator userTranslator) {
        this.userTranslator = userTranslator;
    }
    
    public DBObject toDBObject(User user) {
        if (user == null) {
            return null;
        }
        
        BasicDBObject dbo = new BasicDBObject();
        
        TranslatorUtils.from(dbo, MongoConstants.ID, user.getId());
        TranslatorUtils.from(dbo, "userRef", userTranslator.toDBObject(user.getUserRef()));
        TranslatorUtils.from(dbo, "apps", user.getApplications());
        TranslatorUtils.from(dbo, "manages", Iterables.transform(user.getSources(), Publisher.TO_KEY));
        TranslatorUtils.from(dbo, "role", user.getRole().toString().toLowerCase());
        
        return dbo;
    }
    
    public User fromDBObject(DBObject dbo) {
        if (dbo == null) {
            return null;
        }

        User user = new User(TranslatorUtils.toLong(dbo, MongoConstants.ID));
        
        user.setUserRef(userTranslator.fromDBObject(TranslatorUtils.toDBObject(dbo, "userRef")));
        user.setApplications(TranslatorUtils.toSet(dbo, "apps"));
        user.setSources(ImmutableSet.copyOf(Iterables.transform(TranslatorUtils.toSet(dbo, "manages"),Publisher.FROM_KEY)));
        user.setRole(Role.valueOf(TranslatorUtils.toString(dbo, "role").toUpperCase()));
        
        return user;
    }
    
}
