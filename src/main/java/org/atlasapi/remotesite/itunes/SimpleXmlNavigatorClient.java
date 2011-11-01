package org.atlasapi.remotesite.itunes;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.xml.SimpleXmlNavigator;

import com.google.common.base.Charsets;
import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.HttpResponsePrologue;
import com.metabroadcast.common.http.HttpResponseTransformer;
import com.metabroadcast.common.http.HttpStatusCode;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.http.SimpleHttpRequest;

public class SimpleXmlNavigatorClient implements RemoteSiteClient<SimpleXmlNavigator> {

    private final SimpleHttpClient baseClient;

    public SimpleXmlNavigatorClient(SimpleHttpClient baseClient) {
        this.baseClient = baseClient;
    }
    
    @Override
    public SimpleXmlNavigator get(final String uri) throws Exception {
        return baseClient.get(SimpleHttpRequest.httpRequestFrom(uri, new HttpResponseTransformer<SimpleXmlNavigator>() {
            @Override
            public SimpleXmlNavigator transform(HttpResponsePrologue prologue, InputStream body) throws HttpException, Exception {
                if(HttpStatusCode.OK.code() != prologue.statusCode()) {
                    throw new HttpException(prologue.statusCode() + " response for " + uri, prologue);
                }
                return new SimpleXmlNavigator(new InputStreamReader(body, prologue.getCharsetOrDefault(Charsets.UTF_8)));
            }
        }));
    }

}
