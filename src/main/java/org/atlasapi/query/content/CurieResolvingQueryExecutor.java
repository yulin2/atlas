package org.atlasapi.query.content;

import java.util.List;
import java.util.Map;

import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;

import com.google.common.collect.Lists;
import com.metabroadcast.common.base.Maybe;

public class CurieResolvingQueryExecutor implements KnownTypeQueryExecutor {

	private final KnownTypeQueryExecutor delegate;
	private final CurieExpander curieExpander = new PerPublisherCurieExpander();
	
	public CurieResolvingQueryExecutor(KnownTypeQueryExecutor delegate) {
		this.delegate = delegate;
	}
	
//	public List<Content> discover(ContentQuery query) {
//		return delegate.discover(query);
//	}

	public Map<String, List<Identified>> executeUriQuery(Iterable<String> ids, ContentQuery query) {
		return delegate.executeUriQuery(resolve(ids), query);
	}
	
	private List<String> resolve(Iterable<String> ids) {
		List<String> resolved = Lists.newArrayList(); 
		for (String value : ids) {
			Maybe<String> curieExpanded = curieExpander.expand(value);
			if (curieExpanded.hasValue()) {
				resolved.add(curieExpanded.requireValue());
			} else {
				resolved.add(value);
			}
		}
		return resolved;
	}
}
