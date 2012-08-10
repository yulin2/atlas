package org.atlasapi.remotesite.bbckiwisubtitles;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Iterator;
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
import org.atlasapi.persistence.topic.TopicStore;
import org.atlasapi.remotesite.bbckiwisubtitles.S3KiwiSubtitlesTopicsClient.ItemTopics;
import org.atlasapi.remotesite.worldservice.model.WsTopics.TopicWeighting;
import org.elasticsearch.common.base.Throwables;
import org.elasticsearch.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.scheduling.ScheduledTask;

public class KiwiSubtitlesTopicsUpdater extends ScheduledTask {

    private static final Publisher PUBLISHER = Publisher.BBC_KIWI;
    private static final Logger log = LoggerFactory.getLogger(KiwiSubtitlesTopicsUpdater.class);

    private final S3KiwiSubtitlesTopicsClient topicsClient;
    private final ContentResolver contentResolver;
    private final ContentWriter contentWriter;
    private final AncilliaryContentFactory ancilliaryContentFactory;
    private final TopicStore topicStore;

    public KiwiSubtitlesTopicsUpdater(S3KiwiSubtitlesTopicsClient topicsClient, ContentResolver contentResolver, ContentWriter writer, AncilliaryContentFactory ancilliaryContentFactory, TopicStore topicStore) {
        this.topicsClient = topicsClient;
        this.contentResolver = contentResolver;
        this.contentWriter = writer;
        this.ancilliaryContentFactory = ancilliaryContentFactory;
        this.topicStore = topicStore;
    }

    @Override
    public void runTask()  {
        Iterator<ItemTopics> itemTopicIt = topicsClient.getTopics().iterator();
        while(itemTopicIt.hasNext() && shouldContinue()) {
            ItemTopics itemTopics = itemTopicIt.next();
            try {
                List<Identified> resolved = contentResolver.findByCanonicalUris(ImmutableSet.of(itemTopics.getUri())).getAllResolvedResults();
                if(resolved.isEmpty()) {
                    log.info("Ignoring item whose PID is {} as it is not present in the database", itemTopics.getUri());
                }
                else {
                    Content primaryContent = (Content) Iterables.getOnlyElement(resolved);
                    Content ancilliaryContent = ancilliaryContentFactory.fromPrimaryContent(primaryContent, PUBLISHER);
                    Builder<TopicRef> builder = ImmutableList.builder();
                    for(TopicWeighting topicWeighting : itemTopics.getTopicWeightings()) {
                        builder.add(topicRefFor(topicWeighting));                    
                    }
                    ancilliaryContent.setTopicRefs(builder.build());
                    writeContent(ancilliaryContent);
                }
            }
            catch(Exception e) {
                log.error(String.format("Failed to update topic refs for %s", itemTopics.getUri()), e);
            }
        }
    }

    private void writeContent(Content content) {
        if(content instanceof Container) {
            contentWriter.createOrUpdate((Container) content);
        }
        else {
            contentWriter.createOrUpdate((Item) content);
        }
    }

    private TopicRef topicRefFor(TopicWeighting topicWeighting) {
        try {
            String topicName = URLDecoder.decode(topicWeighting.getTopicValue(), Charsets.UTF_8.name());
            Topic topic = topicStore.topicFor(Publisher.DBPEDIA, Publisher.DBPEDIA.name().toLowerCase(), topicName).requireValue();
            topic.setNamespace(Publisher.DBPEDIA.name().toLowerCase());
            topic.setValue(topicName);
            topic.setPublisher(Publisher.DBPEDIA);
            topic.setTitle(topicName.substring(28).replace("_", " "));
            topic.setType(Topic.Type.SUBJECT);
            topicStore.write(topic);

            return new TopicRef(topic, topicWeighting.getWeighting(), false, TopicRef.Relationship.TRANSCRIPTION_SUBTITLES);
        } catch (UnsupportedEncodingException e) {
            Throwables.propagate(e);
            return null;
        }
    }

}
