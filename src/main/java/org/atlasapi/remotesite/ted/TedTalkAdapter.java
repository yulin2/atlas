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

package org.atlasapi.remotesite.ted;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.query.uri.canonical.Canonicaliser;
import org.atlasapi.remotesite.FetchException;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.html.HtmlDescriptionOfItem;
import org.atlasapi.remotesite.html.HtmlDescriptionSource;

/**
 * {@link SiteSpecificRepresentationAdapter} for Channel 4 Brands.
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 * @author John Ayres (john@metabroadcast.com)
 */
public class TedTalkAdapter implements SiteSpecificAdapter<Item> {

	private final RemoteSiteClient<HtmlDescriptionOfItem> itemClient;
	private final TedTalkGraphExtractor propertyExtractor;

	private static final String TED_BASE_URI = "http://www.ted.com/talks/";
	
	private static final Pattern CANONICAL_URI_PATTERN = Pattern.compile(TED_BASE_URI + "([^\\s/]+).html");
	private static final Pattern ALIAS_URI_PATTERN = Pattern.compile("http://www.ted.com/talks/lang/eng/([^\\s/]+).html");

	public TedTalkAdapter() {
		this(new TedTalkClient(), new TedTalkGraphExtractor());
	}
	
	public TedTalkAdapter(RemoteSiteClient<HtmlDescriptionOfItem> client, TedTalkGraphExtractor propertyExtractor) {
		this.itemClient = client;
		this.propertyExtractor = propertyExtractor;
	}

	public Item fetch(String uri) {
		try {
			HtmlDescriptionOfItem dmItem = itemClient.get(uri);
			return propertyExtractor.extractFrom(new HtmlDescriptionSource(dmItem, uri));
		} catch (Exception e) {
			throw new FetchException("Problem processing html page from ted.com", e);
		}
	}

	public boolean canFetch(String uri) {
		return CANONICAL_URI_PATTERN.matcher(uri).matches();
	}
	
	public static class TedTalkCanonicaliser implements Canonicaliser {

		@Override
		public String canonicalise(String uri) {
			Matcher matcher = ALIAS_URI_PATTERN.matcher(uri);
			if (matcher.matches()) {
				return TED_BASE_URI + matcher.group(1) + ".html";
			}
			return null;
		}
	}
}
