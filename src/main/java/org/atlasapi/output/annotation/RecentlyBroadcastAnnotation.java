package org.atlasapi.output.annotation;

import java.io.IOException;

import org.atlasapi.media.entity.Content;
import org.atlasapi.output.Annotation;
import org.atlasapi.query.v4.schedule.FieldWriter;

import com.google.common.collect.ImmutableSet;

public class RecentlyBroadcastAnnotation extends OutputAnnotation<Content> {

    public RecentlyBroadcastAnnotation(IdentificationAnnotation idAnnotation) {
        super(Annotation.RECENTLY_BROADCAST, Content.class, ImmutableSet.of(idAnnotation));
    }

    @Override
    public void write(Content entity, FieldWriter format) throws IOException {
        // TODO Auto-generated method stub
        
    }

}
