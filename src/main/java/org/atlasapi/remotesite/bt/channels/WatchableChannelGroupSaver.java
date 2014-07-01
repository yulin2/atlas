package org.atlasapi.remotesite.bt.channels;

import java.util.List;

import org.atlasapi.media.channel.ChannelGroupResolver;
import org.atlasapi.media.channel.ChannelGroupWriter;
import org.atlasapi.media.entity.Alias;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.remotesite.bt.channels.mpxclient.Entry;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;


public class WatchableChannelGroupSaver extends AbstractBtChannelGroupSaver {

    private final String aliasUriPrefix;

    public WatchableChannelGroupSaver(Publisher publisher, String aliasUriPrefix, 
            String aliasNamespace, ChannelGroupResolver channelGroupResolver, 
            ChannelGroupWriter channelGroupWriter) {
        super(publisher, channelGroupResolver, channelGroupWriter);
        
        this.aliasUriPrefix = aliasUriPrefix;
    }

    @Override
    protected List<String> keysFor(Entry channel) {
        if (channel.isStreamable()) {
            return ImmutableList.of("1");
        }
        return ImmutableList.of();
    }

    @Override
    protected Optional<Alias> aliasFor(String key) {
        return Optional.absent();
    }

    @Override
    protected String aliasUriFor(String key) {
        return aliasUriPrefix + "watchables";
    }

    @Override
    protected String titleFor(String key) {
        return "BT watchable channels";
    }

}
