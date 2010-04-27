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

import javax.xml.bind.JAXBException;

import org.jherd.beans.BeanGraphExtractor;
import org.jherd.beans.Representation;
import org.jherd.beans.id.IdGeneratorFactory;
import org.jherd.remotesite.FetchException;
import org.jherd.remotesite.SiteSpecificRepresentationAdapter;
import org.jherd.remotesite.http.RemoteSiteClient;
import org.jherd.remotesite.timing.RequestTimer;
import org.uriplay.feeds.OembedItem;

/**
 * {@link SiteSpecificRepresentationAdapter} for oEmbed XML fragments. Should be configured
 * with an oEmbed endpoint URL from which to request oEmbed XML fragment corresponding
 * to given URI.
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class OembedXmlAdapter implements SiteSpecificRepresentationAdapter {

	private final RemoteSiteClient<OembedItem> oembedClient;
	private final BeanGraphExtractor<OembedSource> propertyExtractor;
	private String oembedEndpoint;
	private Pattern acceptedUriPattern;
	private Integer maxWidth;
	private Integer maxHeight;

	public OembedXmlAdapter(IdGeneratorFactory idGeneratorFactory) throws JAXBException {
		this(new OembedXmlClient(), new OembedGraphExtractor(idGeneratorFactory));
	}
	
	public OembedXmlAdapter(RemoteSiteClient<OembedItem> oembedClient, BeanGraphExtractor<OembedSource> propertyExtractor) {
		this.oembedClient = oembedClient;
		this.propertyExtractor = propertyExtractor;
	}

	public Representation fetch(String uri, RequestTimer timer) {
		try {
		String queryUri = oembedEndpointQuery(uri);
		OembedItem oembed = oembedClient.get(queryUri);
			return propertyExtractor.extractFrom(new OembedSource(oembed, uri));
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

}
