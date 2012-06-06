package org.atlasapi.remotesite.metabroadcast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import org.atlasapi.media.entity.KeyPhrase;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Topic;
import org.atlasapi.media.entity.Topic.Type;
import org.atlasapi.media.entity.simple.TopicRef;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.topic.TopicQueryResolver;
import org.atlasapi.persistence.topic.TopicStore;
import org.atlasapi.remotesite.metabroadcast.ContentWords.WordWeighting;
import org.atlasapi.remotesite.redux.UpdateProgress;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.model.S3Object;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.metabroadcast.common.scheduling.StatusReporter;

public class MetaBroadcastMagpieUpdater extends AbstractMetaBroadcastContentUpdater {

    private static final Logger logger = LoggerFactory.getLogger(MetaBroadcastMagpieUpdater.class);
	private static final String MAGPIE_NS = "magpie";
	private Gson gson; 
	private ContentResolver contentResolver;
	private final S3Service s3Service;
	private String s3Bucket;
    private final String s3folder;
	private StatusReporter reporter;

	public MetaBroadcastMagpieUpdater(ContentResolver contentResolver, 
			TopicStore topicStore, TopicQueryResolver topicResolver, ContentWriter contentWriter, 
			S3Service s3Service, String s3Bucket, String s3folder, AdapterLog log) {
		super(contentResolver, topicStore, topicResolver, contentWriter, log, MAGPIE_NS, Publisher.MAGPIE);
		this.contentResolver = contentResolver;
		this.s3Service = s3Service;
		this.s3Bucket = s3Bucket;
        this.s3folder = s3folder;
		try {
			this.gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
		}
		catch (Exception e) {
			log.record(AdapterLogEntry.debugEntry().withSource(getClass()).withDescription("The gson builder exceptioned"));
			this.gson = new GsonBuilder().create();
		}
		
	}

	@Override
	public UpdateProgress updateTopics(List<String> contentIds) {
		log.record(AdapterLogEntry.debugEntry().withSource(getClass()).withDescription("Got into update topics method"));
		UpdateProgress result = UpdateProgress.START;
		try {
			List<InputStream> s3Streams = getS3Stream();
			int debugLoopCount = 0;
			int debugInnerLoopCount = 0;
			for (InputStream stream : s3Streams){
				debugLoopCount++;
				log.record(AdapterLogEntry.debugEntry().withSource(getClass()).withDescription("For inputstream %s", stream.hashCode()));
				MagpieResults json = null;
				try {
					InputStreamReader inputStreamReader = new InputStreamReader(stream, "UTF-8");
					JsonReader streamReader = new JsonReader(inputStreamReader);		
					json = gson.fromJson(streamReader, MagpieResults.class);
				} catch (Exception e) {
					log.record(AdapterLogEntry.errorEntry().withCause(e).withSource(getClass()).withDescription("Failed to parse json"));
					return UpdateProgress.FAILURE;
				}

				List<MagpieScheduleItem> magpieItems = json.getResults();
				Iterable<String> uris = getUris(magpieItems);
				List<String> mbUris = generateMetaBroadcastUris(uris);
				reporter.reportStatus("On loop iteration " + debugLoopCount + " there were " + magpieItems.size() + " magpie items, " + mbUris.size() + 
						" mb uris and " + debugInnerLoopCount + " inner loops");
				
				ResolvedContent resolvedContent = contentResolver.findByCanonicalUris(uris);
				ResolvedContent resolvedMetaBroadcastContent = contentResolver.findByCanonicalUris(mbUris);

				for (MagpieScheduleItem magpieItem : magpieItems) {
					debugInnerLoopCount++;
					try{
						ContentWords contentWordSet = magpieItemToContentWordSet(magpieItem);
						List<org.atlasapi.media.entity.KeyPhrase> transformedKeys = getFullKeyPhraseKeys(magpieItem);
						result = result.reduce(createOrUpdateContent(resolvedContent, resolvedMetaBroadcastContent, result, 
								contentWordSet, Optional.of(transformedKeys)));
						//reporter.reportStatus(result.toString());
					} catch (Exception e) {
						log.record(AdapterLogEntry.debugEntry().withSource(getClass()).withDescription("Fails on MagpieItem %s", magpieItem.getUri()));
					}
				}
			}
		} catch (Exception e) {
			log.record(AdapterLogEntry.errorEntry().withCause(e).withSource(getClass()).withDescription("Failed outside inner for loop"));
			return UpdateProgress.FAILURE;
		}
		return result;
	}

