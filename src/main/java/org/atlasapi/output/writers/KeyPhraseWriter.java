package org.atlasapi.output.writers;

import java.io.IOException;

import org.atlasapi.media.entity.KeyPhrase;
import org.atlasapi.query.v4.schedule.EntityListWriter;
import org.atlasapi.query.v4.schedule.FieldWriter;
import org.atlasapi.query.v4.schedule.OutputContext;

public final class KeyPhraseWriter implements EntityListWriter<KeyPhrase> {

    @Override
    public void write(KeyPhrase entity, FieldWriter writer, OutputContext ctxt) throws IOException {
        writer.writeField("phrase", entity.getPhrase());
        writer.writeField("weighting", entity.getWeighting());
    }

    @Override
    public String listName() {
        return "related_links";
    }

    @Override
    public String fieldName() {
        return "related_link";
    }
}