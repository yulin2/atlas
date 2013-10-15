package org.atlasapi.remotesite.bbc.nitro;

import java.util.Iterator;

import org.atlasapi.media.channel.Channel;
import org.joda.time.LocalDate;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

public class DayRangeChannelDaySupplier implements Supplier<ImmutableList<ChannelDay>>{

    private final Supplier<Range<LocalDate>> dayRangeSupplier;
    private final Supplier<? extends Iterable<Channel>> channelSupplier;

    public DayRangeChannelDaySupplier(Supplier<? extends Iterable<Channel>> channelSupplier, 
            Supplier<Range<LocalDate>> dayRangeSupplier) {
        this.channelSupplier = channelSupplier;
        this.dayRangeSupplier = dayRangeSupplier;
    }

    @Override
    public ImmutableList<ChannelDay> get() {
        final Range<LocalDate> dayRange = dayRangeSupplier.get();
        Preconditions.checkArgument(dayRange.hasLowerBound()
            && dayRange.hasUpperBound(), "Range must be bounded");
        final Iterator<Channel> channels = channelSupplier.get().iterator();
        
        if (!channels.hasNext() || dayRange.isEmpty()) {
            return ImmutableList.of();
        }
        
        ImmutableList.Builder<ChannelDay> channelDays = ImmutableList.builder();
        while (channels.hasNext())  {
            Channel channel = channels.next();
            
            LocalDate day = dayRange.lowerEndpoint();
            if (BoundType.OPEN.equals(dayRange.lowerBoundType())) {
                day = day.plusDays(1);
            }
            
            while (dayRange.contains(day)) {
                channelDays.add(new ChannelDay(channel, day));
            }
        }
        return channelDays.build();
    }

}
