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

package org.uriplay.persistence;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jherd.naming.ResourceMapping;
import org.jherd.persistence.BeanStore;
import org.jherd.remotesite.Fetcher;

/**
 * Refreshes data in the database by reading all the uris we know about, and doing
 * new queries to get the latest from each remote site. Intended to run as a job
 * scheduled by Quartz or some other scheduler.
 *
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class RemoteSiteRefresher implements Refresher {

	Log log = LogFactory.getLog(RemoteSiteRefresher.class);
	
	private final ResourceMapping externalResources;
	private final BeanStore beanStore;
	private final Fetcher<Set<Object>> fetcher;

	public RemoteSiteRefresher(ResourceMapping externalResources, BeanStore beanStore, Fetcher<Set<Object>> fetcher) {
		this.externalResources = externalResources;
		this.beanStore = beanStore;
		this.fetcher = fetcher;
	}

	/**
	 * TODO: refreshAll should query to fetch only those resources that require refreshing
	 * TODO  rather than load every resource into memory; which should also be carried out
	 * TODO  in batches.
	 */
	public void refreshAll() {
		Set<Object> resources = beanStore.getAllResources();
		
		for (Object resource : resources) {
			for (String uri : externalResources.getUris(resource)) {
				Set<Object> beans = fetcher.fetch(uri, null);
				beanStore.store(beans);
			}
		}
	}

}
