package org.atlasapi.remotesite.pa;

import java.util.HashSet;
import java.util.Set;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.content.Publisher;
import org.atlasapi.persistence.content.schedule.ScheduleWriter;
import org.atlasapi.persistence.content.schedule.ScheduleEntry.ItemRefAndBroadcast;
import org.atlasapi.remotesite.channel4.epg.BroadcastTrimmer;
import org.atlasapi.remotesite.pa.PaBaseProgrammeUpdater.PaChannelData;
import org.atlasapi.remotesite.pa.bindings.ProgData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

public class PaChannelProcessor {

    private static final Logger log = LoggerFactory.getLogger(PaChannelProcessor.class);
    private final PaProgDataProcessor processor;
    private final BroadcastTrimmer trimmer;
    private final ScheduleWriter scheduleWriter;

    public PaChannelProcessor(PaProgDataProcessor processor, BroadcastTrimmer trimmer, ScheduleWriter scheduleWriter) {
        this.processor = processor;
        this.trimmer = trimmer;
        this.scheduleWriter = scheduleWriter;
    }

    public int process(PaChannelData channelData, Set<String> currentlyProcessing) {
        int processed = 0;
        Set<ItemRefAndBroadcast> broadcasts = new HashSet<ItemRefAndBroadcast>();
        Channel channel = channelData.channel();
        try {
            Builder<String, String> acceptableBroadcastIds = ImmutableMap.builder();
            for (ProgData programme : channelData.programmes()) {
                String programmeLock = lockIdentifier(programme);
                lock(currentlyProcessing, programmeLock);
                try {
                    ItemRefAndBroadcast itemAndBroadcast = processor.process(programme, channel, channelData.zone(), channelData.lastUpdated());
                    if(itemAndBroadcast != null) {
	                    broadcasts.add(itemAndBroadcast);
	                    acceptableBroadcastIds.put(itemAndBroadcast.getBroadcast().getSourceId(),itemAndBroadcast.getItemUri());
                    }
                    processed++;
                } catch (Exception e) {
                    log.error(String.format("Error processing channel %s, prog id %s", channel.key(), programme.getProgId()));
                } finally {
                    unlock(currentlyProcessing, programmeLock);
                }
            }
            if (trimmer != null) {
                trimmer.trimBroadcasts(channelData.schedulePeriod(), channel, acceptableBroadcastIds.build());
            }
            scheduleWriter.replaceScheduleBlock(Publisher.PA, channel, broadcasts);
            
        } catch (Exception e) {
            //TODO: should we just throw e?
            log.error(String.format("Error processing channel %s", channel.key()));
        }
        return processed;
    }

    private void unlock(Set<String> currentlyProcessing, String programmeLock) {
        synchronized (currentlyProcessing) {
            currentlyProcessing.remove(programmeLock);
            currentlyProcessing.notifyAll();
        }
    }

    private void lock(Set<String> currentlyProcessing, String programmeLock) throws InterruptedException {
        synchronized (currentlyProcessing) {
            while (currentlyProcessing.contains(programmeLock)) {
                currentlyProcessing.wait();
            }
            currentlyProcessing.add(programmeLock);
        }
    }

    private String lockIdentifier(ProgData programme) {
        return Strings.isNullOrEmpty(programme.getSeriesId()) ? programme.getProgId() : programme.getSeriesId();
    }
}
