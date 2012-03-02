package org.atlasapi.remotesite;

import com.metabroadcast.common.http.OAuthSimpleHttpClient;
import java.util.concurrent.TimeUnit;

import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.http.SimpleHttpClientBuilder;
import com.metabroadcast.common.media.MimeType;

public class HttpClients {

    public static final String ATLAS_USER_AGENT = "Mozilla/5.0 (compatible; atlas/1.0; +http://atlasapi.org)";

    /**
     * @return A {@link SimpleHttpClient} that is configured to work
     * well with 3rd party web services that are expected to return data
     * reasonably quickly.
     */
    public static SimpleHttpClient webserviceClient() {
        return new SimpleHttpClientBuilder()
            .withUserAgent(ATLAS_USER_AGENT)
            .withSocketTimeout(30, TimeUnit.SECONDS)
            .withRetries(3)
        .build();
    }

	public static SimpleHttpClient screenScrapingClient() {
		return new SimpleHttpClientBuilder()
	        .withUserAgent(ATLAS_USER_AGENT)
	        .withSocketTimeout(50, TimeUnit.SECONDS)
	        .withConnectionTimeout(10, TimeUnit.SECONDS)
	        .withAcceptHeader(MimeType.TEXT_HTML)
	        .withRetries(3)
        .build();
	}

    public static SimpleHttpClient oauthClient(String apiKey, String apiSecret) {
        return new OAuthSimpleHttpClient.Builder()
                .withApiKey(apiKey)
                .withApiSecret(apiSecret)
                .withConnectTimeout(10, TimeUnit.SECONDS)
                .withReadTimeout(10, TimeUnit.SECONDS)
                .withUserAgent(ATLAS_USER_AGENT)
                .build();
    }
}