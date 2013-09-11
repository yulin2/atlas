package org.atlasapi.remotesite.five;

import java.util.Map;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.channel.ChannelResolver;

import com.google.common.collect.ForwardingMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;


public class FiveChannelMap extends ForwardingMap<String, Channel> {

    private static final String FIVE = "https://pdb.five.tv/internal/channels/C5";
    private static final String FIVER = "https://pdb.five.tv/internal/channels/C6";
    private static final String FIVE_USA = "https://pdb.five.tv/internal/channels/C7";
    
    private final ImmutableMap<String, Channel> delegate;
    
    public FiveChannelMap(ChannelResolver resolver) {
        this.delegate = ImmutableBiMap.<String, Channel>builder()
            .put(FIVE, resolve(resolver, "http://www.five.tv"))
            .put(FIVER, resolve(resolver, "http://www.five.tv/channels/fiver"))
            .put(FIVE_USA, resolve(resolver, "http://www.five.tv/channels/five-usa"))
        .build();
    }
    
    private Channel resolve(ChannelResolver resolver, String uri) {
        return resolver.fromUri(uri).requireValue();
    }

    @Override
    protected Map<String, Channel> delegate() {
        return delegate;
    }

}