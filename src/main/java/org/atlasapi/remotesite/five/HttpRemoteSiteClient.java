package org.atlasapi.remotesite.five;

import org.atlasapi.persistence.system.RemoteSiteClient;

import com.metabroadcast.common.http.HttpResponse;
import com.metabroadcast.common.http.SimpleHttpClient;

@Deprecated
public class HttpRemoteSiteClient implements RemoteSiteClient<HttpResponse> {
    
    private final SimpleHttpClient client;

    public HttpRemoteSiteClient(SimpleHttpClient client) {
        this.client = client;
    }

    @Override
    public HttpResponse get(String uri) throws Exception {
        return client.get(uri);
    }

}
