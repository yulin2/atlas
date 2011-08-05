package org.atlasapi.remotesite.bbc.ion;

import static org.atlasapi.remotesite.bbc.ion.BbcIonDeserializers.deserializerForClass;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.atlasapi.remotesite.HttpClients;
import org.atlasapi.remotesite.bbc.ion.BbcIonDeserializers.BbcIonDeserializer;
import org.atlasapi.remotesite.bbc.ion.model.IonFeed;

import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.HttpResponsePrologue;
import com.metabroadcast.common.http.HttpResponseTransformer;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.http.SimpleHttpRequest;

public class BbcIonFeedClient<T extends IonFeed> {
    
    private final BbcIonDeserializer<T> deserialiser;
    private final SimpleHttpClient httpClient;
    
    public static <T extends IonFeed> BbcIonFeedClient<T> clientForFeedType(Class<T> feedType) {
        return new BbcIonFeedClient<T>(feedType);
    }
    
    public BbcIonFeedClient(Class<T> feedType, SimpleHttpClient httpClient) {
        this.deserialiser = deserializerForClass(feedType);
        this.httpClient = httpClient;
    }
    
    public BbcIonFeedClient(Class<T> feedType) {
        this(feedType, HttpClients.webserviceClient());
    }

    public T getFeed(String scheduleUrl) throws HttpException, Exception {
        return httpClient.get(new SimpleHttpRequest<T>(scheduleUrl, new HttpResponseTransformer<T>() {
            @Override
            public T transform(HttpResponsePrologue prologue, InputStream body) throws HttpException, Exception {
                return deserialiser.deserialise(new InputStreamReader(body));
            }
        }));
    }
    
}
