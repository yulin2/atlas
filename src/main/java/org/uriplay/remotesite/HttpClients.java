package org.uriplay.remotesite;

import java.util.concurrent.TimeUnit;

import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.http.SimpleHttpClientBuilder;

public class HttpClients {

    private static final String URIPLAY_USER_AGENT = "Mozilla/5.0 (compatible; uriplay/0.1; +http://uriplay.org)";

    /**
     * @return A {@link SimpleHttpClient} that is configured to work
     * well with 3rd party web services that are expected to return data
     * reasonably quickly.
     */
    public static SimpleHttpClient webserviceClient() {
        return new SimpleHttpClientBuilder()
            .withUserAgent(URIPLAY_USER_AGENT)
            .withSocketTimeout(30, TimeUnit.SECONDS)
        .build();
    }
}