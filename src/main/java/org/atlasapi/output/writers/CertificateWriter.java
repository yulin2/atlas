package org.atlasapi.output.writers;

import java.io.IOException;

import org.atlasapi.media.entity.Certificate;
import org.atlasapi.query.v4.schedule.EntityListWriter;
import org.atlasapi.query.v4.schedule.FieldWriter;
import org.atlasapi.query.v4.schedule.OutputContext;

public final class CertificateWriter implements EntityListWriter<Certificate> {

    @Override
    public void write(Certificate entity, FieldWriter writer, OutputContext ctxt) throws IOException {
        writer.writeField("classification", entity.classification());
        writer.writeField("code", entity.country().code());
    }

    @Override
    public String listName() {
        return "certificates";
    }

    @Override
    public String fieldName() {
        return "certificate";
    }
}