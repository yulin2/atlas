package org.atlasapi.remotesite.bbc.nitro;

import java.util.Collection;
import java.util.Iterator;

import org.atlasapi.media.channel.Channel;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Range;
import com.metabroadcast.common.scheduling.ScheduledTask;
import com.metabroadcast.common.scheduling.UpdateProgress;

/**
 * {@link ScheduledTask} which processes a range of {@link Channel}s and
 * {@link LocalDate} days via a {@link ChannelDayProcessor}.
 */
public final class ChannelDayProcessingTask extends ScheduledTask {

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private final Supplier<Range<LocalDate>> dayGenerator;
    private final Supplier<? extends Collection<Channel>> channelSupplier;
    private final ChannelDayProcessor processor;
    private final boolean abortOnFailure;

    public ChannelDayProcessingTask(Supplier<? extends Collection<Channel>> channelSupplier, Supplier<Range<LocalDate>> daySupplier, ChannelDayProcessor processor, boolean abortOnFailure) {
        this.channelSupplier = channelSupplier;
        this.dayGenerator = daySupplier;
        this.processor = processor;
        this.abortOnFailure = abortOnFailure;
    }
    
    @Override
    protected void runTask() {
        UpdateProgress progress = UpdateProgress.START;
        Range<LocalDate> range = dayGenerator.get();
        Collection<Channel> channels = channelSupplier.get();
        int channelCount = channels.size();
        Iterator<Channel> channelsIter = channels.iterator();
        for(int i = 1; channelsIter.hasNext() && shouldContinue(); i++) {
            reportStatus(String.format("%s/%s %s", i, channelCount, progress.toString()));
            Channel channel = channelsIter.next();
            progress = processRangeForChannel(range, channel, progress, i, channelCount);
        }
    }

    private UpdateProgress processRangeForChannel(Range<LocalDate> range, Channel channel, UpdateProgress progress,  int i, int size) {
        Iterator<LocalDate> days = daysIn(range);
        while(days.hasNext() && shouldContinue()) {
            LocalDate day = days.next();
            progress = progress.reduce(processChannelDay(channel, day));
            reportStatus(String.format("%s/%s %s", i, size, progress.toString()));
        }
        return progress;
    }

    private UpdateProgress processChannelDay(Channel channel, LocalDate day) {
        try {
            return processor.process(channel, day);
        } catch (Exception e) {
            if (abortOnFailure) {
                throw Throwables.propagate(e);
            } else {
                log.error(String.format("%s %s", channel, day), e);
                return UpdateProgress.FAILURE;
            }
        }
    }

    private Iterator<LocalDate> daysIn(final Range<LocalDate> range) {
        Preconditions.checkArgument(range.hasLowerBound()&&range.hasUpperBound(), 
                "Range must be bounded");
        //TODO: investigate a DiscreteDomain<LocalDate> and then use ContiguousSet
        return new AbstractIterator<LocalDate>() {

            private LocalDate cur = range.lowerEndpoint();
            @Override
            protected LocalDate computeNext() {
                LocalDate val = cur;
                if (!range.contains(cur)) {
                    return endOfData();
                } else {
                    val = cur;
                }
                cur = cur.plusDays(1);
                return val;
            }
        };
    }
    
}
