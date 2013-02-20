package org.atlasapi.output.annotation;


import java.io.IOException;

import org.atlasapi.media.content.Content;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.output.EntityWriter;
import org.atlasapi.output.FieldWriter;
import org.atlasapi.output.OutputContext;


public class SeriesReferenceAnnotation extends OutputAnnotation<Content> {

    public static final class SeriesRefWriter implements EntityWriter<Episode> {
        
        @Override
        public void write(Episode entity, FieldWriter writer, OutputContext ctxt) throws IOException {
            writer.writeField("uri", entity.getSeriesRef().getId());
        }

        @Override
        public String fieldName() {
            return "series";
        }
    }

    private final SeriesRefWriter seriesRefWriter;

    public SeriesReferenceAnnotation() {
        super(Content.class);
        seriesRefWriter = new SeriesRefWriter();
    }

    @Override
    public void write(Content content, FieldWriter writer, OutputContext ctxt) throws IOException {
        if (content instanceof Episode) {
            Episode episode = (Episode) content;
            if (episode.getSeriesRef() == null) {
                writer.writeField("series", null);
            } else {
                writer.writeObject(seriesRefWriter, episode, ctxt);
            }
        }
    }

}
