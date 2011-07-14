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

package org.atlasapi.remotesite.bbc;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.HttpClients;

import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.HttpResponsePrologue;
import com.metabroadcast.common.http.HttpResponseTransformer;
import com.metabroadcast.common.http.HttpStatusCode;
import com.metabroadcast.common.http.HttpStatusCodeException;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.http.SimpleHttpRequest;

/**
 * Client to retrieve XML from Channel 4 and bind it to our object model using JAXB. 
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 * @author John Ayres (john@metabroadcast.com)
 */
public class BbcSlashProgrammesRdfClient<T> implements RemoteSiteClient<T> {

	private final SimpleHttpClient httpClient;
	private final JAXBContext context;

	public BbcSlashProgrammesRdfClient(Class<T> clazz) {
		this(HttpClients.webserviceClient(), clazz);
	}
	
	public BbcSlashProgrammesRdfClient(SimpleHttpClient httpClient, Class<T> clazz) {
		this.httpClient = httpClient;
		try {
			context = JAXBContext.newInstance(clazz);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}
	
	private final HttpResponseTransformer<T> TRANSFORMER = new HttpResponseTransformer<T>() {

        @Override
        @SuppressWarnings("unchecked")
        public T transform(HttpResponsePrologue prologue, InputStream body) throws HttpException, JAXBException {
            if (!HttpStatusCode.OK.is(prologue.statusCode())) {
                throw new HttpStatusCodeException(prologue, "");
            }
            Unmarshaller u = context.createUnmarshaller();
            return (T) u.unmarshal(body);
        }
    };

	public T get(String uri) throws Exception {
	    return httpClient.get(new SimpleHttpRequest<T>(uri, TRANSFORMER));
	}
}
