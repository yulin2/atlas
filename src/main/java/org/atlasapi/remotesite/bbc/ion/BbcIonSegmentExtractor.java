package org.atlasapi.remotesite.bbc.ion;

import static org.atlasapi.media.content.Description.description;
import static org.joda.time.Duration.standardSeconds;

import java.util.Map.Entry;

import org.atlasapi.media.SegmentType;
import org.atlasapi.media.content.Description;
import org.atlasapi.media.content.Publisher;
import org.atlasapi.media.segment.Segment;
import org.atlasapi.media.segment.SegmentEvent;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.bbc.BbcFeeds;
import org.atlasapi.remotesite.bbc.ion.model.IonSegment;
import org.atlasapi.remotesite.bbc.ion.model.IonSegmentEvent;
import org.joda.time.Duration;

import com.google.common.collect.Maps;

public class BbcIonSegmentExtractor implements ContentExtractor<IonSegmentEvent, Entry<SegmentEvent, Segment>> {

    @Override
    public Entry<SegmentEvent, Segment> extract(IonSegmentEvent ionSegmentEvent) {
        return Maps.immutableEntry(partialSegmentEventFrom(ionSegmentEvent), segmentFrom(ionSegmentEvent.getSegment()));
    }

    private SegmentEvent partialSegmentEventFrom(IonSegmentEvent ionSegmentEvent) {
        SegmentEvent event = new SegmentEvent();
        event.setCanonicalUri(BbcFeeds.slashProgrammesUriForPid(ionSegmentEvent.getPid()));
        event.setPosition(ionSegmentEvent.getPosition());
        
        if (ionSegmentEvent.getOffset() != null) {
            event.setOffset(standardSeconds(ionSegmentEvent.getOffset()));
        }
        event.setIsChapter(ionSegmentEvent.getIsChapter());
        event.setDescription(descriptionOf(ionSegmentEvent));
        return event;
    }

    private Description descriptionOf(IonSegmentEvent ionSegmentEvent) {
        return description().withTitle(ionSegmentEvent.getTitle())
                .withShortSynopsis(ionSegmentEvent.getShortSynopsis())
                .withMediumSynopsis(ionSegmentEvent.getMediumSynopsis())
                .withLongSynopsis(ionSegmentEvent.getLongSynopsis()).build();
    }

    private Segment segmentFrom(IonSegment ionSegment) {
        final Segment segment = new Segment();
        segment.setCanonicalUri(BbcFeeds.slashProgrammesUriForPid(ionSegment.getPid()));
        segment.setPublisher(Publisher.BBC);
        segment.setDescription(descriptionOf(ionSegment));
        segment.setType(SegmentType.fromString(ionSegment.getSegmentType()).valueOrNull());
        if (ionSegment.getDuration() != null) {
            segment.setDuration(Duration.standardSeconds(ionSegment.getDuration()));
        }
        return segment;
    }

    private Description descriptionOf(IonSegment ionSegment) {
        return description().withTitle(ionSegment.getTitle())
                .withShortSynopsis(ionSegment.getShortSynopsis())
                .withMediumSynopsis(ionSegment.getMediumSynopsis())
                .withLongSynopsis(ionSegment.getLongSynopsis()).build();
    }

}
