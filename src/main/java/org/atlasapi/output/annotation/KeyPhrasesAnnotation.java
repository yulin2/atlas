package org.atlasapi.output.annotation;

import static org.atlasapi.output.Annotation.KEY_PHRASES;

import java.io.IOException;

import org.atlasapi.media.entity.Content;
import org.atlasapi.query.v4.schedule.FieldWriter;

import com.google.common.collect.ImmutableSet;

public class KeyPhrasesAnnotation extends OutputAnnotation<Content> {

    public KeyPhrasesAnnotation(IdentificationAnnotation idAnnotation) {
        super(KEY_PHRASES, Content.class, ImmutableSet.of(idAnnotation));
    }

    @Override
    public void write(Content entity, FieldWriter format) throws IOException {
        // TODO Auto-generated method stub
        
    }

}
