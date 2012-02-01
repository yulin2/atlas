package org.atlasapi.output.simple;

import java.util.Set;

import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Topic;
import org.atlasapi.media.entity.simple.PublisherDetails;
import org.atlasapi.output.Annotation;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class TopicModelSimplifier extends IdentifiedModelSimplifier<Topic, org.atlasapi.media.entity.simple.Topic> {

    @Override
    public org.atlasapi.media.entity.simple.Topic simplify(Topic fullTopic, Set<Annotation> annotations) {
        org.atlasapi.media.entity.simple.Topic topic = new org.atlasapi.media.entity.simple.Topic();
        copyIdentifiedAttributesTo(fullTopic, topic, annotations);
        topic.setTitle(fullTopic.getTitle());
        topic.setDescription(fullTopic.getDescription());
        topic.setImage(fullTopic.getImage());
        topic.setThumbnail(fullTopic.getThumbnail());
        topic.setPublisher(toPublisherDetails(fullTopic.getPublisher()));
        topic.setType(fullTopic.getType().toString());
        topic.setValue(fullTopic.getValue());
        topic.setNamespace(fullTopic.getNamespace());
        return topic;
    }

}
