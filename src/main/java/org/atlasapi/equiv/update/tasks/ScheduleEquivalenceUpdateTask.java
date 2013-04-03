package org.atlasapi.equiv.update.tasks;

import static org.atlasapi.feeds.utils.UpdateProgress.FAILURE;
import static org.atlasapi.feeds.utils.UpdateProgress.SUCCESS;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.equiv.update.EquivalenceUpdater;
import org.atlasapi.feeds.utils.UpdateProgress;
import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Schedule;
import org.atlasapi.media.entity.Schedule.ScheduleChannel;
import org.atlasapi.persistence.content.ScheduleResolver;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.scheduling.ScheduledTask;

public class ScheduleEquivalenceUpdateTask extends ScheduledTask {

    private final EquivalenceUpdater<Content> updater;
    private final ScheduleResolver scheduleResolver;
    private final List<Publisher> publishers;
    private final List<Channel> channels;
    private final Duration back;
    private final Duration forward;

    private final Logger log = LoggerFactory.getLogger(ScheduleEquivalenceUpdateTask.class);

    public static Builder builder() {
        return new Builder();
    }

    private ScheduleEquivalenceUpdateTask(EquivalenceUpdater<Content> updater,
            ScheduleResolver scheduleResolver, List<Publisher> publishers, List<Channel> channels,
            Duration back, Duration forward) {
        this.updater = updater;
        this.scheduleResolver = scheduleResolver;
        this.publishers = publishers;
        this.channels = channels;
        this.back = back;
        this.forward = forward;
    }

    @Override
    protected void runTask() {
        UpdateProgress progress = UpdateProgress.START;

        DateTime start = new DateTime().withZone(DateTimeZone.UTC).minus(back);
        DateTime end = new DateTime().withZone(DateTimeZone.UTC).plus(forward);
        
        while (end.isAfter(start.plusDays(1))) {
            progress = progress.reduce(equivalateSchedule(start, start.plusDays(1)));
            start = start.plusDays(1);
        } 
        
        progress = progress.reduce(equivalateSchedule(start, end));

        reportStatus(String.format("Finished. %d Items processed, %d failed", progress.getProcessed(), progress.getFailures()));
    }

    public UpdateProgress equivalateSchedule(DateTime start, DateTime end) {
        UpdateProgress progress = UpdateProgress.START;
        for (Publisher publisher : publishers) {
            for (Channel channel : channels) {
                Schedule schedule = scheduleResolver.schedule(
                        start,
                        end,
                        ImmutableList.of(channel),
                        ImmutableList.of(publisher),
                        Optional.<ApplicationConfiguration>absent());
                
                Iterator<ScheduleChannel> channelItr = schedule.scheduleChannels().iterator();
                while (channelItr.hasNext() && shouldContinue()) {
                    ScheduleChannel scheduleChannel = channelItr.next();
                    Iterator<Item> channelItems = scheduleChannel.items().iterator();
                    while (channelItems.hasNext() && shouldContinue()) {
                        Item scheduleItem = channelItems.next();
                        progress = progress.reduce(process(scheduleItem));
                        reportStatus(generateStatus(progress, publisher, scheduleItem, channel));
                    }
                }
            }   
        }
        return progress;
    }

    private String generateStatus(UpdateProgress progress, Publisher publisher, Item item, Channel channel) {
        return String.format(
            "Updating %s on %s, with publisher %s. Current progress: %d processed, %d failures",
            item.getCanonicalUri(),
            channel.getCanonicalUri(),
            publisher.name(),
            progress.getProcessed(),
            progress.getFailures()
        );
    }

    private UpdateProgress process(Item item) {
        try {
            updater.updateEquivalences(item);
            return SUCCESS;
        } catch (Exception e) {
            log.error("Error updating equivalences on " + item.getCanonicalUri(), e);
            return FAILURE;
        }
    }

    public static class Builder {

        private EquivalenceUpdater<Content> updater;
        private ScheduleResolver scheduleResolver;
        private List<Publisher> publishers;
        private List<Channel> channels;
        private Duration back;
        private Duration forward;

        public ScheduleEquivalenceUpdateTask build() {
            return new ScheduleEquivalenceUpdateTask(
                    updater,
                    scheduleResolver,
                    publishers,
                    channels,
                    back,
                    forward);
        }

        private Builder() {
        }

        public Builder withUpdater(EquivalenceUpdater<Content> updater) {
            this.updater = updater;
            return this;
        }

        public Builder withScheduleResolver(ScheduleResolver scheduleResolver) {
            this.scheduleResolver = scheduleResolver;
            return this;
        }

        public Builder withPublishers(Iterable<Publisher> publishers) {
            this.publishers = ImmutableList.copyOf(publishers);
            return this;
        }

        public Builder withChannels(Iterable<Channel> channels) {
            this.channels = ImmutableList.copyOf(channels);
            return this;
        }

        public Builder withBack(Duration back) {
            this.back = back;
            return this;
        }

        public Builder withForward(Duration forward) {
            this.forward = forward;
            return this;
        }
    }
}
