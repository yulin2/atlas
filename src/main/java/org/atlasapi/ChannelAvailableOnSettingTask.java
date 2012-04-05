package org.atlasapi;

import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.Iterables.addAll;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static com.metabroadcast.common.persistence.mongo.MongoBuilders.update;
import static com.metabroadcast.common.persistence.mongo.MongoBuilders.where;

import java.util.Set;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.media.channel.ChannelTranslator;
import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Function;
import com.google.common.collect.Sets;
import com.metabroadcast.common.scheduling.ScheduledTask;
import com.mongodb.DBCollection;

public class ChannelAvailableOnSettingTask extends ScheduledTask {
    
    private final ChannelResolver resolver;
    private final DBCollection collection;

    public ChannelAvailableOnSettingTask(ChannelResolver resolver, DBCollection collection) {
        this.resolver = resolver;
        this.collection = collection;
    }

    @Override
    protected void runTask() {
        for (Channel channel : resolver.all()) {
            Set<String> availableOn = Sets.newHashSet();
            
            availableOn.add(channel.broadcaster().key());
            
            addAll(availableOn, filter(transform(channel.getAliases(), TO_AVAILABLE_ON), notNull()));
            
            collection.update(where().idEquals(channel.getId()).build(), update().setField(ChannelTranslator.AVAILABLE_ON, availableOn).build());
        }
    }
    
    private Function<String, String> TO_AVAILABLE_ON = new Function<String, String>() {
        @Override
        public String apply(String input) {
            if (input.startsWith("http://pressassociation.com")) {
                return Publisher.PA.key();
            } else if (input.startsWith("http://xmltv.radiotimes.com")) {
                return Publisher.RADIO_TIMES.key();
            } else if (input.startsWith("http://devapi.bbcredux.com")) {
                return Publisher.BBC_REDUX.key();
            } else {
                return null;
            }
        }
    };

}
