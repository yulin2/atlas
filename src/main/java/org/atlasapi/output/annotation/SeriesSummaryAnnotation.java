package org.atlasapi.output.annotation;


import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

import org.atlasapi.media.content.Content;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.simple.SeriesSummary;
import org.atlasapi.output.EntityWriter;
import org.atlasapi.output.FieldWriter;
import org.atlasapi.output.OutputContext;
import org.atlasapi.persistence.output.ContainerSummaryResolver;

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
            writer.writeField("uri", entity.getSeriesRef().getId());
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
        public String fieldName(Episode entity) {
            return "series";
        }
    }

    public SeriesSummaryAnnotation(ContainerSummaryResolver containerSummaryResolver) {
        super();
        summaryWriter = new SeriesSummaryWriter(containerSummaryResolver);
    }

    @Override
    public void write(Content entity, FieldWriter writer, OutputContext ctxt) throws IOException {
        if (entity instanceof Episode) {
            Episode episode = (Episode) entity;
            if (episode.getSeriesRef() == null) {
                writer.writeField("series", null);
            } else {
                writer.writeObject(summaryWriter, episode, ctxt);
            }
        }
    }

}
