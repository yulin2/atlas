package org.atlasapi.remotesite.talktalk;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import com.google.common.io.Resources;
import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.HttpResponse;
import com.metabroadcast.common.http.HttpResponsePrologue;
import com.metabroadcast.common.http.HttpStatusCode;
import com.metabroadcast.common.http.Payload;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.http.SimpleHttpRequest;

public class StubSimpleHttpClient implements SimpleHttpClient {

    private Map<String, URL> respondsTo;

    public StubSimpleHttpClient(Map<String, URL> respondsTo) {
        this.respondsTo = checkNotNull(respondsTo);
    }

    @Override
    @Deprecated
    public String getContentsOf(String url) throws HttpException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T get(SimpleHttpRequest<T> request) throws HttpException, Exception {
        URL url = respondsTo.get(request.getUrl());
        InputStream body;
        HttpResponsePrologue prologue;
        if (url == null) {
            prologue = new HttpResponsePrologue(HttpStatusCode.NOT_FOUND.code());
            body = new ByteArrayInputStream(new byte[0]);
        } else {
            body = Resources.newInputStreamSupplier(url).getInput();
            prologue = HttpResponsePrologue.sucessfulResponse();
        }
        return request.getTransformer().transform(prologue, body);
    }

    @Override
    @Deprecated
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
    public HttpResponse delete(String string) throws HttpException {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpResponse put(String url, Payload data) throws HttpException {
        throw new UnsupportedOperationException();
    }
    
}
