package org.atlasapi.query.v4.schedule;

import java.io.IOException;
import java.util.List;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.entity.ChannelSchedule;
import org.atlasapi.media.entity.Item;
import org.atlasapi.output.Annotation;
import org.atlasapi.output.AnnotationRegistry;
import org.atlasapi.output.annotation.OutputAnnotation;
import org.atlasapi.query.common.QueryContext;
import org.atlasapi.query.common.QueryResult;

public class ScheduleQueryResultWriter implements QueryResultWriter<ChannelSchedule> {

    private final class ScheduleChannelWriter implements EntityWriter<Channel> {

        @Override
        public void write(Channel entity, FieldWriter writer, OutputContext ctxt)
            throws IOException {
            List<OutputAnnotation<? super Channel>> annotations = ctxt.getAnnotations(Channel.class, Annotation.ID_SUMMARY);
            for (int i = 0; i < annotations.size(); i++) {
                annotations.get(i).write(entity, writer, ctxt);
            }
        }

        @Override
        public String fieldName() {
            return "channel";
        }
    }

    private final class ScheduleItemWriter implements EntityListWriter<Item> {

        @Override
        public void write(Item entity, FieldWriter writer, OutputContext ctxt) throws IOException {
            List<OutputAnnotation<? super Item>> annotations = ctxt.getAnnotations(entity.getClass(), Annotation.ID);
            for (int i = 0; i < annotations.size(); i++) {
                annotations.get(i).write(entity, writer, ctxt);
            }
        }

        @Override
        public String listName() {
            return "content";
        }

        @Override
        public String fieldName() {
            return "item";
        }
    }

    private final AnnotationRegistry registry;

    public ScheduleQueryResultWriter(AnnotationRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void write(QueryResult<ChannelSchedule> result, ResponseWriter writer) throws IOException {
        writer.startResponse();
        writeResult(result, writer);
        writer.finishResponse();
    }

    private void writeResult(QueryResult<ChannelSchedule> result, ResponseWriter writer)
        throws IOException {

        OutputContext ctxt = outputContext(result.getContext());

        ChannelSchedule channelSchedule = result.getOnlyResource();

        //TODO: train-wreck
        if (result.getContext().getAnnotations().contains(Annotation.LICENSE)) {
            writer.writeField("license",
                "In accessing this feed, you agree that you will only " +
                "access its contents for your own personal and non-commercial " +
                "use, and not for any commercial or other purposes, including " +
                "but not restricted to advertising or selling any goods or " +
                "services, including any third-party software applications " +
                "available to the general public.");
        }
        
        writer.writeObject(new ScheduleChannelWriter(), channelSchedule.channel(), ctxt);
        writer.writeList(new ScheduleItemWriter(), channelSchedule.items(), ctxt);
    }

    private OutputContext outputContext(QueryContext queryContext) {
        return new OutputContext(
            registry.activeAnnotations(queryContext.getAnnotations()),
            queryContext.getApplicationConfiguration()
        );
    }

}
