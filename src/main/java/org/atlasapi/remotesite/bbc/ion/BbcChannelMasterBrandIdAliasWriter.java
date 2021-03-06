package org.atlasapi.remotesite.bbc.ion;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Map.Entry;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.channel.ChannelGroupStore;
import org.atlasapi.media.channel.ChannelStore;
import org.atlasapi.media.channel.MongoChannelGroupStore;
import org.atlasapi.media.channel.MongoChannelStore;
import org.atlasapi.media.entity.Alias;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.properties.Configurer;
import com.mongodb.Mongo;
import com.mongodb.ServerAddress;


public class BbcChannelMasterBrandIdAliasWriter {
    
    private static final String BBC_MASTERBRAND_ID_NAMESPACE = "bbc:masterbrand:id";
    private final Logger log = LoggerFactory.getLogger(BbcChannelMasterBrandIdAliasWriter.class);
    private final BiMap<String, String> uriToMasterBrandIdMap;
    private final ChannelStore channelStore;

    public BbcChannelMasterBrandIdAliasWriter(ChannelStore channelStore) {
        this.uriToMasterBrandIdMap = BbcIonServices.masterBrands.inverse();
        this.channelStore = checkNotNull(channelStore);
    }
    
    public void writeAliases() {
        log.info("Commencing BBC Masterbrand ID alias writing");
        for (Entry<String, String> uriToMasterbrandId : uriToMasterBrandIdMap.entrySet()) {
            Maybe<Channel> resolved = channelStore.fromUri(uriToMasterbrandId.getKey());
            if (resolved.isNothing()) {
                log.error("no channel found for uri {}", uriToMasterbrandId.getKey());
            } else {
                log.info("writing alias {} onto channel with uri {}", uriToMasterbrandId.getValue(), uriToMasterbrandId.getKey());
                Channel channel = resolved.requireValue();
                Alias sIdAlias = new Alias(BBC_MASTERBRAND_ID_NAMESPACE, uriToMasterbrandId.getValue()); 
                channel.addAlias(sIdAlias);
                channelStore.createOrUpdate(channel);
            }
        }
        log.info("BBC Masterbrand ID alias writing COMPLETE");
    }
    
    public static void main(String[] args) {
        String mongoHostStr = Configurer.get("mongo.host").get();
        String dbName = Configurer.get("mongo.dbName").get();
        DatabasedMongo mongo = new DatabasedMongo(configureMongo(mongoHostStr), dbName);
        ChannelGroupStore channelGroupStore = new MongoChannelGroupStore(mongo);
        ChannelStore mongoChannelStore = new MongoChannelStore(mongo, channelGroupStore, channelGroupStore);
        BbcChannelMasterBrandIdAliasWriter aliasWriter = new BbcChannelMasterBrandIdAliasWriter(mongoChannelStore);
        aliasWriter.writeAliases();
    }

    private static Mongo configureMongo(String mongoHostStr) {
        return new Mongo(mongoHosts(mongoHostStr));
    }

    private static List<ServerAddress> mongoHosts(String mongoHostStr) {
        Splitter splitter = Splitter.on(",").omitEmptyStrings().trimResults();
        return ImmutableList.copyOf(Iterables.filter(Iterables.transform(splitter.split(mongoHostStr), new Function<String, ServerAddress>() {

            @Override
            public ServerAddress apply(String input) {
                try {
                    return new ServerAddress(input, 27017);
                } catch (UnknownHostException e) {
                    return null;
                }
            }
        }), Predicates.notNull()));
    }
}
