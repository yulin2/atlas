package org.atlasapi.remotesite.bbc.ion;

import java.util.List;
import java.util.Map.Entry;

import org.atlasapi.media.segment.Segment;
import org.atlasapi.media.segment.SegmentEvent;
import org.atlasapi.persistence.media.segment.SegmentWriter;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.FetchException;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.bbc.BbcFeeds;
import org.atlasapi.remotesite.bbc.ion.model.IonSegmentEvent;
import org.atlasapi.remotesite.bbc.ion.model.IonSegmentEventFeed;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class BbcIonSegmentAdapter implements SiteSpecificAdapter<List<SegmentEvent>> {

    private static final String SEGMENT_FEED_PATTERN = "http://www.bbc.co.uk/iplayer/ion/segmentevent/list/version/%s/format/json";
    private final RemoteSiteClient<IonSegmentEventFeed> scheduleClient;
    private final BbcIonSegmentExtractor extractor;
    private final SegmentWriter segmentWriter;
    
    public BbcIonSegmentAdapter(RemoteSiteClient<IonSegmentEventFeed> scheduleClient, SegmentWriter segmentWriter) {
        this.scheduleClient = scheduleClient;
        this.segmentWriter = segmentWriter;
        this.extractor = new BbcIonSegmentExtractor();
    }
    
    @Override
    public List<SegmentEvent> fetch(String uri) {
        Preconditions.checkArgument(canFetch(uri));
        String pid = BbcFeeds.isBbcPid(uri) ? uri : BbcFeeds.pidFrom(uri);
        
        try {
            IonSegmentEventFeed segmentEventFeed = scheduleClient.get(String.format(SEGMENT_FEED_PATTERN, pid));
            
            ImmutableList.Builder<SegmentEvent> segmentEvents = ImmutableList.builder();
            
            for (IonSegmentEvent segmentEvent : segmentEventFeed.getBlocklist()) {
                Entry<SegmentEvent, Segment> extractedSegmentPairing = extractor.extract(segmentEvent);
                
                Segment identifiedSegment = segmentWriter.write(extractedSegmentPairing.getValue());
                SegmentEvent event = extractedSegmentPairing.getKey();
                event.setSegment(identifiedSegment.toRef());
                segmentEvents.add(event);
            }
            
            return segmentEvents.build();
        } catch (Exception e) {
            throw new FetchException(e.getMessage(), e);
        }
    }

    @Override
    public boolean canFetch(String uri) {
        return BbcFeeds.isBbcPid(uri) || BbcFeeds.isACanonicalSlashProgrammesUri(uri);
    }

}
