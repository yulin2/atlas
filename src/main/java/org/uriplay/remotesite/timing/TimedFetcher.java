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

package org.uriplay.remotesite.timing;

import org.uriplay.persistence.system.Fetcher;
import org.uriplay.persistence.system.RequestTimer;

/**
 * Base class for {@link SiteSpecificRepresentationAdapter}s (and other {@link Fetcher}s)
 * that want to start/stop a timer around their fetch call.
 * 
 * Subclasses should override fetchInternal(). The timer is passed to fetchInternal
 * but only for the case where the fetcher wants to pass it on to another delegate fetcher.
 *
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public abstract class TimedFetcher<T> {

	public T fetch(String uri, RequestTimer timer) {
		if (timer != null ) {
			try {
				timer.start(this, uri);
				return fetchInternal(uri, timer);
			} finally {
				timer.stop(this, uri);
			}
		} else {
			return fetchInternal(uri, timer);
		}
	}

	protected abstract T fetchInternal(String uri, RequestTimer timer);

}
