package org.atlasapi.remotesite.worldservice;

import static com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.SystemOutAdapterLog;
import org.atlasapi.remotesite.worldservice.model.WsTopics;
import org.atlasapi.remotesite.worldservice.model.WsTopics.TopicWeighting;
import org.jets3t.service.S3Service;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.AWSCredentials;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.metabroadcast.common.base.Maybe;

public class S3WsTopicsClient implements WsTopicsClient {

    private final AWSCredentials credentials;
    private final String bucketname;
    private final AdapterLog log;
    private final Gson gson;
    
    private final String filename = "data-dump.json.gz";

    public S3WsTopicsClient(AWSCredentials credentials, String bucketname, AdapterLog log) {
        this.credentials = credentials;
        this.bucketname = bucketname;
        this.log = log;
        this.gson = new GsonBuilder().setFieldNamingPolicy(LOWER_CASE_WITH_UNDERSCORES).registerTypeAdapter(TopicWeighting.class, new JsonDeserializer<TopicWeighting>() {
            @Override
            public TopicWeighting deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                JsonArray list = json.getAsJsonArray();
                return new TopicWeighting(list.get(0).getAsString(), list.get(1).getAsFloat());
            }
        }).create();
    }
    
    @Override
    public Maybe<Map<String, WsTopics>> getLatestTopics() {
        try {
            S3Service service = new RestS3Service(credentials);
            S3Object topicsData = service.getObject(bucketname, filename);
            GZIPInputStream jsonStream = new GZIPInputStream(topicsData.getDataInputStream());
            InputStreamReader jsonReader = new InputStreamReader(jsonStream);
            Map<String, WsTopics> topicsMap = gson.fromJson(jsonReader, new TypeToken<Map<String, WsTopics>>() {}.getType());
            return Maybe.just(topicsMap);
        } catch (Exception e) {
            log.record(AdapterLogEntry.warnEntry().withCause(e).withDescription("Couldn't fetch WS Topics").withSource(getClass()));
            return Maybe.nothing();
        }
    }

}
