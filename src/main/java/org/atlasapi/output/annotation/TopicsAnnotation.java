package org.atlasapi.output.annotation;

import static org.atlasapi.output.writers.SourceWriter.sourceWriter;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Topic;
import org.atlasapi.media.entity.TopicRef;
import org.atlasapi.persistence.topic.TopicQueryResolver;
import org.atlasapi.query.v4.schedule.EntityListWriter;
import org.atlasapi.query.v4.schedule.EntityWriter;
import org.atlasapi.query.v4.schedule.FieldWriter;
import org.atlasapi.query.v4.schedule.OutputContext;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;

public class TopicsAnnotation extends OutputAnnotation<Content> {

    private static final class TopicWriter implements EntityWriter<Topic> {

        private final SubstitutionTableNumberCodec idCodec;
        private final String topicUriBase;
        private static final EntityWriter<Publisher> SOURCE_WRITER = sourceWriter("source");

        public TopicWriter(SubstitutionTableNumberCodec idCodec, String localHostName) {
            this.idCodec = idCodec;
            this.topicUriBase = String.format("http://%s/topics/", localHostName);
        }

        @Override
        public void write(Topic topic, FieldWriter writer, OutputContext ctxt) throws IOException {
            String id = idCodec.encode(BigInteger.valueOf(topic.getId()));
            writer.writeField("id", id);
            writer.writeField("uri", topicUriBase + topic.getId());
            writer.writeField("namespace", topic.getNamespace());
            writer.writeField("value", topic.getValue());
            writer.writeField("type", topic.getType());
            writer.writeObject(SOURCE_WRITER, topic.getPublisher(), ctxt);
            writer.writeField("title", topic.getTitle());
            writer.writeField("description", topic.getDescription());
            writer.writeField("image", topic.getImage());
            writer.writeField("thumbnail", topic.getThumbnail());
        }

        @Override
        public String fieldName() {
            return "topic";
        }
    }

    private static final Function<TopicRef, Long> REF_TO_ID = new Function<TopicRef, Long>() {
        @Override
        public Long apply(TopicRef input) {
            return input.getTopic();
        }
    };
    private static Function<Topic, Long> TOPIC_ID = new Function<Topic, Long>() {

        @Override
        public Long apply(Topic input) {
            return input.getId();
        }
    };

    private final TopicQueryResolver topicResolver;
    private final TopicWriter topicWriter;
    
    
    public TopicsAnnotation(TopicQueryResolver topicResolver, String localHostName, SubstitutionTableNumberCodec idCodec) {
        super(Content.class);
        this.topicResolver = topicResolver;
        this.topicWriter = new TopicWriter(idCodec, localHostName);
    }

    private Iterable<Topic> resolve(List<Long> topicIds) {
        if (topicIds.isEmpty()) { // don't even ask (the resolver)
            return ImmutableList.of();
        }
        return topicResolver.topicsForIds(topicIds);
    }
    
    @Override
    public void write(Content entity, FieldWriter writer, OutputContext ctxt) throws IOException {
        List<TopicRef> topicRefs = entity.getTopicRefs();
        Iterable<Topic> topics = resolve(Lists.transform(topicRefs, REF_TO_ID));
        final Map<Long, Topic> topicsMap = Maps.uniqueIndex(topics, TOPIC_ID);
        
        writer.writeList(new EntityListWriter<TopicRef>() {

            @Override
            public void write(TopicRef entity, FieldWriter writer, OutputContext ctxt) throws IOException {
                writer.writeObject(topicWriter, topicsMap.get(entity.getTopic()), ctxt);
                writer.writeField("supervised", entity.isSupervised());
                writer.writeField("weighting", entity.getWeighting());
                writer.writeField("relationship", entity.getRelationship());
            }

            @Override
            public String listName() {
                return "topics";
            }

            @Override
            public String fieldName() {
                return "topicref";
            }
        }, topicRefs, ctxt);
    }

}
