package org.atlasapi.output.annotation;


import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.simple.SeriesSummary;
import org.atlasapi.persistence.output.ContainerSummaryResolver;
import org.atlasapi.query.v4.schedule.EntityWriter;
import org.atlasapi.query.v4.schedule.FieldWriter;
import org.atlasapi.query.v4.schedule.OutputContext;

import com.google.common.base.Optional;

public class SeriesSummaryAnnotation extends OutputAnnotation<Content> {

    private final SeriesSummaryWriter summaryWriter;

    private final class SeriesSummaryWriter implements EntityWriter<Episode> {

        private final ContainerSummaryResolver containerSummaryResolver;
        
        
        public SeriesSummaryWriter(ContainerSummaryResolver containerSummaryResolver) {
            this.containerSummaryResolver = checkNotNull(containerSummaryResolver);
        }

        @Override
        public void write(Episode entity, FieldWriter writer, OutputContext ctxt) throws IOException {
            writer.writeField("uri", entity.getSeriesRef().getUri());
            Optional<SeriesSummary> possibleSummary = containerSummaryResolver.summarizeSeries(entity.getSeriesRef());
            if (possibleSummary.isPresent()) {
                SeriesSummary summary = possibleSummary.get();
                writer.writeField("type", summary.getType());
                writer.writeField("title", summary.getTitle());
                writer.writeField("description", summary.getDescription());
                writer.writeField("series_number", summary.getSeriesNumber());
            }
        }

        @Override
        public String fieldName() {
            return "series_summary";
        }
    }

    public SeriesSummaryAnnotation(ContainerSummaryResolver containerSummaryResolver) {
        super(Content.class);
        summaryWriter = new SeriesSummaryWriter(containerSummaryResolver);
    }

    @Override
    public void write(Content entity, FieldWriter writer, OutputContext ctxt) throws IOException {
        if (entity instanceof Episode) {
            Episode episode = (Episode) entity;
            if (episode.getSeriesRef() == null) {
                writer.writeField("series_summary", null);
            } else {
                writer.writeObject(summaryWriter, episode, ctxt);
            }
        }
    }

}
