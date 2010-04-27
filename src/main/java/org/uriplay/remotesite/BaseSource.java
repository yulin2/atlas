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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jherd.beans.BeanGraphExtractor;

/**
 * Convenience base class for the 'Source' classes that are commonly
 * passed from {@link SiteSpecificAdapter}s to {@link BeanGraphExtractor}s.
 *
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public abstract class BaseSource {

	private final String uri;

	public BaseSource(String uri) {
		this.uri = uri;
	}

	public String getUri() {
		return uri;
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

}
