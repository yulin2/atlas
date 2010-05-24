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


import org.uriplay.feeds.naming.ResourceMapping;
import org.uriplay.persistence.system.Fetcher;
import org.uriplay.persistence.system.RequestTimer;

/**
 * Aggregate {@link Fetcher} that checks a local datastore for resources,
 * before trying to fetch them remotely.
 *
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class LocalOrRemoteFetcher implements Fetcher<Object> {

	private final ResourceMapping localStore;
	
	private final Fetcher<Object> remoteFetcher;
	
	public LocalOrRemoteFetcher(ResourceMapping localStore, Fetcher<Object> remoteFetcher) {
		this.localStore = localStore;
		this.remoteFetcher = remoteFetcher;
	}

	public Object fetch(String uri, RequestTimer timer) {
		
		Object local = localStore.getResource(uri);
		
		if (local == null) {
			return remoteFetcher.fetch(uri, timer); 
		} else {
		    return local;
		}
	}

}
