package org.atlasapi.application.writers;

import java.io.IOException;
import org.atlasapi.application.sources.SourceIdCodec;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.output.EntityListWriter;
import org.atlasapi.output.FieldWriter;
import org.atlasapi.output.OutputContext;

public class ApplicationSourcesWritesWriter implements
        EntityListWriter<Publisher> {
    private final SourceIdCodec sourceIdCodec;

    public ApplicationSourcesWritesWriter(SourceIdCodec sourceIdCodec) {
        this.sourceIdCodec = sourceIdCodec;
    }

    @Override
    public void write(Publisher entity, FieldWriter writer,
            OutputContext ctxt) throws IOException {
        writer.writeField("id", sourceIdCodec.encode(entity));
        writer.writeField("key", entity.key());
        writer.writeField("name", entity.title());
        writer.writeField("country", entity.country());
    }

    @Override
    public String fieldName(Publisher entity) {
        return "writes";
    }

    @Override
    public String listName() {
        return "writes";
    }

}
