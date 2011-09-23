package org.atlasapi.remotesite.channel4;

import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.HttpResponse;
import com.metabroadcast.common.http.Payload;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.http.SimpleHttpRequest;

public class RequestLimitingSimpleHttpClient implements SimpleHttpClient {
    
    private final SimpleHttpClient delegate;
    private int waitTimeMs;
    private long lastRequest = 0;

    public RequestLimitingSimpleHttpClient(SimpleHttpClient delegate, int requestsPerSecond) {
        this.delegate = delegate;
        this.waitTimeMs = (int) (Math.ceil(1000.0 / requestsPerSecond));
    }

    @Override
    public String getContentsOf(String url) throws HttpException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T get(SimpleHttpRequest<T> request) throws HttpException, Exception {
        synchronized (this) {
            while (true) {
                long now = System.currentTimeMillis();
                long timePassedSinceLastRequest = now - lastRequest;
                
                if (timePassedSinceLastRequest < waitTimeMs) {
                    wait(waitTimeMs - timePassedSinceLastRequest);
                } else {
                    break;
                }
            }
            lastRequest = System.currentTimeMillis();
        }
        return delegate.get(request);
    }

    @Override
    public HttpResponse get(String url) throws HttpException {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpResponse post(String url, Payload data) throws HttpException {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpResponse head(String string) throws HttpException {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpResponse put(String url, Payload data) throws HttpException {
        throw new UnsupportedOperationException();
    }

}
