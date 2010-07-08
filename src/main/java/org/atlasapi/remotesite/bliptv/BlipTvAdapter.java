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

package org.atlasapi.remotesite.bliptv;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.query.uri.canonical.Canonicaliser;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.FetchException;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.html.HtmlDescriptionOfItem;
import org.atlasapi.remotesite.html.HtmlDescriptionSource;

/**
 * {@link SiteSpecificAdapter} for http://blip.tv
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class BlipTvAdapter implements SiteSpecificAdapter<Item> {

	private final RemoteSiteClient<HtmlDescriptionOfItem> itemClient;
	private final ContentExtractor<HtmlDescriptionSource, Item> propertyExtractor;

	private static final Pattern CANONICAL_URI_PATTERN = Pattern.compile("http://blip.tv/file/[\\d]+");
	private static final Pattern ALIAS_PATTERN = Pattern.compile("(http://blip.tv/file/[\\d]+).*");

	public BlipTvAdapter() {
		this(new BlipTvClient(), new BlipTvGraphExtractor());
	}
	
	public BlipTvAdapter(RemoteSiteClient<HtmlDescriptionOfItem> client, ContentExtractor<HtmlDescriptionSource, Item> contentExtractor) {
		this.itemClient = client;
		this.propertyExtractor = contentExtractor;
	}

	public Item fetch(String uri) {
		try {
			HtmlDescriptionOfItem itemDescription = itemClient.get(uri);
			return propertyExtractor.extract(new HtmlDescriptionSource(itemDescription, uri).withEmbedCode(embedCode(itemDescription.getVideoSource())));
		} catch (Exception e) {
			throw new FetchException("Problem processing html page from blip.tv", e);
		}
	}

	private String embedCode(String videoSource) {
		return "<embed src=\"" + videoSource + "\" type=\"application/x-shockwave-flash\" width=\"320\" height=\"270\" allowscriptaccess=\"always\" allowfullscreen=\"true\"></embed>";
	}

	public boolean canFetch(String uri) {
		return CANONICAL_URI_PATTERN.matcher(uri).matches();
	}
	
	public static class BlipTvCanonicaliser implements Canonicaliser {

		@Override
		public String canonicalise(String uri) {
			Matcher matcher = ALIAS_PATTERN.matcher(uri);
			if (matcher.matches()) {
				return matcher.group(1);
			}
			return null;
		}
		
	}
}
