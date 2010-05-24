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

package org.uriplay.remotesite;

import java.util.List;

import org.uriplay.persistence.system.Fetcher;
import org.uriplay.persistence.system.RequestTimer;


/**
 * {@link Fetcher} that retrieves a document via a configured delegate
 * {@link Fetcher} and applies a site-specific {@link SiteSpecificAdapter} to the
 * resulting document before returning it. 
 *
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class PerSiteAdapterDispatcher implements Fetcher<Object> {

	private List<SiteSpecificAdapter<?>> adapters;

	public Object fetch(String uri, RequestTimer timer) {
		SiteSpecificAdapter<?> adapter = findMatchingAdapterFor(uri);
		if (adapter != null) {
			return adapter.fetch(uri, timer);
		} else {
			return null;
		}
	}

	private SiteSpecificAdapter<?> findMatchingAdapterFor(String uri) {
		for (SiteSpecificAdapter<?> adapter : adapters) {
			if (adapter.canFetch(uri)) {
				return adapter;
			}
		}
		throw new NoMatchingAdapterException("No configured adapter matched URI " + uri);
	}

	public void setAdapters(List<SiteSpecificAdapter<?>> adapters) {
		this.adapters = adapters;
	}
}
