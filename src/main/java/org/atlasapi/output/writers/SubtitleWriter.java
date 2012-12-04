package org.atlasapi.output.writers;

import java.io.IOException;

import org.atlasapi.media.entity.Subtitles;
import org.atlasapi.query.v4.schedule.EntityListWriter;
import org.atlasapi.query.v4.schedule.FieldWriter;
import org.atlasapi.query.v4.schedule.OutputContext;

public final class SubtitleWriter implements EntityListWriter<Subtitles> {

    private LanguageWriter languageWriter;

    public SubtitleWriter(LanguageWriter languageWriter) {
        this.languageWriter = languageWriter;
    }

    @Override
    public void write(Subtitles entity, FieldWriter writer, OutputContext ctxt) throws IOException {
        languageWriter.write(entity.code(), writer, ctxt);
    }

    @Override
    public String listName() {
        return "subtitles";
    }

    @Override
    public String fieldName() {
        return "subtitles";
    }
}