package org.atlasapi.remotesite.bbc;

import static org.atlasapi.remotesite.bbc.ion.BbcIonDeserializers.deserializerForClass;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.atlasapi.remotesite.HttpClients;
import org.atlasapi.remotesite.bbc.ion.BbcIonDeserializers.BbcIonDeserializer;
import org.atlasapi.remotesite.bbc.ion.model.IonSchedule;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.HttpResponsePrologue;
import com.metabroadcast.common.http.HttpResponseTransformer;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.http.SimpleHttpRequest;

public class BbcIonScheduleClient {
    
    private final BbcIonDeserializer<IonSchedule> deserialiser = deserializerForClass(IonSchedule.class);
    private final SimpleHttpClient httpClient;
    private final String scheduleUriFormat;
    
    private DateTimeFormatter format = ISODateTimeFormat.dateTime();
    
    public BbcIonScheduleClient(String scheduleUriFormat, SimpleHttpClient httpClient) {
        this.scheduleUriFormat = scheduleUriFormat;
        this.httpClient = httpClient;
    }
    
    public BbcIonScheduleClient(String scheduleUriFormat) {
        this(scheduleUriFormat, HttpClients.webserviceClient());
    }
    
    public BbcIonScheduleClient withDateFormat(DateTimeFormatter formatter) {
        this.format = formatter;
        return this;
    }

    public IonSchedule scheduleFor(String service, DateTime date) throws HttpException, Exception {
        return httpClient.get(new SimpleHttpRequest<IonSchedule>(String.format(scheduleUriFormat, service, format.print(date)), new HttpResponseTransformer<IonSchedule>() {
            @Override
            public IonSchedule transform(HttpResponsePrologue prologue, InputStream body) throws HttpException, Exception {
                return deserialiser.deserialise(new InputStreamReader(body));
            }
        }));
    }
    
}
