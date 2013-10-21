package org.atlasapi.remotesite.channel4.pmlsd;

import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.HttpResponse;
import com.metabroadcast.common.http.Payload;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.http.SimpleHttpRequest;

public final class UnconfiguredSimpleHttpClient implements SimpleHttpClient {

    private static final SimpleHttpClient INSTANCE
        = new UnconfiguredSimpleHttpClient();
    
    public static final SimpleHttpClient get() {
        return INSTANCE;
    }
    
    private UnconfiguredSimpleHttpClient() { }
    
    private final RuntimeException unconfigured() {
        throw new RuntimeException("unconfigured");
    }
    
    @Override
    public String getContentsOf(String url) throws HttpException {
        throw unconfigured();
    }

    @Override
    public <T> T get(SimpleHttpRequest<T> request) throws HttpException, Exception {
        throw unconfigured();
    }

    @Override
    public HttpResponse get(String url) throws HttpException {
        throw unconfigured();
    }

    @Override
    public HttpResponse post(String url, Payload data) throws HttpException {
        throw unconfigured();
    }

    @Override
    public HttpResponse head(String string) throws HttpException {
        throw unconfigured();
    }

    @Override
    public HttpResponse delete(String string) throws HttpException {
        throw unconfigured();
    }

    @Override
    public HttpResponse put(String url, Payload data) throws HttpException {
        throw unconfigured();
    }

}
