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

package org.uriplay.remotesite.embedded;

import javax.xml.bind.JAXBException;

import org.jherd.beans.BeanGraphExtractor;
import org.jherd.beans.Representation;
import org.jherd.beans.id.IdGeneratorFactory;
import org.jherd.remotesite.FetchException;
import org.jherd.remotesite.SiteSpecificRepresentationAdapter;
import org.jherd.remotesite.http.RemoteSiteClient;
import org.jherd.remotesite.timing.RequestTimer;
import org.uriplay.remotesite.html.HtmlDescriptionOfItem;
import org.uriplay.remotesite.html.HtmlDescriptionSource;

/**
 * {@link SiteSpecificRepresentationAdapter} forany site that embeds a video using an embed/object tag.
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class EmbeddedVideoAdapter implements SiteSpecificRepresentationAdapter {

	private final RemoteSiteClient<HtmlDescriptionOfItem> itemClient;
	private final BeanGraphExtractor<HtmlDescriptionSource> propertyExtractor;

	public EmbeddedVideoAdapter(IdGeneratorFactory idGen) throws JAXBException {
		this(new EmbeddedVideoClient(), new EmbeddedVideoGraphExtractor(idGen));
	}
	
	public EmbeddedVideoAdapter(RemoteSiteClient<HtmlDescriptionOfItem> client, BeanGraphExtractor<HtmlDescriptionSource> propertyExtractor) {
		this.itemClient = client;
		this.propertyExtractor = propertyExtractor;
	}

	public Representation fetch(String uri, RequestTimer timer) {
		try {
			HtmlDescriptionOfItem itemDescription = itemClient.get(uri);
			return propertyExtractor.extractFrom(new HtmlDescriptionSource(itemDescription, uri));
		} catch (Exception e) {
			throw new FetchException("Problem processing html page from " + uri, e);
		}
	}

	public boolean canFetch(String uri) {
		return uri.startsWith("http");
	}
}
