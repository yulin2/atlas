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
 * {@link SiteSpecificRepresentationAdapter} for DailyMotion.com.
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 * @author John Ayres (john@metabroadcast.com)
 */
public class DailyMotionItemAdapter implements SiteSpecificRepresentationAdapter {

	private final RemoteSiteClient<HtmlDescriptionOfItem> dailyMotionItemClient;
	private final BeanGraphExtractor<HtmlDescriptionSource> propertyExtractor;

	private static final String baseUri = "http://www.dailymotion.com";

	public DailyMotionItemAdapter(IdGeneratorFactory idGen) throws JAXBException {
		this(new DailyMotionItemClient(), new DailyMotionItemGraphExtractor(idGen));
	}
	
	public DailyMotionItemAdapter(RemoteSiteClient<HtmlDescriptionOfItem> client, BeanGraphExtractor<HtmlDescriptionSource> propertyExtractor) {
		this.dailyMotionItemClient = client;
		this.propertyExtractor = propertyExtractor;
	}

	public Representation fetch(String uri, RequestTimer timer) {
		try {
			HtmlDescriptionOfItem dmItem = dailyMotionItemClient.get(uri);
			return propertyExtractor.extractFrom(new HtmlDescriptionSource(dmItem, uri));
		} catch (Exception e) {
			throw new FetchException("Problem processing html page from dailymotion.com", e);
		}
	}

	public boolean canFetch(String uri) {
		return uri.startsWith(baseUri) && uri.length() > baseUri.length() && uri.contains("/video/");
	}
}
