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

package org.uriplay.query.uri;

import java.util.Set;

import org.jherd.remotesite.Fetcher;
import org.jherd.remotesite.timing.RequestTimer;
import org.uriplay.persistence.content.MutableContentStore;

/**
 * Decorator for a {@link Fetcher} that stores the results before passing them on.
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class SavingFetcher implements Fetcher<Set<Object>> {

	private final Fetcher<Set<Object>> delegateFetcher;
	private final MutableContentStore store;

	public SavingFetcher(Fetcher<Set<Object>> delegateFetcher, MutableContentStore store) {
		this.delegateFetcher = delegateFetcher;
		this.store = store;
	}

	public Set<Object> fetch(String uri, RequestTimer timer) {
		
		Set<Object> beans = delegateFetcher.fetch(uri, timer);
		
		if (beans == null) { return null; }
		
		store.createOrUpdateGraph(beans, false);
		
		return beans;
	}
}
