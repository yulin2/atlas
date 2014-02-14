package org.atlasapi.remotesite.five;

import java.util.Map;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.channel.ChannelResolver;

import com.google.common.collect.ForwardingMap;
import com.google.common.collect.ForwardingMultimap;
import com.google.common.collect.ForwardingSetMultimap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;


public class FiveChannelMap extends ForwardingSetMultimap<String, Channel> {

    private static final String FIVE = "https://pdb.five.tv/internal/channels/C5";
    private static final String FIVER = "https://pdb.five.tv/internal/channels/C6";
    private static final String FIVE_USA = "https://pdb.five.tv/internal/channels/C7";
    private static final String FIVE_PLUS_24 = "https://pdb.five.tv/internal/channels/C9";
    
    private final ImmutableSetMultimap<String, Channel> delegate;
    
    public FiveChannelMap(ChannelResolver resolver) {
        this.delegate = ImmutableSetMultimap.<String, Channel>builder()
            .put(FIVE, resolve(resolver, "http://www.five.tv"))
            .put(FIVER, resolve(resolver, "http://www.five.tv/channels/fiver"))
            .put(FIVE_USA, resolve(resolver, "http://www.five.tv/channels/five-usa"))
            .putAll(FIVE_PLUS_24, 
                resolve(resolver, "http://ref.atlasapi.org/channels/pressassociation.com/1859"),
                resolve(resolver, "http://ref.atlasapi.org/channels/pressassociation.com/1860"))
        .build();
    }
    
    private Channel resolve(ChannelResolver resolver, String uri) {
        return resolver.fromUri(uri).requireValue();
    }

    @Override
    protected SetMultimap<String, Channel> delegate() {
        return delegate;
    }

}
