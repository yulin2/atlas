package org.atlasapi.remotesite.bt.channels;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import org.atlasapi.media.channel.ChannelGroup;
import org.atlasapi.media.channel.ChannelGroupResolver;
import org.atlasapi.media.channel.ChannelGroupWriter;
import org.atlasapi.media.channel.Platform;
import org.atlasapi.media.channel.Region;
import org.atlasapi.media.entity.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.metabroadcast.common.scheduling.ScheduledTask;


public class BtAllChannelsChannelGroupUpdater extends ScheduledTask {

    private static final Logger log = LoggerFactory.getLogger(BtAllChannelsChannelGroupUpdater.class);
    private static final String ALIAS_SUFFIX = "allchannels";
    
    private final ChannelGroupResolver channelGroupResolver;
    private final ChannelGroupWriter channelGroupWriter;
    private final Long freeviewPlatformChannelGroupId;
    private final Long btChannelsChannelGroupId;
    private final String uriPrefix;

    private Publisher publisher;

    public BtAllChannelsChannelGroupUpdater(ChannelGroupWriter channelGroupWriter,
            ChannelGroupResolver channelGroupResolver, long freeviewPlatformChannelGroupId,
            long btChannelsChannelGroupId, String uriPrefix, Publisher publisher) {
        this.channelGroupResolver = checkNotNull(channelGroupResolver);
        this.channelGroupWriter = checkNotNull(channelGroupWriter);
        this.uriPrefix = checkNotNull(uriPrefix);
        this.publisher = checkNotNull(publisher);
        this.freeviewPlatformChannelGroupId = freeviewPlatformChannelGroupId;
        this.btChannelsChannelGroupId = btChannelsChannelGroupId;
    }

    @Override
    protected void runTask() {
        ChannelGroup freeviewPlatform = getChannelGroup(freeviewPlatformChannelGroupId);
        
        Set<Long> allChannels = getAllChannelsFromRegions((Platform) freeviewPlatform);
        Set<Long> btChannels = getChannelGroup(btChannelsChannelGroupId).getChannels();
        ChannelGroup allBtChannelsGroup = createOrQueryBtAllChannelsChannelGroup();
        
        Set<Long> newChannels = Sets.union(allChannels, btChannels);
        
        if (!newChannels.equals(allBtChannelsGroup.getChannels())) {
            allBtChannelsGroup.setChannels(newChannels);
            channelGroupWriter.createOrUpdate(allBtChannelsGroup);
        }
    }
    
    private Set<Long> getAllChannelsFromRegions(Platform platform) {
        Set<Long> channels = Sets.newHashSet();
        for (Long regionId : platform.getRegions()) {
            Optional<ChannelGroup> region = channelGroupResolver.channelGroupFor(regionId);
            if (region.isPresent()) {
                channels.addAll(region.get().getChannels());
            } else {
                log.error("Could not find region " + regionId);
            }
        }
        return channels;
    }
    
    private ChannelGroup getChannelGroup(long channelGroupId) {
        Optional<ChannelGroup> group = channelGroupResolver.channelGroupFor(channelGroupId);
        if (!group.isPresent()) {
            throw new IllegalStateException("Could not find channel group " + channelGroupId);
        }
        return group.get();
    }

    private ChannelGroup createOrQueryBtAllChannelsChannelGroup() {
        String canonicalUri = uriPrefix + ALIAS_SUFFIX;
        Optional<ChannelGroup> possibleGroup = channelGroupResolver.fromAlias(canonicalUri);
        
        if (possibleGroup.isPresent()) {
            return possibleGroup.get();
        }
        
        Region group = new Region();
        group.setCanonicalUri(canonicalUri);
        group.setAliasUrls(ImmutableSet.of(canonicalUri));
        group.setPublisher(publisher);
        group.addTitle("All BT channels");
        
        channelGroupWriter.createOrUpdate(group);
        return group;
    }
    
    
}
