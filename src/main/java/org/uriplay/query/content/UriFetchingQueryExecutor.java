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
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.uriplay.content.criteria.ContentQuery;
import org.uriplay.media.entity.Brand;
import org.uriplay.media.entity.Content;
import org.uriplay.media.entity.Description;
import org.uriplay.media.entity.Item;
import org.uriplay.media.entity.Playlist;
import org.uriplay.persistence.content.query.KnownTypeQueryExecutor;
import org.uriplay.persistence.system.Fetcher;
import org.uriplay.persistence.system.NullRequestTimer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.metabroadcast.common.base.Maybe;

/**
 * Finds any uris from a given {@link ContentQuery}, fetches them using a local/remote
 * fetcher (so either from the database or from the internet), and uses the response
 * to replace the uris given in the query with the canonical versions of each, before passing
 * the updated query on to a delegate. 
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class UriFetchingQueryExecutor implements KnownTypeQueryExecutor {

	private final Log log = LogFactory.getLog(getClass());
	
	private final Fetcher<Content> fetcher;
	private final KnownTypeQueryExecutor delegate;
	
	public UriFetchingQueryExecutor(Fetcher<Content> fetcher, KnownTypeQueryExecutor delegate) {
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
	
	private final CurieExpander curieExpander = new PerPublisherCurieExpander();

	
	@Override
	public Map<String, Content> executeAnyQuery(Iterable<String> urisOrCuries) {
		
		Set<String> uris = urisFrom(urisOrCuries);
		
		Map<String, Content> inDb = Maps.newHashMap(delegate.executeAnyQuery(uris));
		
		Set<String> toFetch = Sets.difference(uris, inDb.keySet());
		
		for (String uri : toFetch) {
			
			try {
				Content found = fetcher.fetch(uri, new NullRequestTimer());
				if (found != null) {
					inDb.put(found.getCanonicalUri(), found);
				}
			} catch (Exception e) {
				log.warn(e);
			}
		}
		return inDb;
	}
	
	private Set<String> urisFrom(Iterable<String> urisOrCuries) {
		Set<String> uris = Sets.newHashSet();
		for (String uriOrCurie : urisOrCuries) {
			Maybe<String> expandedUri = curieExpander.expand(uriOrCurie);
			if (expandedUri.hasValue()) {
				uris.add(expandedUri.requireValue());
			} else {
				uris.add(uriOrCurie);
			}
		}
		return uris;
	}

	/**
	 * @return true if query executor should continue, false if the query will not
	 * match any beans.
	 */
	private boolean fetch(ContentQuery query) {
		Set<String> uris = UriExtractor.extractFrom(query);
		for (String uri : uris) {
			Description fetchedDescriptions = fetcher.fetch(uri, new NullRequestTimer());
			if (fetchedDescriptions == null) {
				return false;
			}
		}
		return true;
	}
}
