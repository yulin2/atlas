package org.uriplay.http;

import java.util.concurrent.TimeUnit;

import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.http.SimpleHttpClientBuilder;

public class RedirectShortUrlResolver implements ShortUrlResolver {

	private final SimpleHttpClient client = new SimpleHttpClientBuilder().withSocketTimeout(10, TimeUnit.SECONDS).withConnectionTimeout(5, TimeUnit.SECONDS).build();
	
	@Override
	public String resolve(String shortUri) {
		try {
			return client.head(shortUri).finalUrl();
		} catch (HttpException e) {
			return null;
		}
	}
}
