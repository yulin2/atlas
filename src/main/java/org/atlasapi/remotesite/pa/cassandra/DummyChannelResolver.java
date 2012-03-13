package org.atlasapi.remotesite.pa.cassandra;

import com.metabroadcast.common.base.Maybe;
import java.util.HashMap;
import java.util.Map;
import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.Publisher;

/**
 */
public class DummyChannelResolver implements ChannelResolver {

    @Override
    public Maybe<Channel> fromUri(String uri) {
        return Maybe.just(new Channel(Publisher.PA, uri, uri, MediaType.AUDIO, uri));
    }

    @Override
    public Maybe<Channel> fromKey(String key) {
        return Maybe.just(new Channel(Publisher.PA, key, key, MediaType.AUDIO, key));
    }

    @Override
    public Maybe<Channel> fromId(long id) {
        return Maybe.just(new Channel(Publisher.PA, Long.toString(id), Long.toString(id), MediaType.AUDIO, Long.toString(id)));
    }

    @Override
    public Map<String, Channel> forAliases(String aliasPrefix) {
        Map<String, Channel> result = new HashMap<String, Channel>();
        result.put(aliasPrefix, new Channel(Publisher.PA, aliasPrefix, aliasPrefix, MediaType.AUDIO, aliasPrefix));
        return result;
    }

    @Override
    public Iterable<Channel> forIds(Iterable<Long> ids) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Iterable<Channel> all() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
