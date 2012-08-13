package org.atlasapi.remotesite.bbckiwisubtitles;

import static com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.atlasapi.remotesite.worldservice.model.WsTopics.TopicWeighting;
import org.elasticsearch.common.collect.ImmutableList;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.AWSCredentials;
import org.kamranzafar.jtar.TarEntry;
import org.kamranzafar.jtar.TarInputStream;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

public class S3KiwiSubtitlesTopicsClient {

    private static final Pattern PID_PATTERN = Pattern.compile("^.*\\/(.*)\\.json$");
    private static final String BBC_PROGRAMMES_PREFIX = "http://www.bbc.co.uk/programmes/";
    private final String bucketName;
    private final Gson gson;
    private AWSCredentials credentials;

    public S3KiwiSubtitlesTopicsClient(AWSCredentials credentials, String bucketName) {
        this.bucketName = bucketName;
        this.credentials = credentials;
        this.gson = new GsonBuilder().setFieldNamingPolicy(LOWER_CASE_WITH_UNDERSCORES).registerTypeAdapter(TopicWeighting.class, new JsonDeserializer<TopicWeighting>() {
            @Override
            public TopicWeighting deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                JsonObject object = (JsonObject) json;
                return new TopicWeighting(object.get("link").getAsString(), object.get("score").getAsFloat());
            }
        }).create();

    }
    
    public Iterable<ItemTopics> getTopics() {
        try {
            final S3Service service = new RestS3Service(credentials);
            final List<S3Object> files = ImmutableList.copyOf(service.listObjects(bucketName, "", ""));
            return new Iterable<ItemTopics>() {

                @Override
                public Iterator<ItemTopics> iterator() {
                    return new S3AbcIpSubtitlesIterator(service, files);
                }
            };
            
        } catch (S3ServiceException e) {
            Throwables.propagate(e);
        }
        return null;
    }
    
    private final class S3AbcIpSubtitlesIterator extends AbstractIterator<ItemTopics> {

        private final S3Service s3service;
        private final Iterator<S3Object> files;
        private Optional<TarInputStream> currentTar;
        
        protected S3AbcIpSubtitlesIterator(S3Service s3service, Iterable<S3Object> files) {
            this.files = files.iterator();
            this.s3service = s3service;
            nextTar();
        }
        
        @Override
        protected ItemTopics computeNext() {
            try {
               if(!currentTar.isPresent()) {
                   return endOfData();
               }
               TarEntry entry = currentTar.get().getNextEntry();
               if(entry == null) {
                   nextTar();
                   return computeNext();
               }
               if(entry.isDirectory()) {
                   return computeNext();
               }
               return deserialize(entry, currentTar.get());
            }
            catch(IOException e) {
                Throwables.propagate(e);
                return null;
            }
        }
        
        private void nextTar() {
            if(!files.hasNext()) {
                currentTar = Optional.absent();
                return;
            }
            try {
                S3Object basicS3Object = files.next();
                S3Object s3Object = s3service.getObject(basicS3Object.getBucketName(), basicS3Object.getKey());
                currentTar = Optional.of(new TarInputStream(new GZIPInputStream(s3Object.getDataInputStream())));
            }
            catch(ServiceException e) {
                Throwables.propagate(e);
            } catch (IOException e) {
                Throwables.propagate(e);
            }
        }
        
        private ItemTopics deserialize(TarEntry entry, InputStream is) {
            Iterable<TopicWeighting> topics = gson.fromJson(new InputStreamReader(is), new TypeToken<Collection<TopicWeighting>>(){}.getType());
            Matcher matcher = PID_PATTERN.matcher(entry.getName());
            if(matcher.matches()) {
                String pid = matcher.group(1);
                return new ItemTopics(BBC_PROGRAMMES_PREFIX + pid, topics);
            }
            else {
                //TODO: Remove this code! This is just so that we don't break with the files that are currently in s3
                return new ItemTopics(null, ImmutableSet.<TopicWeighting>of());
                //throw new IllegalArgumentException("Could not parse PID from filename");
            }
        }
    }
    
    public static class ItemTopics {
        private String uri;
        private Set<TopicWeighting> topicWeightings;
        
        private ItemTopics(String uri, Iterable<TopicWeighting> topicWeightings) {
            this.uri = uri;
            this.topicWeightings = ImmutableSet.copyOf(topicWeightings);
        }
        
        public String getUri() {
            return uri;
        }
        
        public Iterable<TopicWeighting> getTopicWeightings() {
            return topicWeightings;
        }
    };
}
