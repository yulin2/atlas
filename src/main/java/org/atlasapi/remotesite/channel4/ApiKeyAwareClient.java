package org.atlasapi.remotesite.channel4;

import org.atlasapi.persistence.system.RemoteSiteClient;

import com.metabroadcast.common.url.Urls;

public class ApiKeyAwareClient<T> implements RemoteSiteClient<T> {

	private final static String API_KEY_PARAM_NAME = "apikey";
	private final String apiKey;
	private final RemoteSiteClient<T> delegate;
	
	public ApiKeyAwareClient(String apiKey, RemoteSiteClient<T> delegate) {
		this.apiKey = apiKey;
		this.delegate = delegate;
	}
	
	@Override
	public T get(String uri) throws Exception {
		return delegate.get(uriWithKey(uri));
	}

	private String uriWithKey(String uri) {
		if (uri.contains(API_KEY_PARAM_NAME)) {
			return uri;
		}
		return Urls.appendParameters(uri, API_KEY_PARAM_NAME, apiKey);
	}
}
