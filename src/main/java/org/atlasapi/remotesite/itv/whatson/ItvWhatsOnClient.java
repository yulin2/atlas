package org.atlasapi.remotesite.itv.whatson;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.atlasapi.persistence.system.RemoteSiteClient;

import com.google.common.base.Charsets;
import com.google.common.collect.FluentIterable;
import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.HttpResponsePrologue;
import com.metabroadcast.common.http.HttpResponseTransformer;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.http.SimpleHttpRequest;

public class ItvWhatsOnClient implements RemoteSiteClient<FluentIterable<ItvWhatsOnEntry>> {
    private static final ItvWhatsOnDeserializer deserializer = new ItvWhatsOnDeserializer();
    private static final HttpResponseTransformer<FluentIterable<ItvWhatsOnEntry>> ENTRIES_TRANSFORMER = new HttpResponseTransformer<FluentIterable<ItvWhatsOnEntry>>() {
        @Override
        public FluentIterable<ItvWhatsOnEntry> transform(HttpResponsePrologue prologue, InputStream body) throws HttpException, Exception {
            return deserializer.deserialize(new InputStreamReader(body, prologue.getCharsetOrDefault(Charsets.UTF_8)));
        }
    };
    
    private final SimpleHttpClient httpClient;
    
    public ItvWhatsOnClient(SimpleHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public FluentIterable<ItvWhatsOnEntry> get(String uri) throws Exception {
        return httpClient.get(SimpleHttpRequest.httpRequestFrom(uri, ENTRIES_TRANSFORMER));
    }

}
