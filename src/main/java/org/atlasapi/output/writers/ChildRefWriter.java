package org.atlasapi.output.writers;

import java.io.IOException;

import org.atlasapi.media.entity.ChildRef;
import org.atlasapi.output.EntityListWriter;
import org.atlasapi.output.FieldWriter;
import org.atlasapi.output.OutputContext;

public final class ChildRefWriter implements EntityListWriter<ChildRef> {

    private final String listName;

    public ChildRefWriter(String listName) {
        this.listName = listName;
    }

    @Override
    public void write(ChildRef entity, FieldWriter writer, OutputContext ctxt) throws IOException {
        writer.writeField("uri", entity.getId());
        writer.writeField("type", entity.getType());
    }

    @Override
    public String listName() {
        return listName;
    }

    @Override
    public String fieldName() {
        return "content";
    }
}