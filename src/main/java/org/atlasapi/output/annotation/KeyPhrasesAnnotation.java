package org.atlasapi.output.annotation;


import java.io.IOException;

import org.atlasapi.media.content.Content;
import org.atlasapi.output.FieldWriter;
import org.atlasapi.output.OutputContext;
import org.atlasapi.output.writers.KeyPhraseWriter;


public class KeyPhrasesAnnotation extends OutputAnnotation<Content> {

    public KeyPhrasesAnnotation() {
        super(Content.class);
    }

    @Override
    public void write(Content entity, FieldWriter writer, OutputContext ctxt) throws IOException {
        writer.writeList(new KeyPhraseWriter(), entity.getKeyPhrases(), ctxt);
    }

}
