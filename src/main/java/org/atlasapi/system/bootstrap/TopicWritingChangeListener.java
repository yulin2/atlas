package org.atlasapi.system.bootstrap;

import java.util.concurrent.ThreadPoolExecutor;

import org.atlasapi.media.entity.Alias;
import org.atlasapi.media.topic.Topic;
import org.atlasapi.media.topic.TopicStore;

public class TopicWritingChangeListener extends AbstractMultiThreadedChangeListener<Topic> {

    private final TopicStore topicStore;

    public TopicWritingChangeListener(int concurrencyLevel, TopicStore topicStore) {
        super(concurrencyLevel);
        this.topicStore = topicStore;
    }

    public TopicWritingChangeListener(ThreadPoolExecutor executor, TopicStore topicStore) {
        super(executor);
        this.topicStore = topicStore;
    }

    @Override
    protected void onChange(Topic topic) {
        topic.addAlias(new Alias(topic.getNamespace(), topic.getValue()));
        topicStore.writeTopic(topic);
    }
}
