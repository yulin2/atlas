package org.atlasapi.remotesite.bt.channels;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.atlasapi.media.channel.ChannelGroup;
import org.atlasapi.media.channel.ChannelGroupResolver;
import org.atlasapi.media.channel.ChannelGroupWriter;
import org.atlasapi.media.channel.Region;
import org.atlasapi.media.entity.Alias;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.remotesite.bt.channels.mpxclient.Entry;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;


public abstract class AbstractBtChannelGroupSaver {

    // deliberately internalising the codec, since there are many ID codecs flying around
    // and these are v4 style, lowercase only ones, used by the external feed. Anything
    // else would not pass muster
    private static final NumberToShortStringCodec codec = SubstitutionTableNumberCodec.lowerCaseOnly();
    
    private final ChannelGroupResolver channelGroupResolver;
    private final ChannelGroupWriter channelGroupWriter;
    private final Publisher publisher;
    
    public AbstractBtChannelGroupSaver(Publisher publisher, ChannelGroupResolver channelGroupResolver,
            ChannelGroupWriter channelGroupWriter) {
        this.publisher = checkNotNull(publisher);
        this.channelGroupResolver = checkNotNull(channelGroupResolver);
        this.channelGroupWriter = checkNotNull(channelGroupWriter);
    }

    protected void start() { };
    
    protected abstract List<String> keysFor(Entry channel);
    protected abstract Optional<Alias> aliasFor(String key);
    protected abstract String aliasUriFor(String key);
    protected abstract String titleFor(String key);
    
    public void update(Iterable<Entry> channels) {
        start();
        ImmutableMultimap.Builder<String, String> builder = ImmutableMultimap.builder();
        for (Entry channel : channels) {
            List<String> keys = keysFor(channel);
            
            for (String key : keys) {
                builder.put(key, channel.getGuid());
            }
        }
        updateChannelGroups(builder.build());
    }

    private void updateChannelGroups(ImmutableMultimap<String, String> keys) {
        
        for (Map.Entry<String, Collection<String>> entry : keys.asMap().entrySet()) {
            String aliasUri = aliasUriFor(entry.getKey());
            Optional<Alias> alias = aliasFor(entry.getKey());
            
            ChannelGroup channelGroup = getOrCreateChannelGroup(aliasUri, alias);
            channelGroup.setPublisher(publisher);
            channelGroup.addTitle(titleFor(entry.getKey()));
            channelGroup.setChannels(Iterables.transform(entry.getValue(), TO_NUMERIC_ID));
            channelGroupWriter.createOrUpdate(channelGroup);
        };
        
        
    }
    
    private ChannelGroup getOrCreateChannelGroup(String uri, Optional<Alias> alias) {
        Optional<ChannelGroup> channelGroup = channelGroupResolver.fromAlias(uri);
        
        if (channelGroup.isPresent()) {
            return channelGroup.get();
        }
        
        ChannelGroup group = new Region();
        group.setCanonicalUri(uri);
        
        if (alias.isPresent()) {
            group.setAliases(alias.asSet());
        }
        
        // Adding channels to channel groups requires that the channel group has 
        // and ID, so we save now.
        channelGroupWriter.createOrUpdate(group);
        return group;
    }
    
    private static Function<String, Long> TO_NUMERIC_ID = new Function<String, Long>() {

        @Override
        public Long apply(String input) {
            return codec.decode(input).longValue();
        }
        
    };
}
