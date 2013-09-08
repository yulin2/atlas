package org.atlasapi.application.writers;

import java.io.IOException;
import java.util.Map.Entry;
import org.atlasapi.application.SourceStatus;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.output.EntityListWriter;
import org.atlasapi.output.FieldWriter;
import org.atlasapi.output.OutputContext;


public class ApplicationSourcesReadsWriter implements
        EntityListWriter<Entry<Publisher, SourceStatus>> {

    @Override
    public void write(Entry<Publisher, SourceStatus> entity, FieldWriter writer,
            OutputContext ctxt) throws IOException {
        writer.writeField("key", entity.getKey().key());
        writer.writeField("title", entity.getKey().title());
        //writer.writeField("restricted", entity.getValue().isRestricted());
        writer.writeField("state", entity.getValue().getState().toString().toLowerCase());
        writer.writeField("enabled", entity.getValue().isEnabled());
        
    }

    @Override
    public String fieldName(Entry<Publisher, SourceStatus> entity) {
        return "reads";
    }

    @Override
    public String listName() {
        return "reads";
    }

}
