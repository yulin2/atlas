package org.atlasapi.remotesite.bbc.ion;

import static com.metabroadcast.common.http.SimpleHttpRequest.httpRequestFrom;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.bbc.ion.BbcIonDeserializers.BbcIonDeserializer;
import org.atlasapi.remotesite.bbc.ion.model.IonFeed;

import com.google.common.base.Charsets;
import com.google.gson.reflect.TypeToken;
import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.HttpResponsePrologue;
import com.metabroadcast.common.http.HttpResponseTransformer;
import com.metabroadcast.common.http.SimpleHttpClient;

public class HttpBackedBbcIonClient<T extends IonFeed> implements RemoteSiteClient<T> {

    public static <T extends IonFeed> HttpBackedBbcIonClient<T> ionClient(SimpleHttpClient backingClient, Class<T> cls) {
        return new HttpBackedBbcIonClient<T>(backingClient, TypeToken.get(cls));
    }
    
    public static <T extends IonFeed> HttpBackedBbcIonClient<T> ionClient(SimpleHttpClient backingClient, TypeToken<T> type) {
        return new HttpBackedBbcIonClient<T>(backingClient, type);
    }
    
    
    private final SimpleHttpClient backingClient;
    private BbcIonDeserializer<T> deserialiser;

    private HttpBackedBbcIonClient(SimpleHttpClient backingClient, TypeToken<T> type) {
        this.backingClient = backingClient;
        this.deserialiser = BbcIonDeserializers.deserializerForType(type);
    }

    @Override
    public T get(String uri) throws HttpException, Exception {
        return backingClient.get(httpRequestFrom(uri, new HttpResponseTransformer<T>() {

            @Override
            public T transform(HttpResponsePrologue prologue, InputStream body) throws HttpException, Exception {
                return deserialiser.deserialise(new InputStreamReader(body, prologue.getCharsetOrDefault(Charsets.UTF_8)));
            }
            
        }));
    }

}
