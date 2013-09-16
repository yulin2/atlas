package org.atlasapi.application.writers;

import java.io.IOException;

import javax.annotation.Nonnull;

import org.atlasapi.application.model.SourceRequest;
import org.atlasapi.application.model.UsageType;
import org.atlasapi.application.sources.SourceIdCodec;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.output.EntityListWriter;
import org.atlasapi.output.EntityWriter;
import org.atlasapi.output.FieldWriter;
import org.atlasapi.output.OutputContext;


public class SourceRequestListWriter implements EntityListWriter<SourceRequest> {
    private final EntityListWriter<Publisher> sourcesWriter; 
    private final EntityWriter<UsageType> usageTypeWriter;
    
    public SourceRequestListWriter(SourceIdCodec sourceIdCodec) {
        sourcesWriter = new SourceWithIdWriter(sourceIdCodec, "source", "sources");
        usageTypeWriter = new UsageTypeWriter();
    }
    
    @Override
    public void write(SourceRequest entity, FieldWriter writer, OutputContext ctxt)
            throws IOException {
        // TODO output application id
        writer.writeField("application", entity.getAppSlug());
        writer.writeField("app_url", entity.getAppUrl());
        writer.writeField("email", entity.getEmail());
        writer.writeObject(sourcesWriter, entity.getSource(), ctxt);
        writer.writeField("reason", entity.getReason());
        writer.writeField("usage_type", entity.getUsageType());
        writer.writeField("approved",entity.isApproved());
    }

    @Override
    public String fieldName(SourceRequest entity) {
        return "source_request";
    }

    @Override
    public String listName() {
        return "source_requests";
    }

}
