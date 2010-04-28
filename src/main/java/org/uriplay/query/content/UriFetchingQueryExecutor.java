/* Copyright 2009 Meta Broadcast Ltd

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License. */

package org.uriplay.query.content;

import java.util.List;
import java.util.Set;

import org.jherd.remotesite.Fetcher;
import org.jherd.remotesite.timing.NullRequestTimer;
import org.uriplay.content.criteria.ContentQuery;
import org.uriplay.media.entity.Brand;
import org.uriplay.media.entity.Description;
import org.uriplay.media.entity.Item;
import org.uriplay.media.entity.Playlist;
import org.uriplay.persistence.content.query.KnownTypeQueryExecutor;

import com.google.soy.common.collect.Lists;

/**
 * Finds any uris from a given {@link ContentQuery}, fetches them using a local/remote
 * fetcher (so either from the database or from the internet), and uses the response
 * to replace the uris given in the query with the canonical versions of each, before passing
 * the updated query on to a delegate. 
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class UriFetchingQueryExecutor implements KnownTypeQueryExecutor {

	private final Fetcher<Set<Description>> fetcher;
	private final KnownTypeQueryExecutor delegate;
	
	public UriFetchingQueryExecutor(Fetcher<Set<Description>> fetcher, KnownTypeQueryExecutor delegate) {
		this.fetcher = fetcher;
		this.delegate = delegate;
	}
	
	public List<Item> executeItemQuery(ContentQuery query) {
		if (!fetch(query)) {
			return Lists.newArrayList();
		}
		return delegate.executeItemQuery(query);
	}

	public List<Playlist> executePlaylistQuery(ContentQuery query) {
		if (!fetch(query)) {
			return Lists.newArrayList();
		}
		return delegate.executePlaylistQuery(query);
	}
	
	public List<Brand> executeBrandQuery(ContentQuery query) {
		if (!fetch(query)) {
			return Lists.newArrayList();
		}
		return delegate.executeBrandQuery(query);
	}
	
	/**
	 * @return true if query executor should continue, false if the query will not
	 * match any beans.
	 */
	private boolean fetch(ContentQuery query) {
		Set<String> uris = UriExtractor.extractFrom(query);
		for (String uri : uris) {
			Set<Description> fetchedDescriptions = fetcher.fetch(uri, new NullRequestTimer());
			if (fetchedDescriptions == null || fetchedDescriptions.isEmpty()) {
				return false;
			}
		}
		return true;
	}
}
