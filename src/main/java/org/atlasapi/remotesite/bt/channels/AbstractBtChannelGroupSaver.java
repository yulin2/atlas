package org.atlasapi.remotesite.bt.channels;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.channel.ChannelGroup;
import org.atlasapi.media.channel.ChannelGroupResolver;
import org.atlasapi.media.channel.ChannelGroupWriter;
import org.atlasapi.media.channel.ChannelNumbering;
import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.media.channel.ChannelWriter;
import org.atlasapi.media.channel.Region;
import org.atlasapi.media.entity.Alias;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.remotesite.bt.channels.mpxclient.Entry;
import org.slf4j.Logger;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;


public abstract class AbstractBtChannelGroupSaver {

    // deliberately internalising the codec, since there are many ID codecs flying around
    // and these are v4 style, lowercase only ones, used by the external feed. Anything
    // else would not pass muster
    private static final NumberToShortStringCodec codec = SubstitutionTableNumberCodec.lowerCaseOnly();
    
    private final ChannelGroupResolver channelGroupResolver;
    private final ChannelGroupWriter channelGroupWriter;
    private final ChannelResolver channelResolver;
    private final ChannelWriter channelWriter;
    private final Publisher publisher;
    private final Logger log;
    
    public AbstractBtChannelGroupSaver(Publisher publisher, ChannelGroupResolver channelGroupResolver,
            ChannelGroupWriter channelGroupWriter, ChannelResolver channelResolver, ChannelWriter channelWriter, 
            Logger log) {
        this.publisher = checkNotNull(publisher);
        this.channelGroupResolver = checkNotNull(channelGroupResolver);
        this.channelGroupWriter = checkNotNull(channelGroupWriter);
        this.channelResolver = checkNotNull(channelResolver);
        this.channelWriter = checkNotNull(channelWriter);
        this.log = checkNotNull(log);
    }

    protected void start() { };
    
    protected abstract List<String> keysFor(Entry channel);
    protected abstract Optional<Alias> aliasFor(String key);
    protected abstract String aliasUriFor(String key);
    protected abstract String titleFor(String key);
    
    public Set<String> update(Iterable<Entry> channels) {
        start();
        ImmutableMultimap.Builder<String, String> builder = ImmutableMultimap.builder();
        for (Entry channel : channels) {
            List<String> keys = keysFor(channel);
            
            for (String key : keys) {
                builder.put(key, channel.getGuid());
            }
        }
        return updateChannelGroups(builder.build());
    }

    private Set<String> updateChannelGroups(ImmutableMultimap<String, String> keys) {
        ImmutableSet.Builder<String> channelGroupUris = ImmutableSet.builder();
        for (Map.Entry<String, Collection<String>> entry : keys.asMap().entrySet()) {
            String aliasUri = aliasUriFor(entry.getKey());
            Optional<Alias> alias = aliasFor(entry.getKey());
            
            ChannelGroup channelGroup = getOrCreateChannelGroup(aliasUri, alias);
            channelGroup.setPublisher(publisher);
            channelGroup.addTitle(titleFor(entry.getKey()));
            
            Set<Long> currentChannels = Sets.newHashSet();
            for (String channelId : entry.getValue()) {
                Long numericId = TO_NUMERIC_ID.apply(channelId);
                currentChannels.add(numericId);
                Channel channel = Iterables.getOnlyElement(channelResolver.forIds(ImmutableSet.of(numericId)), null);
                if (channel != null) {
                    channel.addChannelNumber(ChannelNumbering.builder().withChannelGroup(channelGroup).build());
                    channelWriter.createOrUpdate(channel);
                } else {
                    log.warn("Could not resolve channel with ID " + channelId);
                }
            }
            removeOldChannelsInGroup(channelGroup, currentChannels);
            channelGroupWriter.createOrUpdate(channelGroup);
            channelGroupUris.add(channelGroup.getCanonicalUri());
        };
        return channelGroupUris.build();
    }
    
    private void removeOldChannelsInGroup(final ChannelGroup channelGroup, Set<Long> currentChannels) {
        Set<Long> removedChannels = 
                Sets.difference(
                        ImmutableSet.copyOf(Iterables.transform(channelGroup.getChannelNumberings(), ChannelNumbering.TO_CHANNEL)), 
                        currentChannels
                );
        
        if (removedChannels.isEmpty()) {
            return;
        }
        
        for (Channel channel : channelResolver.forIds(removedChannels)) {
            channel.setChannelNumbers(
                Iterables.filter(channel.getChannelNumbers(), new Predicate<ChannelNumbering>() {
    
                    @Override
                    public boolean apply(ChannelNumbering input) {
                        return !input.getChannelGroup().equals(channelGroup.getId());
                    }
                })
            );
            channelWriter.createOrUpdate(channel);
        }
        
    }

    private ChannelGroup getOrCreateChannelGroup(String uri, Optional<Alias> alias) {
        Optional<ChannelGroup> channelGroup = channelGroupResolver.channelGroupFor(uri);
        
        ChannelGroup group;
        if (channelGroup.isPresent()) {
            group = channelGroup.get();
        } else {
            group = new Region();
            group.setCanonicalUri(uri);
            // Adding channels to channel groups requires that the channel group has 
            // and ID, so we save now.
            channelGroupWriter.createOrUpdate(group);
        }
        
        
        if (alias.isPresent()) {
            group.setAliases(alias.asSet());
        }

        return group;
    }
    
    private static Function<String, Long> TO_NUMERIC_ID = new Function<String, Long>() {

        @Override
        public Long apply(String input) {
            return codec.decode(input).longValue();
        }
        
    };
}
