package org.atlasapi.remotesite.events;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.atlasapi.media.entity.Publisher.DBPEDIA;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.atlasapi.media.entity.Topic;
import org.atlasapi.persistence.topic.TopicStore;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.base.Maybe;


public final class EventTopicResolver {

    private static final String DBPEDIA_NAMESPACE = "dbpedia";
    
    private final Function<Entry<String, String>, Topic> topicLookup = new Function<Entry<String, String>, Topic>() {
        @Override
        public Topic apply(Entry<String, String> input) {
            return resolveOrCreateDbpediaTopic(input.getKey(), Topic.Type.SUBJECT, input.getValue());
        }
    };
    private final TopicStore topicStore;
    
    public EventTopicResolver(TopicStore topicStore)  {
        this.topicStore = checkNotNull(topicStore);
    }
    
    /**
     * For a given title and location uri, looks up a matching DBpedia Topic and returns 
     * it.
     * @param title the title to give the resulting Topic, if creating
     * @param locationUri the location to resolve/create a Topic for
     */
    public Topic createOrResolveVenue(String title, String locationUri) {
        return resolveOrCreateDbpediaTopic(locationUri, Topic.Type.PLACE, locationUri);
    }
    
    /**
     * Each entry in the provided map of title -> location uri is resolved to a Topic,
     * or created if it does not exist. The resulting set of Topics is then returned.
     * @param sport
     * @return Optional containing Set of dbpedia Topics, or Optional.absent if no
     * values found for the provided sport
     */
    public Set<Topic> createOrResolveEventGroups(Map<String, String> eventGroups) {
        return ImmutableSet.copyOf(Iterables.transform(eventGroups.entrySet(), topicLookup));
    }
    
    private Topic resolveOrCreateDbpediaTopic(String title, Topic.Type topicType, String value) {
        Maybe<Topic> resolved = topicStore.topicFor(DBPEDIA_NAMESPACE, value);
        if (resolved.hasValue()) {
            Topic topic = resolved.requireValue();
            
            topic.setPublisher(DBPEDIA);
            topic.setTitle(title);
            topic.setType(topicType);
            
            topicStore.write(topic);
            
            return topic;
        }
        throw new IllegalStateException(String.format(
                "Topic store failed to create Topic with namespace %s and value %s", 
                DBPEDIA_NAMESPACE, 
                value
        ));
    }
}
