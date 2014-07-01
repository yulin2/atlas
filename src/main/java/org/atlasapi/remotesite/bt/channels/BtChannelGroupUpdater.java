package org.atlasapi.remotesite.bt.channels;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.atlasapi.media.channel.ChannelGroupResolver;
import org.atlasapi.media.channel.ChannelGroupWriter;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.remotesite.bt.channels.mpxclient.BtMpxClient;
import org.atlasapi.remotesite.bt.channels.mpxclient.BtMpxClientException;
import org.atlasapi.remotesite.bt.channels.mpxclient.PaginatedEntries;

import com.google.api.client.repackaged.com.google.common.base.Throwables;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.query.Selection;
import com.metabroadcast.common.scheduling.ScheduledTask;


public class BtChannelGroupUpdater extends ScheduledTask {

    private final BtMpxClient btMpxClient;
    private final List<AbstractBtChannelGroupSaver> channelGroupSavers;
    
    public BtChannelGroupUpdater(BtMpxClient btMpxClient, Publisher publisher, String aliasUriPrefix, 
            String aliasNamespace, ChannelGroupResolver channelGroupResolver, 
            ChannelGroupWriter channelGroupWriter) {
        
        channelGroupSavers = ImmutableList.of(
                new SubscriptionChannelGroupSaver(publisher, aliasUriPrefix, aliasNamespace, 
                        channelGroupResolver, channelGroupWriter),
                new TargetUserGroupChannelGroupSaver(publisher,  aliasUriPrefix, aliasNamespace, 
                        channelGroupResolver, channelGroupWriter, btMpxClient),
                new WatchableChannelGroupSaver(publisher, aliasUriPrefix, aliasNamespace, 
                        channelGroupResolver, channelGroupWriter),
                new OutputProtectionChannelGroupSaver(publisher, aliasUriPrefix, aliasNamespace, 
                        channelGroupResolver, channelGroupWriter));
        this.btMpxClient = checkNotNull(btMpxClient);
    }
    
    @Override
    protected void runTask() {
        try {
            PaginatedEntries entries = btMpxClient.getChannels(Optional.<Selection>absent());
            
            for (AbstractBtChannelGroupSaver saver : channelGroupSavers) {
                saver.update(entries.getEntries());
            }
        } catch (BtMpxClientException e) {
            throw Throwables.propagate(e);
        }
        
    }

}
