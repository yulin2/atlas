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

package org.uriplay.remotesite.ted;

import javax.xml.bind.JAXBException;

import org.jherd.remotesite.FetchException;
import org.jherd.remotesite.SiteSpecificAdapter;
import org.jherd.remotesite.http.RemoteSiteClient;
import org.jherd.remotesite.timing.RequestTimer;
import org.uriplay.media.entity.Item;
import org.uriplay.remotesite.html.HtmlDescriptionOfItem;
import org.uriplay.remotesite.html.HtmlDescriptionSource;

/**
 * {@link SiteSpecificRepresentationAdapter} for Channel 4 Brands.
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 * @author John Ayres (john@metabroadcast.com)
 */
public class TedTalkAdapter implements SiteSpecificAdapter<Item> {

	private final RemoteSiteClient<HtmlDescriptionOfItem> itemClient;
	private final TedTalkGraphExtractor propertyExtractor;

	private static final String baseUri = "http://www.ted.com/talks";

	public TedTalkAdapter() throws JAXBException {
		this(new TedTalkClient(), new TedTalkGraphExtractor());
	}
	
	public TedTalkAdapter(RemoteSiteClient<HtmlDescriptionOfItem> client, TedTalkGraphExtractor propertyExtractor) {
		this.itemClient = client;
		this.propertyExtractor = propertyExtractor;
	}

	public Item fetch(String uri, RequestTimer timer) {
		try {
			HtmlDescriptionOfItem dmItem = itemClient.get(uri);
			return propertyExtractor.extractFrom(new HtmlDescriptionSource(dmItem, uri));
		} catch (Exception e) {
			throw new FetchException("Problem processing html page from ted.com", e);
		}
	}

	public boolean canFetch(String uri) {
		return uri.startsWith(baseUri) && uri.length() > baseUri.length();
	}
}
