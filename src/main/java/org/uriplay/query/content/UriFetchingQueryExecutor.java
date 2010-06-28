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

import org.uriplay.content.criteria.ContentQuery;
import org.uriplay.media.entity.Brand;
import org.uriplay.media.entity.Content;
import org.uriplay.media.entity.Item;
import org.uriplay.media.entity.Playlist;
import org.uriplay.persistence.content.query.KnownTypeQueryExecutor;
import org.uriplay.persistence.system.Fetcher;
import org.uriplay.persistence.system.NullRequestTimer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

/**
 * Finds any uris from a given {@link ContentQuery}, fetches them using a local/remote
 * fetcher (so either from the database or from the Internet), and uses the response
 * to replace the uris given in the query with the canonical versions of each, before passing
 * the updated query on to a delegate. 
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class UriFetchingQueryExecutor implements KnownTypeQueryExecutor {

	private final Fetcher<Content> fetcher;
	private final KnownTypeQueryExecutor delegate;
	
	public UriFetchingQueryExecutor(Fetcher<Content> fetcher, KnownTypeQueryExecutor delegate) {
		this.fetcher = fetcher;
		this.delegate = delegate;
	}
	
	public List<Item> executeItemQuery(ContentQuery query) {
		return executeContentQuery(query, new DelegateQueryExecutor<Item>() {

			@Override
			public List<Item> executeQuery(KnownTypeQueryExecutor executor, ContentQuery query) {
				return executor.executeItemQuery(query);
			}
		});
	}
	
	public List<Brand> executeBrandQuery(ContentQuery query) {
		return executeContentQuery(query, new DelegateQueryExecutor<Brand>() {

			@Override
			public List<Brand> executeQuery(KnownTypeQueryExecutor executor, ContentQuery query) {
				return executor.executeBrandQuery(query);
			}
		});
	}
	
	public List<Playlist> executePlaylistQuery(ContentQuery query) {
		return executeContentQuery(query, new DelegateQueryExecutor<Playlist>() {

			@Override
			public List<Playlist> executeQuery(KnownTypeQueryExecutor executor, ContentQuery query) {
				return executor.executePlaylistQuery(query);
			}
		});
	}
	
	public <T extends Content> List<T> executeContentQuery(ContentQuery query, DelegateQueryExecutor<T> executor) {

		List<T> found =  executor.executeQuery(delegate, query);
		
		Set<String> missingUris = missingUris(found, query);
		
		if (missingUris.isEmpty()) {
			return found;
		} 

		boolean foundAtLeastOneUri = false;
		
		for (String missingUri : missingUris) {
			Content remoteContent = fetcher.fetch(missingUri, new NullRequestTimer());
			if (remoteContent != null) {
				foundAtLeastOneUri = true;
			}
		}
		
		if (!foundAtLeastOneUri) {
			return ImmutableList.of();
		}

		return executor.executeQuery(delegate, query);
	}
	
	private static Set<String> missingUris(Iterable<? extends Content> content, ContentQuery query) {
		return Sets.difference(UriExtractor.extractFrom(query), urisFrom(content));
	}

	private static Set<String> urisFrom(Iterable<? extends Content> contents) {
		Set<String> uris = Sets.newHashSet();
		for (Content content : contents) {
			uris.addAll(content.getAllUris());
			if (content instanceof Playlist) {
				for (Item item : ((Playlist) content).getItems()) {
					uris.addAll(item.getAllUris());
				}
			}
		}
		return uris;
	}
}
