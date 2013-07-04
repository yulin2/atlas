package org.atlasapi.remotesite.metabroadcast;

import java.util.List;
import java.util.Set;

import org.atlasapi.media.entity.KeyPhrase;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Topic;
import org.atlasapi.media.entity.Topic.Type;
import org.atlasapi.media.entity.TopicRef;
import org.atlasapi.media.entity.TopicRef.Relationship;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.topic.TopicQueryResolver;
import org.atlasapi.persistence.topic.TopicStore;
import org.atlasapi.remotesite.metabroadcast.ContentWords.ContentWordsList;
import org.atlasapi.remotesite.metabroadcast.ContentWords.WordWeighting;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.metabroadcast.common.scheduling.UpdateProgress;

public class MetaBroadcastTwitterTopicsUpdater extends AbstractMetaBroadcastContentUpdater {
    
    public static final String TWITTER_NS_FOR_AUDIENCE = "twitter";
    public static final String TWITTER_NS_FOR_AUDIENCE_RELATED = "twitter:audience-related";
    //
    
    private final ContentResolver contentResolver;
    private final CannonTwitterTopicsClient cannonTopicsClient;

    public MetaBroadcastTwitterTopicsUpdater(CannonTwitterTopicsClient cannonTopicsClient, ContentResolver contentResolver, 
            TopicStore topicStore, TopicQueryResolver topicResolver, ContentWriter contentWriter, String namespace, AdapterLog log) {
        super(contentResolver, topicStore, topicResolver, contentWriter, namespace, Publisher.VOILA);
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
            result = result.reduce(createOrUpdateContent(resolvedContent, resolvedMetaBroadcastContent, contentWordSet, key));
        }
        return result;
    }

    @Override
    protected Type topicTypeFromSource(String source) {
        if(source.equals("http://schema.org/Person")) {
            return Topic.Type.PERSON;
        }
        else if (source.equals("http://schema.org/Place")) {
            return Topic.Type.PLACE;
        }
        else if (source.equals("http://schema.org/Product")) {
            return Topic.Type.PRODUCT;
        }
        else return Topic.Type.SUBJECT;
    }

    @Override
    protected String topicValueFromWordWeighting(WordWeighting weighting) {
        return weighting.getUrl();
    }

    @Override
    protected Relationship topicRefRelationship() {
        if (namespace.equals(TWITTER_NS_FOR_AUDIENCE)) {
        return TopicRef.Relationship.TWITTER_AUDIENCE;
        } else {
        return TopicRef.Relationship.TWITTER_AUDIENCE_RELATED;
        }
    }
    
    private Iterable<String> urisForWords(ContentWordsList contentWordsList) {
        List<ContentWords> results = contentWordsList.getResults();
        Set<String> uris = Sets.newHashSetWithExpectedSize(results.size());
        for (ContentWords contentWords : results) {
            uris.add(contentWords.getUri());
        }
        return uris;
    }
}
