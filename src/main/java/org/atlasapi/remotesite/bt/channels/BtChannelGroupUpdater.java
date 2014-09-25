package org.atlasapi.remotesite.bt.channels;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Set;

import org.atlasapi.media.channel.ChannelGroup;
import org.atlasapi.media.channel.ChannelGroupResolver;
import org.atlasapi.media.channel.ChannelGroupWriter;
import org.atlasapi.media.channel.ChannelNumbering;
import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.media.channel.ChannelWriter;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.remotesite.bt.channels.mpxclient.BtMpxClient;
import org.atlasapi.remotesite.bt.channels.mpxclient.BtMpxClientException;
import org.atlasapi.remotesite.bt.channels.mpxclient.PaginatedEntries;

import com.google.api.client.repackaged.com.google.common.base.Throwables;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.query.Selection;
import com.metabroadcast.common.scheduling.ScheduledTask;


public class BtChannelGroupUpdater extends ScheduledTask {

    private final BtMpxClient btMpxClient;
    private final List<AbstractBtChannelGroupSaver> channelGroupSavers;
    private final BtAllChannelsChannelGroupUpdater allChannelsGroupUpdater;
    private final ChannelGroupResolver channelGroupResolver;
    private final Publisher publisher;
    private final ChannelGroupWriter channelGroupWriter;
    
    public BtChannelGroupUpdater(BtMpxClient btMpxClient, Publisher publisher, String aliasUriPrefix, 
            String aliasNamespacePrefix, ChannelGroupResolver channelGroupResolver, 
            ChannelGroupWriter channelGroupWriter, ChannelResolver channelResolver, 
            ChannelWriter channelWriter, BtAllChannelsChannelGroupUpdater allChannelsGroupUpdater) {
        
        this.channelGroupWriter = checkNotNull(channelGroupWriter);
        this.publisher = checkNotNull(publisher);
        this.channelGroupResolver = checkNotNull(channelGroupResolver);
        this.allChannelsGroupUpdater = checkNotNull(allChannelsGroupUpdater);
        channelGroupSavers = ImmutableList.of(
                new SubscriptionChannelGroupSaver(publisher, aliasUriPrefix, aliasNamespacePrefix, 
                        channelGroupResolver, channelGroupWriter, channelResolver, channelWriter),
                new TargetUserGroupChannelGroupSaver(publisher,  aliasUriPrefix, aliasNamespacePrefix, 
                        channelGroupResolver, channelGroupWriter, btMpxClient, channelResolver, channelWriter),
                new WatchableChannelGroupSaver(publisher, aliasUriPrefix, aliasNamespacePrefix, 
                        channelGroupResolver, channelGroupWriter, channelResolver, channelWriter),
                new OutputProtectionChannelGroupSaver(publisher, aliasUriPrefix, aliasNamespacePrefix, 
                        channelGroupResolver, channelGroupWriter, channelResolver, channelWriter),
                new AllBtChannelsChannelGroupSaver(publisher, aliasUriPrefix, aliasNamespacePrefix, 
                        channelGroupResolver, channelGroupWriter, channelResolver, channelWriter)
                );
        this.btMpxClient = checkNotNull(btMpxClient);
    }
    
    @Override
    protected void runTask() {
        try {
            PaginatedEntries entries = btMpxClient.getChannels(Optional.<Selection>absent());
            ImmutableSet.Builder<String> allCurrentChannelGroups = ImmutableSet.builder();
            for (AbstractBtChannelGroupSaver saver : channelGroupSavers) {
                allCurrentChannelGroups.addAll(saver.update(entries.getEntries()));
            }
            removeOldChannelGroupChannels(allCurrentChannelGroups.build());
            allChannelsGroupUpdater.update();
        } catch (BtMpxClientException e) {
            throw Throwables.propagate(e);
        }
    }

    private void removeOldChannelGroupChannels(Set<String> allCurrentChannelGroups) {
        for (ChannelGroup channelGroup : channelGroupResolver.channelGroups()) {
            if (publisher.equals(channelGroup.getPublisher())
                    && !allCurrentChannelGroups.contains(channelGroup.getCanonicalUri())) {
                channelGroup.setChannelNumberings(ImmutableSet.<ChannelNumbering>of());
                channelGroupWriter.createOrUpdate(channelGroup);
            }
        }
    }

}
