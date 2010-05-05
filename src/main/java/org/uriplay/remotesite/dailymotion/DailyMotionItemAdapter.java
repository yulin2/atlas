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

package org.uriplay.remotesite.dailymotion;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jherd.remotesite.FetchException;
import org.jherd.remotesite.SiteSpecificAdapter;
import org.jherd.remotesite.http.RemoteSiteClient;
import org.jherd.remotesite.timing.RequestTimer;
import org.uriplay.media.entity.Item;
import org.uriplay.query.uri.canonical.Canonicaliser;
import org.uriplay.remotesite.ContentExtractor;
import org.uriplay.remotesite.html.HtmlDescriptionOfItem;
import org.uriplay.remotesite.html.HtmlDescriptionSource;

/**
 * {@link SiteSpecificAdapter} for DailyMotion.com.
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 * @author John Ayres (john@metabroadcast.com)
 */
public class DailyMotionItemAdapter implements SiteSpecificAdapter<Item> {

	private final RemoteSiteClient<HtmlDescriptionOfItem> dailyMotionItemClient;
	private final ContentExtractor<HtmlDescriptionSource, Item> itemExtractor;

	private static final String BASE_URI = "http://www.dailymotion.com/";
	private static final Pattern CANONICAL_URI_PATTERN = Pattern.compile(BASE_URI + "video/[^/\\s\\.]+");
	
	private static final Pattern ALIAS_URI_PATTERN = Pattern.compile(BASE_URI + "(.*)/(video/[^/\\s\\.]+)");

	public DailyMotionItemAdapter() {
		this(new DailyMotionItemClient(), new DailyMotionItemGraphExtractor());
	}
	
	public DailyMotionItemAdapter(RemoteSiteClient<HtmlDescriptionOfItem> client, ContentExtractor<HtmlDescriptionSource, Item> itemExtractor) {
		this.dailyMotionItemClient = client;
		this.itemExtractor = itemExtractor;
	}

	public Item fetch(String uri, RequestTimer timer) {
		try {
			HtmlDescriptionOfItem dmItem = dailyMotionItemClient.get(uri);
			return itemExtractor.extract(new HtmlDescriptionSource(dmItem, uri));
		} catch (Exception e) {
			throw new FetchException("Problem processing html page from dailymotion.com", e);
		}
	}

	public boolean canFetch(String uri) {
		return CANONICAL_URI_PATTERN.matcher(uri).matches();
	}
	
	public static class DailyMotionItemCanonicaliser implements Canonicaliser {

		@Override
		public String canonicalise(String uri) {
			Matcher matcher = ALIAS_URI_PATTERN.matcher(uri);
			if (matcher.matches()) {
				return BASE_URI + matcher.group(2);
			}
			return null;
		}
	}
}
