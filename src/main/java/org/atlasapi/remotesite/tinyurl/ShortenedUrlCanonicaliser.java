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

package org.atlasapi.remotesite.tinyurl;

import java.util.Collection;

import org.atlasapi.http.RedirectShortUrlResolver;
import org.atlasapi.http.ShortUrlResolver;
import org.atlasapi.query.uri.canonical.Canonicaliser;

/**
 * Simple {@link Canonicaliser} that resolves shortened url redirects from sites like tinyurl.com 
 * 
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class ShortenedUrlCanonicaliser implements Canonicaliser {

	private final ShortUrlResolver shortUrlResolver;
	private final Collection<String> shortUrlServices;

	public ShortenedUrlCanonicaliser(Collection<String> shortUrlServices) {
		this.shortUrlServices = shortUrlServices;
		this.shortUrlResolver = new RedirectShortUrlResolver();
	}
	
	public ShortenedUrlCanonicaliser() {
		this(ShortUrlServices.ALL);
	}

	@Override
	public String canonicalise(String uri) {
		if (!uri.startsWith("http") || !isProbablyAShortUrlService(uri)) {
			return null;
		}
		return shortUrlResolver.resolve(uri);
	}

	private boolean isProbablyAShortUrlService(String uri) {
		for (String shortUrlService : shortUrlServices) {
			if (uri.startsWith(shortUrlService)) {
				return true;
			}
		}
		return false;
	}
}
