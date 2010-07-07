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

package org.uriplay.remotesite.oembed;

import java.util.regex.Pattern;

import org.uriplay.feeds.OembedItem;
import org.uriplay.media.entity.Item;
import org.uriplay.persistence.system.RemoteSiteClient;
import org.uriplay.remotesite.ContentExtractor;
import org.uriplay.remotesite.FetchException;
import org.uriplay.remotesite.SiteSpecificAdapter;

/**
 * {@link SiteSpecificAdapter} for oEmbed XML fragments. Should be configured
 * with an oEmbed endpoint URL from which to request oEmbed XML fragment corresponding
 * to given URI.
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class OembedXmlAdapter implements SiteSpecificAdapter<Item> {

	private final RemoteSiteClient<OembedItem> oembedClient;
	private final ContentExtractor<OembedSource, Item> propertyExtractor;
	private String oembedEndpoint;
	private Pattern acceptedUriPattern;
	private Integer maxWidth;
	private Integer maxHeight;
	private String publisher;

	public OembedXmlAdapter() {
		this(new OembedXmlClient(), new OembedGraphExtractor());
	}
	
	public OembedXmlAdapter(RemoteSiteClient<OembedItem> oembedClient, ContentExtractor<OembedSource, Item> contentExtractor) {
		this.oembedClient = oembedClient;
		this.propertyExtractor = contentExtractor;
	}

	public Item fetch(String uri) {
		try {
			String queryUri = oembedEndpointQuery(uri);
			OembedItem oembed = oembedClient.get(queryUri);
			Item item = propertyExtractor.extract(new OembedSource(oembed, uri));
			item.setPublisher(publisher);
			return item;
		} catch (Exception e) {
			throw new FetchException("Problem processing XML from " + oembedEndpoint, e);
		}
	}

	public boolean canFetch(String uri) {
		if (acceptedUriPattern != null) {
			return acceptedUriPattern.matcher(uri).matches();
		}
		return true;
	}

	private String oembedEndpointQuery(String uri) {
		StringBuffer queryUri = new StringBuffer();
		queryUri.append(oembedEndpoint)
		        .append("?url=")
		        .append(uri);
		
		if (maxWidth != null) {
			queryUri.append("&maxwidth=").append(maxWidth);
		}
		
		if (maxHeight != null) {
			queryUri.append("&maxheight=").append(maxHeight);
		}
		
		return queryUri.toString();
	}
	
	public void setOembedEndpoint(String oembedEndpoint) {
		this.oembedEndpoint = oembedEndpoint;
	}

	public void setAcceptedUriPattern(String acceptedUriRegex) {
		acceptedUriPattern = Pattern.compile(acceptedUriRegex);
	}

	public void setMaxWidth(int maxWidth) {
		this.maxWidth = maxWidth;
	}

	public void setMaxHeight(int maxHeight) {
		this.maxHeight = maxHeight;
	}
	
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

}
