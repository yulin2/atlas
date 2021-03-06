package org.atlasapi.input;

import static com.google.common.base.Preconditions.checkNotNull;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.BlackoutRestriction;
import org.joda.time.DateTime;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;

public class BroadcastModelTransformer {

    private final SubstitutionTableNumberCodec codec = new SubstitutionTableNumberCodec();

    private final ChannelResolver channelResolver;

    public BroadcastModelTransformer(ChannelResolver channelResolver) {
        this.channelResolver = checkNotNull(channelResolver);
    }

    public Broadcast transform(org.atlasapi.media.entity.simple.Broadcast simple) {

        Broadcast complex = new Broadcast(resolveChannel(simple),
                new DateTime(simple.getTransmissionTime()),
                new DateTime(simple.getTransmissionEndTime()))
                .withId(simple.getId());
        
        if (simple.getActualTransmissionTime() != null) {
            complex.setActualTransmissionTime(new DateTime(simple.getActualTransmissionTime()));
        }
        if (simple.getActualTransmissionEndTime() != null) {
            complex.setActualTransmissionEndTime(new DateTime(simple.getActualTransmissionEndTime()));
        }
        complex.setScheduleDate(simple.getScheduleDate());
        complex.setRepeat(simple.getRepeat());
        complex.setSubtitled(simple.getSubtitled());
        complex.setSigned(simple.getSigned());
        complex.setAudioDescribed(simple.getAudioDescribed());
        complex.setHighDefinition(simple.getHighDefinition());
        complex.setWidescreen(simple.getWidescreen());
        complex.setSurround(simple.getSurround());
        complex.setLive(simple.getLive());
        complex.setAliasUrls(simple.getAliases());
        if (simple.getBlackoutRestriction() != null) {
            complex.setBlackoutRestriction(new BlackoutRestriction(simple.getBlackoutRestriction().getAll()));
        }
        return complex;
    }

    private String resolveChannel(org.atlasapi.media.entity.simple.Broadcast simple) {
        if (Strings.isNullOrEmpty(simple.getBroadcastOn())
            && !hasChannelId(simple)) {
            throw new IllegalArgumentException(
                    "Must specify a channel either through a channel object or broadcastOn channel URI");
        }

        if (!Strings.isNullOrEmpty(simple.getBroadcastOn()) && hasChannelId(simple)) {
            throw new IllegalArgumentException(
                    "Must not specify a channel ID and a channel URI. Supply only one.");
        }

        String broadcastOn = null;
        if (hasChannelId(simple)) {

            String channelId = simple.getChannel().getId();
            Iterable<Channel> channels = channelResolver.forIds(ImmutableSet.of(codec.decode(channelId)
                    .longValue()));
            if (Iterables.size(channels) != 1) {
                throw new IllegalArgumentException("Could not resolve channel ID " + channelId);
            }
            broadcastOn = Iterables.getOnlyElement(channels).getCanonicalUri();
        } else {
            broadcastOn = simple.getBroadcastOn();
        }
        return broadcastOn;
    }

    private boolean hasChannelId(org.atlasapi.media.entity.simple.Broadcast simple) {
        return simple.getChannel() != null
            && !Strings.isNullOrEmpty(simple.getChannel().getId());
    }
}
