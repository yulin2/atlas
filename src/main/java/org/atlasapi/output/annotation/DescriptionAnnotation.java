package org.atlasapi.output.annotation;

import java.io.IOException;

import org.atlasapi.media.entity.Described;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.output.EntityWriter;
import org.atlasapi.output.FieldWriter;
import org.atlasapi.output.OutputContext;
import org.atlasapi.output.writers.SourceWriter;

public class DescriptionAnnotation<T extends Described> extends
        OutputAnnotation<T> {

    private final EntityWriter<Publisher> publisherWriter = SourceWriter.sourceWriter("source");

    @Override
    public void write(T entity, FieldWriter writer, OutputContext ctxt) throws IOException {
        writer.writeObject(publisherWriter, entity.getPublisher(), ctxt);
        
        writer.writeField("title", entity.getTitle());
        writer.writeField("description", entity.getDescription());
        writer.writeField("image", entity.getImage());
        writer.writeField("thumbnail", entity.getThumbnail());
        
    }

}