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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;

import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.jherd.remotesite.FetchException;
import org.jherd.remotesite.http.CommonsHttpClient;
import org.jherd.remotesite.http.RemoteSiteClient;
import org.uriplay.remotesite.html.HtmlDescriptionOfItem;
import org.uriplay.remotesite.html.HtmlNavigator;

/**
 * Client that tries to extract a title and an embedded video from a given webpage.
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class EmbeddedVideoClient implements RemoteSiteClient<HtmlDescriptionOfItem>  {

	private final RemoteSiteClient<Reader> client;

	public EmbeddedVideoClient(RemoteSiteClient<Reader> client) {
		this.client = client;
	}

	public EmbeddedVideoClient() {
		this(new CommonsHttpClient());
	}

	public HtmlDescriptionOfItem get(String uri) throws Exception {
		Reader in = client.get(uri);
		
		HtmlNavigator html = new HtmlNavigator(in);
		HtmlDescriptionOfItem item = new HtmlDescriptionOfItem();

		item.setTitle(html.titleTagContents());
		item.setEmbedObject(embedObjectFrom(html, uri));
		
		return item;
	}

	private String embedObjectFrom(HtmlNavigator html, String uri) {
		
		XMLOutputter out = new XMLOutputter();
		
		Element content = html.firstElementOrNull("//embed");
		
		if (content == null) {
			content = html.firstElementOrNull("//video");
		}
		
		if (content == null) { throw new FetchException("could not locate a video embedded on " + uri); }

		// deal with case where located embed tag is inside an object tag.
		if (content.getParentElement().getName().equals("object")) {
			content = content.getParentElement();
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		try {
			out.output(content, baos);
		} catch (IOException e) {
			throw new FetchException("Error rendering XML element", e);
		}
		String embedObject = baos.toString();
		return tryToMakeRelativeUrlsAbsolute(embedObject, uri);
	}

	private String tryToMakeRelativeUrlsAbsolute(String embedObject, String uri) {
		return embedObject.replaceAll("src=\"(?!http://)(.+?)\"", "src=\"" +
				baseUriFrom(uri) + "$1\"");
	}

	private String baseUriFrom(String uri) {
		if (uri.endsWith("/")) { return uri; }
		return uri.substring(0, uri.lastIndexOf("/") + 1);
	}

}
