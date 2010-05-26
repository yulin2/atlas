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

package org.uriplay.remotesite.bbc;

import java.io.Reader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.uriplay.persistence.system.RemoteSiteClient;
import org.uriplay.remotesite.http.CommonsHttpClient;

/**
 * Client to retrieve XML from Channel 4 and bind it to our object model using JAXB. 
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 * @author John Ayres (john@metabroadcast.com)
 */
public class BbcSlashProgrammesEpisodeRdfClient implements RemoteSiteClient<SlashProgrammesRdf> {

	private final RemoteSiteClient<Reader> httpClient;
	private final JAXBContext context;

	public BbcSlashProgrammesEpisodeRdfClient() {
		this(new CommonsHttpClient().withAcceptHeader("text/html"));
	}
	
	public BbcSlashProgrammesEpisodeRdfClient(RemoteSiteClient<Reader> httpClient) {
		this.httpClient = httpClient;
		try {
			context = JAXBContext.newInstance(SlashProgrammesRdf.class);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	public SlashProgrammesRdf get(String uri) throws Exception {
		Reader in = httpClient.get(uri);
		Unmarshaller u = context.createUnmarshaller();
		SlashProgrammesRdf episodeDescription =(SlashProgrammesRdf) u.unmarshal(in);
		return episodeDescription;
	}
}
