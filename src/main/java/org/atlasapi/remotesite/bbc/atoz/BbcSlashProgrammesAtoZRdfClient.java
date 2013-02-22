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

package org.atlasapi.remotesite.bbc.atoz;

import java.io.InputStreamReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.atlasapi.http.AbstractHttpResponseTransformer;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metabroadcast.common.http.HttpResponseTransformer;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.http.SimpleHttpRequest;

/**
 * Client to retrieve RDF XML representing a version, from BBC /programmes and 
 * bind it to our object model using JAXB. 
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class BbcSlashProgrammesAtoZRdfClient implements RemoteSiteClient<SlashProgrammesAtoZRdf> {
    
    private static final Logger log = LoggerFactory.getLogger(BbcSlashProgrammesAtoZRdfClient.class);

	private final SimpleHttpClient httpClient;
	private final JAXBContext context;
    private final HttpResponseTransformer<SlashProgrammesAtoZRdf> rdfResponseTransformer = new AbstractHttpResponseTransformer<SlashProgrammesAtoZRdf>() {
        @Override
        protected SlashProgrammesAtoZRdf transform(InputStreamReader bodyReader) throws Exception {
            Unmarshaller u = context.createUnmarshaller();
            return (SlashProgrammesAtoZRdf) u.unmarshal(bodyReader);
        }
    };

	public BbcSlashProgrammesAtoZRdfClient() {
		this(HttpClients.webserviceClient());
	}
	
	public BbcSlashProgrammesAtoZRdfClient(SimpleHttpClient httpClient) {
		this.httpClient = httpClient;
		try {
			context = JAXBContext.newInstance(SlashProgrammesAtoZRdf.class);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	public SlashProgrammesAtoZRdf get(String uri) throws Exception {
        log.info(uri);
        return httpClient.get(SimpleHttpRequest.httpRequestFrom(uri, rdfResponseTransformer));
    }

}
