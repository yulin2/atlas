package org.atlasapi.remotesite.pa;

import static org.atlasapi.persistence.logging.AdapterLogEntry.errorEntry;

import java.util.Set;

import org.atlasapi.media.entity.Channel;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.channel4.epg.BroadcastTrimmer;
import org.atlasapi.remotesite.pa.PaBaseProgrammeUpdater.PaChannelData;
import org.atlasapi.remotesite.pa.bindings.ProgData;
import org.joda.time.Interval;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

public class PaChannelProcessJob implements Runnable {

    public static class PaChannelProcessJobBuilder {
        
        private final PaProgDataProcessor processor;
        private final BroadcastTrimmer trimmer;
        private final AdapterLog log;

        public PaChannelProcessJobBuilder(PaProgDataProcessor processor, BroadcastTrimmer trimmer, AdapterLog log) {
            this.processor = processor;
            this.trimmer = trimmer;
            this.log = log;
        }
        
        public PaChannelProcessJob buildFor(Set<String> currentlyProcessing, PaChannelData data) {
            return new PaChannelProcessJob(processor, trimmer, log, currentlyProcessing, data);
        }
        
    }
    
    private final PaProgDataProcessor processor;
    private final Set<String> currentlyProcessing;
    private final PaChannelData channelData;
    private final BroadcastTrimmer trimmer;
    private final AdapterLog log;

    public PaChannelProcessJob(PaProgDataProcessor processor, BroadcastTrimmer trimmer, AdapterLog log, Set<String> currentlyProcessing, PaChannelData channelData) {
        this.processor = processor;
        this.currentlyProcessing = currentlyProcessing;
        this.trimmer = trimmer;
        this.log = log;
        this.channelData = channelData;
    }

    @Override
    public void run() {
        Channel channel = channelData.channel();
        try {
            Set<String> acceptableBroadcastIds = Sets.newHashSet();
            for (ProgData programme : channelData.programmes()) {
                String programmeLock = lockIdentifier(programme);
                lock(programmeLock);
                try {
                    processor.process(programme, channel, channelData.zone(), channelData.lastUpdated());
                    acceptableBroadcastIds.add(PaHelper.getBroadcastId(programme.getShowingId()));
                } catch (Exception e) {
                    log.record(errorEntry().withCause(e).withSource(getClass()).withDescription("Error processing channel %s, prog id %s", channel.key(), programme.getProgId()));
                } finally {
                    unlock(programmeLock);
                }
            }
            if (trimmer != null) {
                trimmer.trimBroadcasts(new Interval(channelData.day(), channelData.day().plusDays(1)), channel, acceptableBroadcastIds);
            }
        } catch (Exception e) {
            log.record(errorEntry().withCause(e).withSource(getClass()).withDescription("Error processing channel %s", channel.key()));
        }
    }

    private void unlock(String programmeLock) {
        synchronized (currentlyProcessing) {
            currentlyProcessing.remove(programmeLock);
        }
    }

    private void lock(String programmeLock) throws InterruptedException {
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