	private List<InputStream> getS3Stream() throws S3ServiceException{
		S3Object[] listOfObjects = s3Service.listObjects(s3Bucket, s3folder + "/", "");
		List<S3Object> mostRecentObjectsMetadata = getMostRecentObjects(listOfObjects, 21); // Temporarily set to 21 for old ingesting, usually should be 7
		List<InputStream> mostRecentObjectStreams = Lists.transform(mostRecentObjectsMetadata, new Function<S3Object, InputStream>() {
			@Override
			public InputStream apply(S3Object input) {
				InputStream stream = null;
				try {
					S3Object object = s3Service.getObject(s3Bucket, input.getKey());
					stream =  object.getDataInputStream();
					logger.debug("Using s3 file {}", input.getKey());
				} catch (Exception e) {
					log.record(AdapterLogEntry.errorEntry().withCause(e).withSource(getClass()).withDescription("Failed to get s3 Stream"));
				}
				return stream;
			}
		});
		return mostRecentObjectStreams;
	}

	private List<S3Object> getMostRecentObjects(S3Object[] listOfObjects, int numberOfDaysToIngest) {
		Ordering<S3Object> byNewest = new Ordering<S3Object>() {
			@Override
			public int compare(S3Object left, S3Object right) {
				return (left.getLastModifiedDate().before(right.getLastModifiedDate())) ? -1 : 1;
			}
		};
		return byNewest.greatestOf(Arrays.asList(listOfObjects), numberOfDaysToIngest);
	}

	private List<KeyPhrase> getFullKeyPhraseKeys(MagpieScheduleItem magpieItem) {
		List<org.atlasapi.media.entity.simple.KeyPhrase> keys = magpieItem.getKeyPhrases();
		List<org.atlasapi.media.entity.KeyPhrase> transformedKeys = Lists.newArrayList();
		for (org.atlasapi.media.entity.simple.KeyPhrase simplePhrase : keys) {
			transformedKeys.add(new KeyPhrase(simplePhrase.getPhrase(), Publisher.MAGPIE, simplePhrase.getWeighting()));
		}
		return transformedKeys;
	}

	private ContentWords magpieItemToContentWordSet(MagpieScheduleItem magpieItem) {
		ContentWords contentWordSet = new ContentWords();
		// We don't have the voila content ID so use the Atlas URI
		contentWordSet.setContentId(magpieItem.getUri());
		contentWordSet.setUri(magpieItem.getUri());
		List<WordWeighting> words = Lists.newArrayList();

		for (TopicRef topic : magpieItem.getTopics()) {
			words.add(topicRefToWordWeighting(topic));
		} 
		contentWordSet.setWords(words);

		return contentWordSet;
	}

	private WordWeighting topicRefToWordWeighting(TopicRef topic) {
		return new WordWeighting(topic.getTopic().getTitle(), StrictMath.round(topic.getWeighting() * 100), topic.getTopic().getUri(), topic.getTopic().getValue() , topic.getTopic().getType());
	}

	private Iterable<String> getUris(List<MagpieScheduleItem> items){
		return ImmutableSet.copyOf(Iterables.transform(items, new Function<MagpieScheduleItem, String>() {
			@Override
			public String apply(MagpieScheduleItem input) {
				return input.getUri();
			}	
		}));
	}

	protected void setReporter(StatusReporter reporter) {
		this.reporter = reporter;
	}

    @Override
    protected Type topicTypeFromSource(String source) {
        return Type.fromKey(source);
    }
}
