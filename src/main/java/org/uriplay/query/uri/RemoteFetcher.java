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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jherd.beans.BeanGraphErrors;
import org.jherd.beans.GraphFactory;
import org.jherd.beans.Representation;
import org.jherd.persistence.BeanStore;
import org.jherd.rdf.entity.Description;
import org.jherd.remotesite.FetchException;
import org.jherd.remotesite.Fetcher;
import org.jherd.remotesite.NoMatchingAdapterException;
import org.jherd.remotesite.timing.RequestTimer;

import com.google.common.collect.Sets;

/**
 * {@link Fetcher} that fetches data from a remote site, transforms the resulting {@link Representation}
 * into a bean graph, and store the results into {@link BeanStore}.
 * 
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class RemoteFetcher implements Fetcher<Set<Object>> {

	private final Log log = LogFactory.getLog(getClass());
	
	private final Fetcher<Representation> fetcher;
	private final GraphFactory graphFactory;

	private Fetcher<Description> newStyleFetcher = null;
	
	public RemoteFetcher(Fetcher<Representation> fetcher, GraphFactory graphFactory) {
		this.fetcher = fetcher;
		this.graphFactory = graphFactory;
	}

	public Set<Object> fetch(String uri, RequestTimer timer) {
		try {
			return oldStyleFetch(uri, timer);
		} catch (NoMatchingAdapterException e) {
			return newStyleFetch(uri, timer);
		}
	}

	private Set<Object> newStyleFetch(String uri, RequestTimer timer) {
		return Sets.<Object>newHashSet(newStyleFetcher.fetch(uri, timer));
	}

	private Set<Object> oldStyleFetch(String uri, RequestTimer timer) {
		Representation representation = fetcher.fetch(uri, timer);

		Set<Object> beans = null;

		if (representation != null) {
			try {
				beans = graphFactory.createGraph(representation, new BeanGraphErrors());
			} catch (Exception e) {
				throw new FetchException("Problem fetching uri: " + uri, e);
			}
		}
		
		return beans;
	}

	public Set<Object> fetch(String uri) {
		throw new UnsupportedOperationException("Use fetch(uri, timer) instead");
	}
	
	public void setNewStyleFetcher(Fetcher<Description> newStyleFetcher) {
		this.newStyleFetcher = newStyleFetcher;
	}
}
