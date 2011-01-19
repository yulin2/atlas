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

package org.atlasapi.remotesite;

import java.util.List;

import org.atlasapi.media.entity.Identified;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.persistence.system.Fetcher;


/**
 * {@link Fetcher} that retrieves a document via a configured delegate
 * {@link Fetcher} and applies a site-specific {@link SiteSpecificAdapter} to the
 * resulting document before returning it. 
 *
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class PerSiteAdapterDispatcher implements Fetcher<Identified> {

	private List<SiteSpecificAdapter<? extends Identified>> adapters;
	private final AdapterLog log;

	public PerSiteAdapterDispatcher(AdapterLog log) {
		this.log = log;
	}

	public Identified fetch(String uri) {
		SiteSpecificAdapter<? extends Identified> adapter = findMatchingAdapterFor(uri);
		if (adapter != null) {
			try {
				return adapter.fetch(uri);
			} catch (RuntimeException e) {
				log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(adapter.getClass()));
				throw e;
			}
		} else {
			return null;
		}
	}

	private SiteSpecificAdapter<? extends Identified> findMatchingAdapterFor(String uri) {
		for (SiteSpecificAdapter<? extends Identified> adapter : adapters) {
			if (adapter.canFetch(uri)) {
				return adapter;
			}
		}
		throw new NoMatchingAdapterException("No configured adapter matched URI " + uri);
	}

	public void setAdapters(List<SiteSpecificAdapter<? extends Identified>> adapters) {
		this.adapters = adapters;
	}
}
