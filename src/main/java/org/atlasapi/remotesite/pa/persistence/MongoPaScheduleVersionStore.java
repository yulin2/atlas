package org.atlasapi.remotesite.pa.persistence;

import org.atlasapi.media.channel.Channel;
import org.joda.time.LocalDate;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.persistence.mongo.MongoConstants;
import com.metabroadcast.common.persistence.translator.TranslatorUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class MongoPaScheduleVersionStore implements PaScheduleVersionStore {

    private static final String VERSION_KEY = "version";
    
    private final static Joiner joiner = Joiner.on('|');
    private final DBCollection scheduleVersions;

    public MongoPaScheduleVersionStore(DatabasedMongo mongo) {
        scheduleVersions = mongo.collection("paScheduleVersion");
    }
    
    @Override
    public Optional<Long> get(Channel channel, LocalDate scheduleDay) {
        DBObject dbo = scheduleVersions.findOne(idFor(channel, scheduleDay));
        if(dbo != null) {
            return Optional.of(TranslatorUtils.toLong(dbo, VERSION_KEY));
        }
        else {
            return Optional.absent();
        }
    }
    
    @Override
    public void store(Channel channel, LocalDate scheduleDay, long version) {
        DBObject dbo = new BasicDBObject();
        TranslatorUtils.from(dbo, MongoConstants.ID, idFor(channel, scheduleDay));
        TranslatorUtils.from(dbo, VERSION_KEY, version);
        scheduleVersions.save(dbo);
    }
    
    private String idFor(Channel channel, LocalDate scheduleDay) {
        return joiner.join(channel.getKey(), scheduleDay.toString());
    }
}
