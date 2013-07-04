package org.atlasapi.remotesite.metabroadcast;

import static com.metabroadcast.common.scheduling.UpdateProgress.FAILURE;
import static com.metabroadcast.common.scheduling.UpdateProgress.SUCCESS;

import java.util.List;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Clip;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Described;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Film;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.KeyPhrase;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Series;
import org.atlasapi.media.entity.Topic;
import org.atlasapi.media.entity.TopicRef;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.persistence.topic.TopicQueryResolver;
import org.atlasapi.persistence.topic.TopicStore;
import org.atlasapi.remotesite.metabroadcast.ContentWords.WordWeighting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.scheduling.UpdateProgress;

public abstract class AbstractMetaBroadcastContentUpdater {

    private final static Logger log = LoggerFactory.getLogger(AbstractMetaBroadcastContentUpdater.class);

    protected final String namespace;
    protected final TopicStore topicStore;
    protected final TopicQueryResolver topicResolver;
    protected final ContentWriter contentWriter;
    protected final ContentResolver contentResolver;
    protected final Publisher publisher;

    protected AbstractMetaBroadcastContentUpdater(ContentResolver contentResolver, TopicStore topicStore, TopicQueryResolver topicResolver, ContentWriter contentWriter, String namespace, Publisher publisher) {
        this.contentResolver = contentResolver;
        this.topicStore = topicStore;
        this.topicResolver = topicResolver;
        this.contentWriter = contentWriter;
        this.namespace = namespace;
        this.publisher = publisher;
    }

    protected UpdateProgress createOrUpdateContent(ResolvedContent resolvedContent, ResolvedContent resolvedMetaBroadcastContent, ContentWords contentWordSet, Optional<List<KeyPhrase>> keyPhrases) {
        try {
            String mbUri = generateMetaBroadcastUri(contentWordSet.getUri());
            log.debug("Processing content {}", mbUri);
            Maybe<Identified> possibleMetaBroadcastContent = resolvedMetaBroadcastContent.get(mbUri);
            if (possibleMetaBroadcastContent.hasValue()) { // Content exists, update it
                updateExistingContent(contentWordSet, possibleMetaBroadcastContent, keyPhrases);
            } else { // Generate new content
                createThenUpdateContent(resolvedContent, contentWordSet, mbUri, keyPhrases);
            }
            return SUCCESS;
        } catch (Exception e) {
            log.error("Topic update failed", e);
            return FAILURE;
        }
    }

    private void createThenUpdateContent(ResolvedContent resolvedContent, ContentWords contentWordSet, String mbUri, Optional<List<KeyPhrase>> keyPhrase) {
        String subjectUri = contentWordSet.getUri();
        Maybe<Identified> possibleContent = resolvedContent.get(subjectUri);
        if (possibleContent.hasValue()) {
            Identified identified = possibleContent.requireValue();
            String newCuri = ""; // TODO define a curie at some point
            Content content = getNewContent(identified, mbUri, newCuri);
            content.setTopicRefs(getTopicRefsFor(contentWordSet).addAll(filter(content.getTopicRefs())).build());
            if (keyPhrase.isPresent()) {
                content.setKeyPhrases(Lists.newArrayList(keyPhrase.get()));
            }
            content.addEquivalentTo((Described) identified); // TODO check
                                                             // equivalent to
            write(content);
        } else {
            throw new IllegalStateException("Couldn't resolve content " + subjectUri);
        }
    }

    private void updateExistingContent(ContentWords contentWordSet, Maybe<Identified> possibleMetaBroadcastContent, Optional<List<KeyPhrase>> keyPhrase) {
        Content content = (Content) possibleMetaBroadcastContent.requireValue();
        content.setTopicRefs(getTopicRefsFor(contentWordSet).addAll(filter(content.getTopicRefs())).build());
        if (keyPhrase.isPresent()) {
            content.setKeyPhrases(Lists.newArrayList(keyPhrase.get()));
        }
        write(content);
    }

