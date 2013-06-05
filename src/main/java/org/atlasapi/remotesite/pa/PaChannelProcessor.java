package org.atlasapi.remotesite.pa;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.content.schedule.ScheduleHierarchy;
import org.atlasapi.media.content.schedule.ScheduleWriter;
import org.atlasapi.media.util.WriteException;
import org.atlasapi.remotesite.pa.PaBaseProgrammeUpdater.PaChannelData;
import org.atlasapi.remotesite.pa.listings.bindings.ProgData;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

public class PaChannelProcessor {

    private final PaProgDataProcessor processor;
    private final ScheduleWriter scheduleWriter;

    public PaChannelProcessor(ScheduleWriter scheduleWriter, PaProgDataProcessor processor) {
        this.scheduleWriter = checkNotNull(scheduleWriter);
        this.processor = checkNotNull(processor);
    }

    public int process(PaChannelData channelData) throws WriteException {
        int processed = 0;
        List<ScheduleHierarchy> extractedContent = Lists.newArrayListWithCapacity(channelData.programmes().size());
        Channel channel = channelData.channel();
        for (ProgData programme : channelData.programmes()) {
            Optional<ScheduleHierarchy> extracted = processor.process(programme, channel, channelData.zone(), channelData.lastUpdated());
            if (extracted.isPresent()) {
                processed++;
                extractedContent.add(extracted.get());
            }
        }
        scheduleWriter.writeSchedule(extractedContent, channel, channelData.schedulePeriod());
        return processed;
    }

}
