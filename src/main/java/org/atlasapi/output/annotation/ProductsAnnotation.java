package org.atlasapi.output.annotation;

import static org.atlasapi.output.Annotation.PRODUCTS;

import java.io.IOException;

import org.atlasapi.media.entity.Content;
import org.atlasapi.query.v4.schedule.FieldWriter;

import com.google.common.collect.ImmutableSet;

public class ProductsAnnotation extends OutputAnnotation<Content> {

    public ProductsAnnotation(IdentificationAnnotation idAnnotation) {
        super(PRODUCTS, Content.class, ImmutableSet.of(idAnnotation));
    }

    @Override
    public void write(Content entity, FieldWriter format) throws IOException {
        // TODO Auto-generated method stub
        
    }

}
