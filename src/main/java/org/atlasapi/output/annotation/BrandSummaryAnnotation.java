package org.atlasapi.output.annotation;

import java.io.IOException;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Item.ContainerSummary;
import org.atlasapi.media.entity.ParentRef;
import org.atlasapi.media.entity.simple.BrandSummary;
import org.atlasapi.persistence.output.ContainerSummaryResolver;
import org.atlasapi.query.v4.schedule.EntityWriter;
import org.atlasapi.query.v4.schedule.FieldWriter;
import org.atlasapi.query.v4.schedule.OutputContext;

import com.google.common.base.Optional;


public class BrandSummaryAnnotation extends OutputAnnotation<Content> {

    private static final class ContainerSummaryWriter implements EntityWriter<Item> {

        private final ContainerSummaryResolver containerSummaryResolver;

        public ContainerSummaryWriter(ContainerSummaryResolver containerSummaryResolver) {
            this.containerSummaryResolver = containerSummaryResolver;
        }

        @Override
        public void write(Item entity, FieldWriter writer, OutputContext ctxt) throws IOException {
            ParentRef container = entity.getContainer();
            ContainerSummary summary = entity.getContainerSummary();
            writer.writeField("uri", container.getUri());
            if (summary != null) {
                writer.writeField("type", summary.getType());
                writer.writeField("title", summary.getTitle());
                writer.writeField("description", summary.getDescription());
                if (summary.getSeriesNumber() != null) {
                    writer.writeField("series_number", summary.getSeriesNumber());
                }
            } else {
                Optional<BrandSummary> simpleSummary = containerSummaryResolver.summarizeTopLevelContainer(container);
                if(simpleSummary.isPresent()) {
                    writer.writeField("type", simpleSummary.get().getType());
                    writer.writeField("title", simpleSummary.get().getTitle());
                    writer.writeField("description", simpleSummary.get().getDescription());
                }
            }
        }

        @Override
        public String fieldName() {
            return "container";
        }
    }

    private final ContainerSummaryWriter summaryWriter;

    public BrandSummaryAnnotation(ContainerSummaryResolver containerSummaryResolver) {
        super(Content.class);
        summaryWriter = new ContainerSummaryWriter(containerSummaryResolver);
    }

    @Override
    public void write(Content entity, FieldWriter writer, OutputContext ctxt) throws IOException {
        if (entity instanceof Item) {
            Item item = (Item) entity;
            if (item.getContainer() == null) {
                writer.writeField("container", null);
            } else {
                writer.writeObject(summaryWriter, item, ctxt);
            }
        }
    }

}