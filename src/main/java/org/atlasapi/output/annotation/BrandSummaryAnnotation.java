package org.atlasapi.output.annotation;

import static org.atlasapi.output.Annotation.BRAND_SUMMARY;

import java.io.IOException;

import org.atlasapi.media.entity.Content;
import org.atlasapi.query.v4.schedule.FieldWriter;

import com.google.common.collect.ImmutableSet;


public class BrandSummaryAnnotation extends OutputAnnotation<Content> {

    public BrandSummaryAnnotation(IdentificationAnnotation idAnnotation) {
        super(BRAND_SUMMARY, Content.class, ImmutableSet.of(idAnnotation));
    }

    @Override
    public void write(Content entity, FieldWriter format) throws IOException {
        // TODO Auto-generated method stub
        
    }

}
