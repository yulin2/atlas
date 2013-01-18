package org.atlasapi.output.annotation;

import java.io.IOException;

import org.atlasapi.media.content.Content;
import org.atlasapi.media.entity.Item;
import org.atlasapi.query.v4.schedule.EntityWriter;
import org.atlasapi.query.v4.schedule.FieldWriter;
import org.atlasapi.query.v4.schedule.OutputContext;

public class BrandReferenceAnnotation extends OutputAnnotation<Content> {

    public static final class BrandRefWriter implements EntityWriter<Item> {
        
        @Override
        public void write(Item entity, FieldWriter writer, OutputContext ctxt) throws IOException {
            writer.writeField("uri", entity.getContainer().getId());
        }

        @Override
        public String fieldName() {
            return "container";
        }
    }

    private final BrandRefWriter brandRefWriter;

    public BrandReferenceAnnotation() {
        super(Content.class);
        brandRefWriter = new BrandRefWriter();
    }

    @Override
    public void write(Content content, FieldWriter writer, OutputContext ctxt) throws IOException {
        if (content instanceof Item) {
            Item item = (Item) content;
            if (item.getContainer() == null) {
                writer.writeField("container", null);
            } else {
                writer.writeObject(brandRefWriter, item, ctxt);
            }
        }
    }

}
