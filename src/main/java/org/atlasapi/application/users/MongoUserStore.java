package org.atlasapi.application.users;

import static com.metabroadcast.common.persistence.mongo.MongoConstants.ID;
import static com.metabroadcast.common.persistence.mongo.MongoConstants.SINGLE;
import static com.metabroadcast.common.persistence.mongo.MongoConstants.UPSERT;

import com.google.common.base.Optional;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.social.model.UserRef;
import com.metabroadcast.common.social.model.translator.UserRefTranslator;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class MongoUserStore implements UserStore {

    private DBCollection users;
    private UserTranslator translator;
    private UserRefTranslator userRefTranslator;

    public MongoUserStore(DatabasedMongo mongo) {
        this.users = mongo.collection("users");
        this.userRefTranslator = new UserRefTranslator();
        this.translator = new UserTranslator(userRefTranslator);
    }
    
    @Override
    public Optional<User> userForRef(UserRef ref) {
        return Optional.fromNullable(translator.fromDBObject(users.findOne(userRefTranslator.toQuery(ref, "userRef").build())));
    }

    @Override
    public Optional<User> userForId(Long userId) {
        return Optional.fromNullable(translator.fromDBObject(users.findOne(userId)));
    }

    @Override
    public void store(User user) {
        store(translator.toDBObject(user));
    }

    public void store(final DBObject dbo) {
        this.users.update(new BasicDBObject(ID, dbo.get(ID)), dbo, UPSERT, SINGLE);
    }

}
