package org.atlasapi.output.simple;

import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;

import org.atlasapi.media.channel.ChannelGroup;
import org.atlasapi.media.channel.ChannelGroupResolver;
import org.atlasapi.media.entity.simple.ChannelGroupSummary;

import com.google.common.collect.Iterables;
import com.metabroadcast.common.ids.NumberToShortStringCodec;


public class ChannelGroupSummarySimplifier {

    private final NumberToShortStringCodec channelIdCodec;

    public ChannelGroupSummarySimplifier(NumberToShortStringCodec channelIdCodec,
            ChannelGroupResolver channelGroupResolver) {
        this.channelIdCodec = checkNotNull(channelIdCodec);
    }
    
    public ChannelGroupSummary simplify(ChannelGroup channelGroup) {
        ChannelGroupSummary summary = new ChannelGroupSummary();
        summary.setId(channelIdCodec.encode(BigInteger.valueOf(channelGroup.getId())));
        
        summary.setAliases(Iterables.transform(
                channelGroup.getAliases(), IdentifiedModelSimplifier.TO_SIMPLE_ALIAS));
        summary.setTitle(channelGroup.getTitle());
        
        return summary; 
    }
}
