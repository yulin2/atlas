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

package org.uriplay.remotesite.synd;

import java.net.URL;

import org.jherd.remotesite.http.RemoteSiteClient;

import com.sun.syndication.feed.opml.Opml;
import com.sun.syndication.io.WireFeedInput;
import com.sun.syndication.io.XmlReader;

/**
 * Simple wrapper for ROME OPML client, retrieves OPML feeds.
 * 
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class OpmlFeedClient implements RemoteSiteClient<Opml> {

	public Opml get(String uri) throws Exception {

		WireFeedInput input = new WireFeedInput();
		Opml feed = (Opml) input.build(new XmlReader(new URL(uri)));

		return feed;
	}

}
