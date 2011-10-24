/* Copyright 2010 Meta Broadcast Ltd

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License. */

package org.atlasapi.remotesite.support.atom;

import static com.metabroadcast.common.http.SimpleHttpRequest.httpRequestFrom;

import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.http.HttpServletResponse;

import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.HttpClients;

import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.HttpResponsePrologue;
import com.metabroadcast.common.http.HttpResponseTransformer;
import com.metabroadcast.common.http.HttpStatusCodeException;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.io.WireFeedInput;

public class AtomClient implements RemoteSiteClient<Feed> {
		
	private final SimpleHttpClient client;

	public AtomClient(SimpleHttpClient client) {
		this.client = client;
	}
	
	public AtomClient() {
		this(HttpClients.webserviceClient());
	}

	@Override
	public Feed get(final String uri) throws Exception {
	    return client.get(httpRequestFrom(uri, new HttpResponseTransformer<Feed>() {
            @Override
            public Feed transform(HttpResponsePrologue prologue, InputStream body) throws HttpException, Exception {
            	if(prologue.statusCode() != HttpServletResponse.SC_OK) {
            		throw new HttpStatusCodeException(prologue, String.format("Cannot transform response for %s as http status %d was returned", uri, prologue.statusCode()));
            	}
                return (Feed) new WireFeedInput().build(new InputStreamReader(body));
            }
        }));
	}
}
