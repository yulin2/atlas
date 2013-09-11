package org.atlasapi.application.writers;

import java.io.IOException;
import org.atlasapi.application.model.SourceReadEntry;
import org.atlasapi.output.EntityListWriter;
import org.atlasapi.output.FieldWriter;
import org.atlasapi.output.OutputContext;

public class ApplicationSourcesReadsWriter implements
        EntityListWriter<SourceReadEntry> {

    @Override
    public void write(SourceReadEntry entity, FieldWriter writer,
            OutputContext ctxt) throws IOException {
        writer.writeField("key", entity.getPublisher().key());
        writer.writeField("title", entity.getPublisher().title());
        writer.writeField("state", entity.getSourceStatus().getState().toString().toLowerCase());
        writer.writeField("enabled", entity.getSourceStatus().isEnabled());
    }

    @Override
    public String fieldName(SourceReadEntry entity) {
        return "reads";
    }

    @Override
    public String listName() {
        return "reads";
    }

}
