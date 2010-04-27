/* Copyright 2009 British Broadcasting Corporation
   Copyright 2009 Meta Broadcast Ltd

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

import org.jherd.naming.ResourceMapping;
import org.jherd.remotesite.Fetcher;
import org.jherd.remotesite.timing.RequestTimer;

import com.google.common.collect.Sets;

/**
 * Aggregate {@link Fetcher} that checks a local datastore for resources,
 * before trying to fetch them remotely.
 *
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class LocalOrRemoteFetcher implements Fetcher<Set<Object>> {

	private final ResourceMapping localStore;
	
	private final Fetcher<Set<Object>> remoteFetcher;
	
	public LocalOrRemoteFetcher(ResourceMapping localStore, Fetcher<Set<Object>> remoteFetcher) {
		this.localStore = localStore;
		this.remoteFetcher = remoteFetcher;
	}

	public Set<Object> fetch(String uri, RequestTimer timer) {
		
		Object local = localStore.getResource(uri);
		
		if (local == null) {
			return remoteFetcher.fetch(uri, timer); 
		} else {
		    return Sets.newHashSet(local);
		}
	}

}
