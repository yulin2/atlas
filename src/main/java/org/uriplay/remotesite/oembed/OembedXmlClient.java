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

import java.io.Reader;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.uriplay.feeds.OembedItem;
import org.uriplay.persistence.system.RemoteSiteClient;
import org.uriplay.remotesite.HttpClients;

import com.metabroadcast.common.http.SimpleHttpClient;

/**
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class OembedXmlClient implements RemoteSiteClient<OembedItem> {

	private final SimpleHttpClient httpClient;
	private final JAXBContext context;
	
	public OembedXmlClient() throws JAXBException {
		this(HttpClients.webserviceClient());
	}
	
	public OembedXmlClient(SimpleHttpClient httpClient) throws JAXBException {
		this.httpClient = httpClient;
		context = JAXBContext.newInstance(OembedItem.class);
	}

	public OembedItem get(String uri) throws Exception {
		Reader in = new StringReader(httpClient.getContentsOf(uri));
		Unmarshaller u = context.createUnmarshaller();
		OembedItem oembedItem =(OembedItem) u.unmarshal(in);
		return oembedItem;
	}
}
