package org.atlasapi.remotesite;

import java.util.Map;

import org.atlasapi.media.content.Content;
import org.atlasapi.persistence.system.Fetcher;

import com.google.common.collect.Maps;

public class StubFetcher implements Fetcher<Content> {

	private Map<String, Content> data = Maps.newHashMap();
	
	public StubFetcher respondTo(Content content) {
		data.put(content.getCanonicalUri(), content);
		return this;
	}
	
	@Override
	public Content fetch(String uri) {
		if (!data.containsKey(uri)) {
			throw new FetchException(uri);
		}
		return data.get(uri);
	}
}
