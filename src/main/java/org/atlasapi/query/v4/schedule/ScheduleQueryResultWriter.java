package org.atlasapi.query.v4.schedule;

import static org.atlasapi.output.Annotation.IDENTIFICATION;
import static org.atlasapi.output.Annotation.IDENTIFICATION_SUMMARY;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.entity.ChannelSchedule;
import org.atlasapi.media.entity.Item;
import org.atlasapi.output.Annotation;
import org.atlasapi.output.AnnotationRegistry;
import org.atlasapi.output.annotation.AvailableLocationsAnnotation;
import org.atlasapi.output.annotation.BrandSummaryAnnotation;
import org.atlasapi.output.annotation.BroadcastsAnnotation;
import org.atlasapi.output.annotation.ChannelAnnotation;
import org.atlasapi.output.annotation.ChannelsAnnotation;
import org.atlasapi.output.annotation.ClipsAnnotation;
import org.atlasapi.output.annotation.ContentGroupsAnnotation;
import org.atlasapi.output.annotation.DescriptionAnnotation;
import org.atlasapi.output.annotation.ExtendedDescriptionAnnotation;
import org.atlasapi.output.annotation.ExtendedIdentificationAnnotation;
import org.atlasapi.output.annotation.FilteringResourceAnnotation;
import org.atlasapi.output.annotation.FirstBroadcastAnnotation;
import org.atlasapi.output.annotation.IdentificationAnnotation;
import org.atlasapi.output.annotation.IdentificationSummaryAnnotation;
import org.atlasapi.output.annotation.KeyPhrasesAnnotation;
import org.atlasapi.output.annotation.LocationsAnnotation;
import org.atlasapi.output.annotation.NextBroadcastAnnotation;
import org.atlasapi.output.annotation.OutputAnnotation;
import org.atlasapi.output.annotation.PeopleAnnotation;
import org.atlasapi.output.annotation.ProductsAnnotation;
import org.atlasapi.output.annotation.PublisherAnnotation;
import org.atlasapi.output.annotation.RecentlyBroadcastAnnotation;
import org.atlasapi.output.annotation.RelatedLinksAnnotation;
import org.atlasapi.output.annotation.SegmentEventsAnnotation;
import org.atlasapi.output.annotation.SeriesSummaryAnnotation;
import org.atlasapi.output.annotation.SubItemAnnotation;
import org.atlasapi.output.annotation.TopicsAnnotation;
import org.atlasapi.output.annotation.UpcomingAnnotation;

import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;

public class ScheduleQueryResultWriter implements QueryResultWriter<ScheduleQueryResult> {

    private final class ScheduleChannelWriter implements EntityWriter<Channel> {

        private final List<OutputAnnotation<? super Channel>> annotations;

        private ScheduleChannelWriter(List<OutputAnnotation<? super Channel>> annotations) {
            this.annotations = annotations;
        }

        @Override
        public void write(Channel entity, FieldWriter writer) throws IOException {
            for (int i = 0; i < annotations.size(); i++) {
                annotations.get(i).write(entity, writer);
            }
        }
    }

    private final class ScheduleItemWriter implements EntityListWriter<Item> {

        private final List<OutputAnnotation<? super Item>> annotations;

        private ScheduleItemWriter(List<OutputAnnotation<? super Item>> annotations) {
            this.annotations = annotations;
        }

        @Override
        public void write(Item entity, FieldWriter writer) throws IOException {
            for (int i = 0; i < annotations.size(); i++) {
                annotations.get(i).write(entity, writer);
            }
        }

        @Override
        public String listName() {
            return "content";
        }

        @Override
        public String elementName() {
            return "item";
        }
    }

    private final ResponseWriterFactory formatResolver = new ResponseWriterFactory();
    private final AnnotationRegistry registry;
    
    public ScheduleQueryResultWriter() {
        IdentificationSummaryAnnotation identSummary = new IdentificationSummaryAnnotation(SubstitutionTableNumberCodec.lowerCaseOnly());
        IdentificationAnnotation ident = new IdentificationAnnotation(identSummary);
        DescriptionAnnotation desc = new DescriptionAnnotation(ident);
        ExtendedIdentificationAnnotation extIdent = new ExtendedIdentificationAnnotation(ident);
        ImmutableSet<OutputAnnotation<?>> outputAnnotations = ImmutableSet.<OutputAnnotation<?>>builder()
            .add(identSummary)
            .add(ident)
            .add(extIdent)
            .add(desc)
            .add(new ExtendedDescriptionAnnotation(desc, extIdent))
            .add(new BrandSummaryAnnotation(ident))
            .add(new SeriesSummaryAnnotation(ident))
            .add(new SubItemAnnotation(ident))
            .add(new ClipsAnnotation(ident))
            .add(new PeopleAnnotation(ident))
            .add(new TopicsAnnotation(ident))
            .add(new ContentGroupsAnnotation(ident))
            .add(new SegmentEventsAnnotation(ident))
            .add(new RelatedLinksAnnotation(ident))
            .add(new KeyPhrasesAnnotation(ident))
            .add(new LocationsAnnotation(ident))
            .add(new BroadcastsAnnotation(ident))
            .add(new FirstBroadcastAnnotation(ident))
            .add(new NextBroadcastAnnotation(ident))
            .add(new AvailableLocationsAnnotation(ident))
            .add(new UpcomingAnnotation(ident))
            .add(new FilteringResourceAnnotation(ident))
            .add(new ChannelAnnotation(ident))
            .add(new ProductsAnnotation(ident))
            .add(new RecentlyBroadcastAnnotation(ident))
            .add(new ChannelsAnnotation(ident))
            .add(new PublisherAnnotation(ident))
            .build();
        this.registry = new AnnotationRegistry(outputAnnotations);
    }
    
    
    @Override
    public void write(ScheduleQueryResult result) throws IOException {
        
        HttpServletRequest request = result.getRequest();
        HttpServletResponse response = result.getResponse();

        ResponseWriter writer = formatResolver.writerFor(request, response);

        writer.startResponse(request, response);
        writeResult(result, writer);
        writer.finishResponse(request, response);
    }

    private void writeResult(ScheduleQueryResult result, ResponseWriter writer)
        throws IOException {

        ChannelSchedule channelSchedule = result.getChannelSchedule();

        Set<Annotation> annotations = result.getAnnotations();
        List<OutputAnnotation<? super Channel>> channelAnnotations = registry.map(annotations, Channel.class, IDENTIFICATION_SUMMARY);
        writer.writeObject("channel", new ScheduleChannelWriter(channelAnnotations), channelSchedule.channel());

        List<OutputAnnotation<? super Item>> itemAnnotations = registry.map(annotations, Item.class, IDENTIFICATION);
        writer.writeList(new ScheduleItemWriter(itemAnnotations),
            channelSchedule.items());
    }

    @Override // this shouldn't be here.
    public void writeError(ErrorResult errorResult) throws IOException {

        HttpServletRequest request = errorResult.getRequest();
        HttpServletResponse response = errorResult.getResponse();

        ResponseWriter writer = formatResolver.writerFor(request, response);
        
        writer.writeObject("error", new ErrorSummaryWriter(), errorResult.getErrorSummary());
        
    }

}
