package org.atlasapi.remotesite.hulu;

import static org.atlasapi.persistence.logging.AdapterLogEntry.errorEntry;
import static org.atlasapi.persistence.logging.AdapterLogEntry.warnEntry;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.html.HtmlNavigator;

import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.HttpResponsePrologue;
import com.metabroadcast.common.http.HttpResponseTransformer;
import com.metabroadcast.common.http.HttpStatusCode;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.http.SimpleHttpRequest;

public class HttpBackedHuluClient implements HuluClient {

    private static final int MAX_ATTEMPTS = 5;
    private final SimpleHttpClient client;
    private final AdapterLog log;

    public HttpBackedHuluClient(SimpleHttpClient client, AdapterLog log) {
        this.client = client;
        this.log = log;
    }

    @Override
    public Maybe<HtmlNavigator> get(String resource) {
        int attempts = start();
        while (attempts --> MAX_ATTEMPTS) {
            try {
                Maybe<HtmlNavigator> navigator = getNavigatorFor(resource);
                if (navigator != null) {
                    return navigator;
                }
            } catch (Exception e) {
                log.record(errorEntry().withSource(getClass()).withCause(e).withDescription("Exception getting %s", resource));
            }
        }
        log.record(warnEntry().withSource(getClass()).withDescription("Couldn't get %s after %s attempts", resource, MAX_ATTEMPTS));
        return Maybe.nothing();
    }

    private Maybe<HtmlNavigator> getNavigatorFor(final String resource) throws HttpException, Exception {
        return client.get(SimpleHttpRequest.httpRequestFrom(resource, new HttpResponseTransformer<Maybe<HtmlNavigator>>() {
            @Override
            public Maybe<HtmlNavigator> transform(HttpResponsePrologue prologue, InputStream body) throws HttpException, Exception {
                if(HttpStatusCode.OK.is(prologue.statusCode())) {
                    return Maybe.just(new HtmlNavigator(new InputStreamReader(body)));
                }
                if(HttpStatusCode.NOT_FOUND.is(prologue.statusCode()) || HttpStatusCode.FORBIDDEN.is(prologue.statusCode())) {
                    log.record(warnEntry().withSource(getClass()).withDescription("%s response for %s", prologue.statusCode(), resource));
                    return Maybe.nothing();
                }
                return null;
            }
        }));
    }

    private int start() {
        return 2 * MAX_ATTEMPTS;
    }
}
