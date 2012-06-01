package org.atlasapi.remotesite.metabroadcast;

import java.util.List;

import org.atlasapi.media.entity.KeyPhrase;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.topic.TopicQueryResolver;
import org.atlasapi.persistence.topic.TopicStore;
import org.atlasapi.remotesite.metabroadcast.ContentWords.ContentWordsList;
import org.atlasapi.remotesite.redux.UpdateProgress;

import com.google.common.base.Optional;

public class MetaBroadcastTwitterTopicsUpdater extends AbstractMetaBroadcastContentUpdater {

	private final ContentResolver contentResolver;
	private final CannonTwitterTopicsClient cannonTopicsClient;

	public MetaBroadcastTwitterTopicsUpdater(CannonTwitterTopicsClient cannonTopicsClient, ContentResolver contentResolver, 
			TopicStore topicStore, TopicQueryResolver topicResolver, ContentWriter contentWriter, String namespace, AdapterLog log) {
		super(contentResolver, topicStore, topicResolver, contentWriter, log, namespace, Publisher.VOILA);
		this.cannonTopicsClient = cannonTopicsClient;
		this.contentResolver = contentResolver;
	}

	public UpdateProgress updateTopics(List<String> contentIds) {

		Optional<ContentWordsList> possibleContentWords = cannonTopicsClient.getContentWordsForIds(contentIds);

		if (!possibleContentWords.isPresent()) {
			return new UpdateProgress(0, contentIds.size());
		}

		ContentWordsList contentWords = possibleContentWords.get();

		Iterable<String> uris = urisForWords(contentWords);
		List<String> uriToMetaBroadcastUri = generateMetaBroadcastUris(uris);

		ResolvedContent resolvedContent = contentResolver.findByCanonicalUris(uris);
		ResolvedContent resolvedMetaBroadcastContent = contentResolver.findByCanonicalUris(uriToMetaBroadcastUri);
		Optional<List<KeyPhrase>> key = Optional.absent();
		
		UpdateProgress result = UpdateProgress.START;
		for (ContentWords contentWordSet : contentWords) {
			result = result.reduce(createOrUpdateContent(resolvedContent, resolvedMetaBroadcastContent, result, contentWordSet, key));
		}
		return result;
	}
}