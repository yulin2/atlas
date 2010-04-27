package org.uriplay.query.content;

import java.util.Set;

import org.jherd.remotesite.Fetcher;
import org.jherd.remotesite.timing.NullRequestTimer;
import org.uriplay.media.entity.Description;
import org.uriplay.persistence.content.query.KnownTypeQueryExecutor;

import com.google.common.collect.Sets;

public class TypeResolvingQueryExecutor implements UnknownTypeQueryExecutor {

	private final KnownTypeQueryExecutor queryExecutor;
	private final Fetcher<Set<Description>> fetcher;

	public TypeResolvingQueryExecutor(KnownTypeQueryExecutor queryExecutor, Fetcher<Set<Description>> fetcher) {
		this.queryExecutor = queryExecutor;
		this.fetcher = fetcher;
	}
	

	private Description findDescriptionFrom(String uri, Set<? extends Description> descriptions) {
		if (descriptions == null) {
			return null;
		}
		for (Description description : descriptions) {
			Set<String> uris = description.getAllUris();
			if (uris != null && uris.contains(uri)) {
				return description;
			}
 		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<Description> executeQuery(String uri) {
		return (Set) beansForUris(uri);
	}

	private Set<? extends Description> beansForUris(String uri) {
		Description bean = beanForUri(uri);
		if (bean == null) {
			return Sets.newHashSet();
		} else {
			return Sets.newHashSet(bean);
		}
	}

	private Description beanForUri(String uri) {
		return findDescriptionFrom(uri, fetcher.fetch(uri, new NullRequestTimer()));
	}
}
