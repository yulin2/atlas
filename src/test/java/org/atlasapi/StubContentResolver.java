package org.atlasapi;

import java.util.Map;

import org.atlasapi.media.entity.Content;
import org.atlasapi.persistence.content.ContentResolver;

import com.google.common.collect.Maps;

public class StubContentResolver implements ContentResolver {

	private Map<String, Content> data = Maps.newHashMap();
	
	public StubContentResolver respondTo(Content content) {
		data.put(content.getCanonicalUri(), content);
		return this;
	}

	@Override
	public Content findByCanonicalUri(String uri) {
		return data.get(uri);
	}
}

