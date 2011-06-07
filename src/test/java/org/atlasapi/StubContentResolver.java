package org.atlasapi;

import java.util.Map;

import org.atlasapi.media.entity.Content;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ResolvedContent;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

public class StubContentResolver implements ContentResolver {

	private Map<String, Content> data = Maps.newHashMap();
	
	public StubContentResolver respondTo(Content content) {
		data.put(content.getCanonicalUri(), content);
		return this;
	}

	@Override
	public ResolvedContent findByCanonicalUris(Iterable<String> uri) {
		return ResolvedContent.builder().putAll(Maps.filterKeys(data, Predicates.in(ImmutableSet.copyOf(uri)))).build();
	}
}

