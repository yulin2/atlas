package org.atlasapi.application.writers;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import org.atlasapi.application.SourceStatus;
import org.atlasapi.application.model.ApplicationSources;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.output.EntityListWriter;
import org.atlasapi.output.EntityWriter;
import org.atlasapi.output.FieldWriter;
import org.atlasapi.output.OutputContext;
import org.atlasapi.output.writers.SourceWriter;


public class ApplicationSourcesWriter implements EntityListWriter<ApplicationSources> {
    private final EntityListWriter<Entry<Publisher, SourceStatus>> readsWriter = new ApplicationSourcesReadsWriter();
    private final EntityListWriter<Publisher> writesWriter = SourceWriter.sourceListWriter("writes");
    @Override
    public void write(ApplicationSources entity, FieldWriter writer, OutputContext ctxt)
            throws IOException {
        writer.writeField("precedence", entity.isPrecedenceEnabled());
        writer.writeList(readsWriter, entity.getReads().entrySet(), ctxt);
        writer.writeList(writesWriter, entity.getWrites(), ctxt);
    }

    @Override
    public String fieldName(ApplicationSources entity) {
        return "sources";
    }

    @Override
    public String listName() {
        return "sources";
    }

}
