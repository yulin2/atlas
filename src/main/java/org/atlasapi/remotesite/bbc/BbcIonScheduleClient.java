package org.atlasapi.remotesite.bbc;

import static org.atlasapi.remotesite.bbc.ion.BbcIonDeserializers.deserializerForClass;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.atlasapi.remotesite.HttpClients;
import org.atlasapi.remotesite.bbc.ion.BbcIonDeserializers.BbcIonDeserializer;
import org.atlasapi.remotesite.bbc.ion.model.IonSchedule;

import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.HttpResponsePrologue;
import com.metabroadcast.common.http.HttpResponseTransformer;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.http.SimpleHttpRequest;

public class BbcIonScheduleClient {
    
    private final BbcIonDeserializer<IonSchedule> deserialiser = deserializerForClass(IonSchedule.class);
    private final SimpleHttpClient httpClient;
    
    public BbcIonScheduleClient(SimpleHttpClient httpClient) {
        this.httpClient = httpClient;
    }
    
    public BbcIonScheduleClient() {
        this(HttpClients.webserviceClient());
    }

    public IonSchedule scheduleFor(String scheduleUrl) throws HttpException, Exception {
        return httpClient.get(new SimpleHttpRequest<IonSchedule>(scheduleUrl, new HttpResponseTransformer<IonSchedule>() {
            @Override
            public IonSchedule transform(HttpResponsePrologue prologue, InputStream body) throws HttpException, Exception {
                return deserialiser.deserialise(new InputStreamReader(body));
            }
        }));
    }
    
}
