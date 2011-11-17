package org.atlasapi.http;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

import com.google.common.base.Charsets;
import com.google.common.collect.DiscreteDomains;
import com.google.common.collect.Ranges;
import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.HttpResponsePrologue;
import com.metabroadcast.common.http.HttpResponseTransformer;

public abstract class AbstractHttpResponseTransformer<T> implements HttpResponseTransformer<T> {

    @Override
    public T transform(HttpResponsePrologue prologue, InputStream body) throws HttpException, Exception {
        if(acceptableResponse(prologue)) {
            return transform(new InputStreamReader(body, prologue.getCharsetOrDefault(Charsets.UTF_8)));
        }
        throw new HttpException(prologue.statusCode() + " Response", prologue);
    }

    protected abstract T transform(InputStreamReader bodyReader) throws Exception;
    
    protected boolean acceptableResponse(HttpResponsePrologue prologue) {
        return acceptableResponseCodes().contains(prologue.statusCode());
    }

    protected Set<Integer> acceptableResponseCodes() {
        return Ranges.closedOpen(200, 400).asSet(DiscreteDomains.integers());
    }
    
}
