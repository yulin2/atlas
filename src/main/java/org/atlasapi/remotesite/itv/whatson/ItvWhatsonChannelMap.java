package org.atlasapi.remotesite.itv.whatson;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;

import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.media.channel.Channel;

import com.google.common.collect.ForwardingMap;
import com.google.common.collect.ImmutableBiMap;


public class ItvWhatsonChannelMap extends ForwardingMap<String, Channel> {
    
    private ImmutableBiMap<String, Channel> delegate;

    public ItvWhatsonChannelMap(ChannelResolver resolver) {
        this.delegate = ImmutableBiMap.<String, Channel>builder()
                .put("ITV1", resolve(resolver, "http://www.itv.com/channels/itv1/london"))
                .put("ITV2", resolve(resolver, "http://www.itv.com/channels/itv2"))
                .put("ITV3", resolve(resolver, "http://www.itv.com/channels/itv3"))
                .put("ITV4", resolve(resolver, "http://www.itv.com/channels/itv4"))
                .put("CITV", resolve(resolver, "http://www.itv.com/channels/citv"))
                .build();
    }

    private Channel resolve(ChannelResolver resolver, String uri) {
        return checkNotNull(resolver.fromUri(uri), uri).requireValue();
    }
    
    @Override
    protected Map<String, Channel> delegate() {
        return delegate;
    }
    
}
