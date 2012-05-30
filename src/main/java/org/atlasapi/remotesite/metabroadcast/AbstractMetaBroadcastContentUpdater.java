package org.atlasapi.remotesite.metabroadcast;

import static org.atlasapi.remotesite.redux.UpdateProgress.FAILURE;
import static org.atlasapi.remotesite.redux.UpdateProgress.SUCCESS;

import java.util.List;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Clip;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Film;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.KeyPhrase;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Series;
import org.atlasapi.media.entity.Topic;
import org.atlasapi.media.entity.TopicRef;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.topic.TopicQueryResolver;
import org.atlasapi.persistence.topic.TopicStore;
import org.atlasapi.remotesite.metabroadcast.ContentWords.ContentWordsList;
import org.atlasapi.remotesite.metabroadcast.ContentWords.WordWeighting;
import org.atlasapi.remotesite.redux.UpdateProgress;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.metabroadcast.common.base.Maybe;

public abstract class AbstractMetaBroadcastContentUpdater {

	private final String namespace;
	private final TopicStore topicStore;
	private final TopicQueryResolver topicResolver;
	private final ContentWriter contentWriter;
	protected final AdapterLog log;

	public abstract UpdateProgress updateTopics(List<String> contentIds);

	protected AbstractMetaBroadcastContentUpdater(TopicStore topicStore, TopicQueryResolver topicResolver, ContentWriter contentWriter, AdapterLog log, String namespace){
		this.topicStore = topicStore;
		this.topicResolver = topicResolver;
		this.contentWriter = contentWriter;
		this.log = log;
		this.namespace = namespace;
	}
	
	protected UpdateProgress createOrUpdateContent(ResolvedContent resolvedContent, ResolvedContent resolvedMetaBroadcastContent, 
			UpdateProgress result, ContentWords contentWordSet, Optional<List<KeyPhrase>> keyPhrases, Publisher publisher) {
		try {
			String mbUri = generateMetaBroadcastUri(contentWordSet.getUri());
			Maybe<Identified> possibleMetaBroadcastContent = resolvedMetaBroadcastContent.get(mbUri);
			if(possibleMetaBroadcastContent.hasValue()) {
				// Content exists, update it
				updateExistingContent(contentWordSet, possibleMetaBroadcastContent, keyPhrases);
				result = result.reduce(SUCCESS);
			} else {
				// Generate new content
				createThenUpdateContent(resolvedContent, result, contentWordSet, mbUri, keyPhrases, publisher);
				result = result.reduce(SUCCESS);
			}
		} catch (Exception e) {
			log.record(AdapterLogEntry.errorEntry().withCause(e).withSource(getClass()).withDescription("Failed to update topics for %s", contentWordSet.getUri()));
			result = result.reduce(FAILURE);
		}
		return result;
	}

	private void createThenUpdateContent(ResolvedContent resolvedContent, UpdateProgress result, ContentWords contentWordSet, String mbUri, 
			Optional<List<KeyPhrase>> keyPhrase, Publisher publisher) {
		String newCuri = ""; // TODO define a curie at some point
		Identified identified = resolvedContent.get(contentWordSet.getUri()).requireValue();
		Content content = getNewContent(identified, mbUri, newCuri, publisher);
		content.setTopicRefs(getTopicRefsFor(contentWordSet).addAll(filter(content.getTopicRefs())).build());
		if(keyPhrase.isPresent()){
			content.setKeyPhrases(Lists.newArrayList(keyPhrase.get()));
		}
		content.addEquivalentTo(identified); //TODO check equivalent to
		write(content);
	}

	private void updateExistingContent(ContentWords contentWordSet, Maybe<Identified> possibleMetaBroadcastContent, Optional<List<KeyPhrase>> keyPhrase) {
		Content content = (Content) possibleMetaBroadcastContent.requireValue();
		content.setTopicRefs(getTopicRefsFor(contentWordSet).addAll(filter(content.getTopicRefs())).build());
		if(keyPhrase.isPresent()){
			content.setKeyPhrases(Lists.newArrayList(keyPhrase.get()));
		}
		write(content);
	}

	protected static Content getNewContent(Identified originalContent, String newUri, String newCuri, Publisher publisher) {
		if (originalContent instanceof Brand){
			return new Brand(newUri, newCuri, publisher);
		} else if (originalContent instanceof Series){
			return new Series(newUri, newCuri, publisher);
		} else if (originalContent instanceof Clip){
			return new Clip(newUri, newCuri, publisher);
		} else if (originalContent instanceof Episode){
			return new Episode(newUri, newCuri, publisher);
		} else if (originalContent instanceof Film){
			return new Film(newUri, newCuri, publisher);
		}
		throw new IllegalArgumentException("Unrecognised type of content");
	}

	protected List<String> generateMetaBroadcastUris(Iterable<String> uris) {
		List<String> list = Lists.newArrayList();
		for (String uri : uris) {	
			list.add(generateMetaBroadcastUri(uri));
		}
		return list;
	}
	
	protected static String generateMetaBroadcastUri(String uri){
		return "http://metabroadcast.com/" + uri.replaceFirst("(http(s?)://)", "");
	}

	private Iterable<? extends TopicRef> filter(List<TopicRef> topicRefs) {
		return Iterables.filter(topicRefs, new Predicate<TopicRef>() {
			@Override
			public boolean apply(TopicRef input) {
				Maybe<Topic> possibleTopic = topicResolver.topicForId(input.getTopic());
				if (possibleTopic.hasValue()) {
					Topic topic = possibleTopic.requireValue();
					return !(namespace.equals(topic.getNamespace()) && Publisher.METABROADCAST.equals(topic.getPublisher()));
				}
				return false;
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
		Builder<TopicRef> topicRefs = ImmutableSet.builder();
		for (WordWeighting wordWeighting : ImmutableSet.copyOf(contentWordSet.getWords())) {
			Maybe<Topic> possibleTopic = topicStore.topicFor(namespace, wordWeighting.getUrl());
			if (possibleTopic.hasValue()) {
				Topic topic = possibleTopic.requireValue();
				topic.setTitle(wordWeighting.getContent());
				topic.setPublisher(Publisher.METABROADCAST);
				topic.setType(Topic.Type.SUBJECT);
				topicStore.write(topic);
				topicRefs.add(new TopicRef(topic, wordWeighting.getWeight()/100.0f, false));
			}
		}
		return topicRefs;
	}

	public Iterable<String> urisForWords(ContentWordsList contentWords) {
		return ImmutableSet.copyOf(Iterables.transform(contentWords.getResults(), new Function<ContentWords, String>() {
			@Override
			public String apply(ContentWords input) {
				return input.getUri();
			}
		}));
	}
}