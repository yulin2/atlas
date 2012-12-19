package org.atlasapi.output.annotation;


import java.io.IOException;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.query.v4.schedule.EntityWriter;
import org.atlasapi.query.v4.schedule.FieldWriter;
import org.atlasapi.query.v4.schedule.OutputContext;


public class SeriesReferenceAnnotation extends OutputAnnotation<Content> {

    public static final class SeriesRefWriter implements EntityWriter<Episode> {
        
        @Override
        public void write(Episode entity, FieldWriter writer, OutputContext ctxt) throws IOException {
            writer.writeField("uri", entity.getSeriesRef().getUri());
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
