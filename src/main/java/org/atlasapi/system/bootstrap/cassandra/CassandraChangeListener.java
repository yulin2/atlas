package org.atlasapi.system.bootstrap.cassandra;

import java.util.concurrent.ThreadPoolExecutor;

import org.atlasapi.media.content.Container;
import org.atlasapi.media.content.ContentStore;
import org.atlasapi.media.entity.Alias;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.topic.Topic;
import org.atlasapi.media.topic.TopicStore;
import org.atlasapi.system.bootstrap.AbstractMultiThreadedChangeListener;

/**
 */
public class CassandraChangeListener extends AbstractMultiThreadedChangeListener {

    private ContentStore cassandraContentStore;
    private TopicStore cassandraTopicStore;

    public CassandraChangeListener(int concurrencyLevel) {
        super(concurrencyLevel);
    }

    public CassandraChangeListener(ThreadPoolExecutor executor) {
        super(executor);
    }


    public void setCassandraContentStore(ContentStore cassandraContentStore) {
        this.cassandraContentStore = cassandraContentStore;
    }

    public void setCassandraTopicStore(TopicStore cassandraTopicStore) {
        this.cassandraTopicStore = cassandraTopicStore;
    }

    @Override
    protected void onChange(Object change) {
        if (change instanceof Item) {
            Item item = (Item) change;
            item.setReadHash(null);
            cassandraContentStore.writeContent(item);
        } else if (change instanceof Container) {
            Container container = (Container) change;
            container.setReadHash(null);
            cassandraContentStore.writeContent(container);
        } else if (change instanceof Topic) {
            Topic topic = (Topic) change;
            topic.addAlias(new Alias(topic.getNamespace(), topic.getValue()));
            cassandraTopicStore.writeTopic((Topic) change);
        } 
    }
}
