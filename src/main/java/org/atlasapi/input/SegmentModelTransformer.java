package org.atlasapi.input;

import org.atlasapi.media.entity.Description;
import org.atlasapi.media.segment.SegmentEvent;
import org.atlasapi.media.segment.SegmentRef;
import org.joda.time.Duration;

public class SegmentModelTransformer {

    public SegmentEvent transform(org.atlasapi.media.entity.simple.SegmentEvent segmentEvent) {
        SegmentEvent complex = new SegmentEvent();
        complex.setPosition(segmentEvent.getPosition());
        complex.setIsChapter(segmentEvent.getIsChapter());
        complex.setDescription(new Description(segmentEvent.getTitle(),
                segmentEvent.getDescription(),
                "",
                ""));
        complex.setOffset(new Duration(segmentEvent.getOffset()));
        complex.setSegment(new SegmentRef(segmentEvent.getSegment().getId()));
        return complex;
    }
}
