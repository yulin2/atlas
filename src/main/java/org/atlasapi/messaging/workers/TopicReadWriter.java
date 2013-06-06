package org.atlasapi.messaging.workers;

import static com.google.common.base.Preconditions.checkNotNull;

import org.atlasapi.media.common.Id;
import org.atlasapi.media.topic.Topic;
import org.atlasapi.media.topic.TopicWriter;
import org.atlasapi.messaging.EntityUpdatedMessage;
import org.atlasapi.persistence.topic.TopicQueryResolver;

import com.metabroadcast.common.base.Maybe;


public class TopicReadWriter extends AbstractWorker {

    private final TopicQueryResolver reader;
    private final TopicWriter writer;

    public TopicReadWriter(TopicQueryResolver reader, TopicWriter writer) {
        this.reader = checkNotNull(reader);
        this.writer = checkNotNull(writer);
    }
    
    @Override
    public void process(EntityUpdatedMessage message) {
        Maybe<Topic> read = reader.topicForId(Id.valueOf(message.getEntityId()));
        if (read.hasValue()) {
            writer.writeTopic(read.requireValue());
        }
    }

}