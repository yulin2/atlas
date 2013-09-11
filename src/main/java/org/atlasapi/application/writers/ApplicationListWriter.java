package org.atlasapi.application.writers;

import java.io.IOException;

import org.atlasapi.application.model.Application;
import org.atlasapi.application.model.ApplicationSources;
import org.atlasapi.output.EntityListWriter;
import org.atlasapi.output.FieldWriter;
import org.atlasapi.output.OutputContext;
import org.atlasapi.query.common.Resource;


public class ApplicationListWriter implements EntityListWriter<Application> {
    private final ApplicationCredentialsWriter credentialsWriter = new ApplicationCredentialsWriter();
    private final EntityListWriter<ApplicationSources> sourcesWriter = new ApplicationSourcesWriter();
    public ApplicationListWriter() {
        
    }
    
    @Override
    public void write(Application entity, FieldWriter writer, OutputContext ctxt)
            throws IOException {
        ctxt.startResource(Resource.APPLICATION);
        writer.writeField("id", entity.getId());
        writer.writeField("title", entity.getTitle());
        writer.writeField("created", entity.getCreated());
        writer.writeObject(credentialsWriter, entity.getCredentials(), ctxt);
        writer.writeObject(sourcesWriter, entity.getSources(), ctxt);
        ctxt.endResource();
        
    }

    @Override
    public String fieldName(Application entity) {
        return entity.getClass().getSimpleName().toLowerCase();
    }

    @Override
    public String listName() {
        return "applications";
    }

}