    protected Content getNewContent(Identified originalContent, String newUri, String newCuri) {
        if (originalContent instanceof Brand) {
            return new Brand(newUri, newCuri, publisher);
        } else if (originalContent instanceof Series) {
            Series originalSeries = (Series) originalContent;
            Series series = new Series(newUri, newCuri, publisher);
            if (originalSeries.getParent() != null) {
                Brand brand = (Brand) getOrCreateContainer(originalSeries.getParent().getUri());
                series.setParent(brand);
            }
            return series;
        } else if (originalContent instanceof Clip) {
            return new Clip(newUri, newCuri, publisher);
        } else if (originalContent instanceof Episode) {
            Episode originalEpisode = (Episode) originalContent;
            Container brand = getOrCreateContainer(originalEpisode.getContainer().getUri());
            Episode episode = new Episode(newUri, newCuri, publisher);
            episode.setContainer(brand);
            return episode;
        } else if (originalContent instanceof Item) {
            return new Item(newUri, newCuri, publisher);
        } else if (originalContent instanceof Film) {
            return new Film(newUri, newCuri, publisher);
        }
        throw new IllegalArgumentException("Unrecognised type of content: " + originalContent.getClass().getName());
    }

    private Container getOrCreateContainer(String originalUri) {
        String auxDataUri = generateMetaBroadcastUri(originalUri);
        ResolvedContent content = contentResolver.findByCanonicalUris(ImmutableList.of(auxDataUri, originalUri));
        Container originalContent = (Container) content.get(originalUri).requireValue();
        Maybe<Identified> possibleAuxDataContainer = content.get(auxDataUri);
        Container container;
        if (possibleAuxDataContainer.hasValue()) {
            container = (Container) possibleAuxDataContainer.requireValue();
        } else {
            if (originalContent instanceof Brand) {
                container = new Brand(auxDataUri, "", publisher);
            } else if (originalContent instanceof Series) {
                container = new Series(auxDataUri, "", publisher);
            } else {
                throw new IllegalStateException(originalUri + " has unexpected type " + originalContent.getClass().getSimpleName());
            }
        }
        container.addEquivalentTo(originalContent);
        contentWriter.createOrUpdate(container);
        return container;
    }

    protected List<String> generateMetaBroadcastUris(Iterable<String> uris) {
        List<String> list = Lists.newArrayList();
        for (String uri : uris) {
            list.add(generateMetaBroadcastUri(uri));
        }
        return list;
    }

    protected String generateMetaBroadcastUri(String uri) {
        if (Publisher.VOILA.equals(publisher)) {
            return "http://voila.metabroadcast.com/" + uri.replaceFirst("(http(s?)://)", "");
        } else if (Publisher.MAGPIE.equals(publisher)) {
            return "http://magpie.metabroadcast.com/" + uri.replaceFirst("(http(s?)://)", "");
        } else {
            throw new IllegalArgumentException();
        }
    }

    private Iterable<? extends TopicRef> filter(List<TopicRef> topicRefs) {
        return Iterables.filter(topicRefs, new Predicate<TopicRef>() {
            @Override
            public boolean apply(TopicRef input) {
                return !input.getRelationship().equals(topicRefRelationship());
            }
        });
    }

    public void write(Content content) {
        if (content instanceof Container) {
            contentWriter.createOrUpdate((Container) content);
        } else {
            contentWriter.createOrUpdate((Item) content);
        }
    }

    public Builder<TopicRef> getTopicRefsFor(ContentWords contentWordSet) {
        String namespace = Publisher.DBPEDIA.name().toLowerCase();
        Builder<TopicRef> topicRefs = ImmutableSet.builder();
        for (WordWeighting wordWeighting : ImmutableSet.copyOf(contentWordSet.getWords())) {
            Topic topic = topicStore.topicFor(Publisher.DBPEDIA, namespace, topicValueFromWordWeighting(wordWeighting)).valueOrNull();
            if (topic == null) {
                throw new IllegalStateException("This should never happen, as topic is either found or created by the topic store, so failing fast.");
            } else {
                topic.setNamespace(namespace);
                topic.setTitle(wordWeighting.getContent());
                topic.setPublisher(Publisher.DBPEDIA);
                topic.setType(topicTypeFromSource(wordWeighting.getType()));
                topicStore.write(topic);
                topicRefs.add(new TopicRef(topic, wordWeighting.getWeight() / 100.0f, false, topicRefRelationship()));
            }
        }
        return topicRefs;
    }

    protected abstract TopicRef.Relationship topicRefRelationship();

    protected abstract Topic.Type topicTypeFromSource(String dbpedia);

    protected abstract String topicValueFromWordWeighting(WordWeighting weighting);
}
