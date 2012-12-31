package org.atlasapi.output.annotation;


import java.io.IOException;

import org.atlasapi.media.content.Content;
import org.atlasapi.output.writers.KeyPhraseWriter;
import org.atlasapi.query.v4.schedule.FieldWriter;
import org.atlasapi.query.v4.schedule.OutputContext;


public class KeyPhrasesAnnotation extends OutputAnnotation<Content> {

    public KeyPhrasesAnnotation() {
        super(Content.class);
    }

    @Override
    public void write(Content entity, FieldWriter writer, OutputContext ctxt) throws IOException {
        writer.writeList(new KeyPhraseWriter(), entity.getKeyPhrases(), ctxt);
    }

}
