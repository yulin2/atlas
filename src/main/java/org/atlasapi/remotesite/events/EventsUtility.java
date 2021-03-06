package org.atlasapi.remotesite.events;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.atlasapi.media.entity.Publisher.DBPEDIA;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.atlasapi.media.entity.Topic;
import org.atlasapi.persistence.topic.TopicStore;
import org.joda.time.DateTime;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.base.Maybe;


public abstract class EventsUtility<S> {

    private static final String DBPEDIA_NAMESPACE = "dbpedia";
    
    private final Function<Entry<String, String>, Topic> topicLookup = new Function<Entry<String, String>, Topic>() {
        @Override
        public Topic apply(Entry<String, String> input) {
            return resolveOrCreateDbpediaTopic(input.getKey(), Topic.Type.SUBJECT, input.getValue());
        }
    };
    private final TopicStore topicStore;
    
    public EventsUtility(TopicStore topicStore)  {
        this.topicStore = checkNotNull(topicStore);
    }
    
    public abstract String createEventUri(String id);
    
    public abstract String createTeamUri(String id);
    
    /**
     * Where an end time has not been provided for an {@link org.atlasapi.media.entity.Event},
     * one can be estimated based on which sport is being played. 
     * @param start
     * @return An approximate end time for the event, or Optional.absent if the sport does 
     * not have a mapped duration
     */
    public abstract Optional<DateTime> createEndTime(S sport, DateTime start);
    
    /**
     * For a given location String, looks up a matching DBpedia Topic and returns 
     * it.
     * @param location
     * @return An Optional containing the Topic, or Optional.absent if no topic 
     * found for the provided location
     */
    public Optional<Topic> createOrResolveVenue(String location) {
        Optional<String> value = fetchLocationUrl(location);
        if (!value.isPresent()) {
            return Optional.absent();
        }
        return Optional.of(resolveOrCreateDbpediaTopic(location, Topic.Type.PLACE, value.get()));
    }
    
    public abstract Optional<String> fetchLocationUrl(String location);
    
    /**
     * For a given sport, looks up a set of DBpedia Topic value Strings associated
     * with that sport, parses them to Topics and returns.
     * <p> 
     * For example, Rugby might be associated with the topics for Rugby League and 
     * Rugby Football.
     * @param sport
     * @return Optional containing Set of dbpedia Topics, or Optional.absent if no
     * values found for the provided sport
     */
    public Optional<Set<Topic>> parseEventGroups(S sport) {
        Optional<Map<String, String>> eventGroups = fetchEventGroupUrls(sport);
        if (!eventGroups.isPresent()) {
            return Optional.absent();
        }
        return Optional.<Set<Topic>>of(ImmutableSet.copyOf(Iterables.transform(eventGroups.get().entrySet(), topicLookup)));
    }
    
    public abstract Optional<Map<String, String>> fetchEventGroupUrls(S sport);
    
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
