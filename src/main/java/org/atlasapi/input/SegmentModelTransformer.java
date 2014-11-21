package org.atlasapi.input;


import com.google.common.base.Strings;
import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;

import org.atlasapi.media.SegmentType;
import org.atlasapi.media.entity.Description;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.simple.PublisherDetails;
import org.atlasapi.media.segment.Segment;
import org.atlasapi.media.segment.SegmentEvent;
import org.atlasapi.media.segment.SegmentRef;
import org.atlasapi.media.segment.SegmentWriter;
import org.joda.time.Duration;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Jamie on 19/11/2014.
 */
public class SegmentModelTransformer {

    private final SegmentWriter segmentWriter;
    private final NumberToShortStringCodec codec = new SubstitutionTableNumberCodec(); 
    
    public SegmentModelTransformer(SegmentWriter segmentWriter) {
        this.segmentWriter = checkNotNull(segmentWriter);
    }

    public SegmentEvent transform(org.atlasapi.media.entity.simple.SegmentEvent simple, PublisherDetails publisher) {
        checkArgument(Strings.isNullOrEmpty(simple.getUri()), "You must specify a URI on the item");
        checkArgument(simple.getSegment() != null, "You must specify a Segment on the SegmentEvent");
        SegmentEvent complex = new SegmentEvent();
        complex.setCanonicalUri(simple.getUri());
        complex.setDescription(Description.description()
                .withSynopsis(simple.getDescription())
                .withTitle(simple.getTitle())
                .build());
        complex.setPosition(simple.getPosition());
        if (simple.getOffset() != null) {
            complex.setOffset(Duration.standardMinutes(simple.getOffset()));
        }
        complex.setIsChapter(simple.getIsChapter());
        if (Strings.isNullOrEmpty(simple.getSegment().getId())) {
            complex.setSegment(writeSegment(simple.getSegment(), publisher));
        } else {
            complex.setSegment(new SegmentRef(codec.decode(simple.getSegment().getId()).longValue()));
        }
        return complex;
    }

    private SegmentRef writeSegment(org.atlasapi.media.entity.simple.Segment segment, PublisherDetails publisher) {
        Segment complex = transform(segment, publisher);
        long id = segmentWriter.write(complex).getId();
        return new SegmentRef(id);
    }

    private Segment transform(org.atlasapi.media.entity.simple.Segment simple, PublisherDetails publisher) {
        checkArgument((simple.getDuration() != null && simple.getDuration() > 0),
                "You must specify a (positive) duration on the Segment");
        checkArgument(!Strings.isNullOrEmpty(simple.getSegmentType()),
                "You must specify a type of Segment");

        Segment complex = new Segment();
        complex.setPublisher(Publisher.fromKey(publisher.getKey()).requireValue());
        complex.setType(SegmentType.valueOf(simple.getSegmentType()));
        complex.setDuration(Duration.standardMinutes(simple.getDuration()));
        complex.setDescription(simple.getDescription());
        complex.setTitle(simple.getTitle());
        return complex;
    }
}
