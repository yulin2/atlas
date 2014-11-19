package org.atlasapi.input;

import static com.google.common.base.Preconditions.checkArgument;

import org.atlasapi.media.entity.Description;
import org.atlasapi.media.segment.SegmentEvent;
import org.atlasapi.media.segment.SegmentRef;
import org.joda.time.Duration;

import com.google.api.client.repackaged.com.google.common.base.Strings;

public class SegmentModelTransformer {

    public SegmentEvent transform(org.atlasapi.media.entity.simple.SegmentEvent segmentEvent) {
        checkArgument((segmentEvent.getSegment() != null
                && segmentEvent.getSegment().getId() != null),
                    "You must specify a segment ID");

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
