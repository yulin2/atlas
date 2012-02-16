package org.atlasapi.remotesite.voila;

import static org.atlasapi.persistence.logging.AdapterLogEntry.warnEntry;

import java.util.List;

import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Topic;
import org.atlasapi.media.entity.TopicRef;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.persistence.topic.TopicStore;
import org.atlasapi.remotesite.voila.ContentWords.ContentWordsList;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.net.HostSpecifier;
import com.metabroadcast.common.base.Maybe;

public class ContentTwitterTopicsUpdater {

    private final RemoteSiteClient<ContentWordsList> client;
    private final String contentWordsRequestBase;
    private final AdapterLog log;

    private final Joiner joiner = Joiner.on(',');
    private final ContentResolver contentResolver;
    private final TopicStore topicStore;
    private final ContentWriter contentWriter;

    public ContentTwitterTopicsUpdater(RemoteSiteClient<ContentWordsList> client, HostSpecifier cannonHost, Optional<Integer> cannonPort, ContentResolver contentResolver, TopicStore topicStore,
            ContentWriter contentWriter, AdapterLog log) {
        this.client = client;
        this.contentResolver = contentResolver;
        this.topicStore = topicStore;
        this.contentWriter = contentWriter;
        this.contentWordsRequestBase = String.format("http://%s%s/contentWords?ids=", cannonHost, cannonPort.isPresent() ? ":"+cannonPort.get() : "");
        this.log = log;
    }

    public void updateTopics(List<String> contentIds) {
        
        ContentWordsList contentWords = null;
        try {
            contentWords = client.get(joiner.appendTo(new StringBuilder(contentWordsRequestBase), contentIds).toString());
        } catch (Exception e) {
            log.record(AdapterLogEntry.errorEntry().withCause(e).withSource(getClass()).withDescription("Failed to get words for %s",joiner.join(contentIds)));
        }

        ResolvedContent resolvedContent = contentResolver.findByCanonicalUris(Iterables.transform(contentWords.getResults(), new Function<ContentWords, String>() {
            @Override
            public String apply(ContentWords input) {
                return input.getUri();
            }
        }));
        
        for (ContentWords contentWordSet : contentWords.getResults()) {
            Maybe<Identified> possibleContent = resolvedContent.get(contentWordSet.getUri());
            if(possibleContent.hasValue()) {
                ImmutableSet.Builder<TopicRef> topicRefs = ImmutableSet.builder();
                for (String word : ImmutableSet.copyOf(contentWordSet.getWords())) {
                    Maybe<Topic> possibleTopic = topicStore.topicFor("twitter", word);
                    if (possibleTopic.hasValue()) {
                        Topic topic = possibleTopic.requireValue();
                        topic.setPublisher(Publisher.METABROADCAST);
                        topic.setType(Topic.Type.SUBJECT);
                        topicStore.write(topic);
                        topicRefs.add(new TopicRef(topic, 1.0f, false));
                    }
                }
                Content content = (Content) possibleContent.requireValue();
                content.setTopicRefs(topicRefs.addAll(content.getTopicRefs()).build());
                if (content instanceof Container) {
                    contentWriter.createOrUpdate((Container)content);
                } else {
                    contentWriter.createOrUpdate((Item)content);
                }
            } else {
                log.record(warnEntry().withSource(getClass()).withDescription("Couldn't resolve content for %s", contentWordSet.getUri()));
            }
        }
        
    }

}
