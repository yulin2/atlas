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

package org.uriplay.remotesite.itv;

import java.io.Reader;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;
import org.uriplay.persistence.system.RemoteSiteClient;
import org.uriplay.remotesite.FetchException;
import org.uriplay.remotesite.html.HtmlNavigator;
import org.uriplay.remotesite.http.CommonsHttpClient;

/**
 * Client to retrieve XML/HTML from ITV and bind it to our object model. 
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 * @author John Ayres (john@metabroadcast.com)
 */
public class ItvCatchupClient implements RemoteSiteClient<List<ItvProgramme>> {

	private final RemoteSiteClient<Reader> httpClient;
	private final JAXBContext context;

	private static final Log log = LogFactory.getLog(ItvCatchupClient.class);
	
	public ItvCatchupClient() throws JAXBException {
		this(new CommonsHttpClient().withAcceptHeader("text/html"));
	}
	
	public ItvCatchupClient(RemoteSiteClient<Reader> httpClient) throws JAXBException {
		this.httpClient = httpClient;
		context = JAXBContext.newInstance(ItvProgrammes.class, ItvProgramme.class);
	}

	public List<ItvProgramme> get(String uri) throws Exception {
		Reader in = httpClient.get(uri);
		Unmarshaller u = context.createUnmarshaller();
		u.setSchema(null);
		ItvProgrammes itvProgrammes = (ItvProgrammes) u.unmarshal(in);
		for (ItvProgramme program : itvProgrammes.programmeList()) {
			log.info("fetching details for: " + program.url());
			fetchEpisodeDetails(program);
		}
		return itvProgrammes.programmeList();
	}

	private void fetchEpisodeDetails(ItvProgramme program) {

		int programmeId = program.programmeId();
		
		try {
			
			Reader Reader = httpClient.get("http://www.itv.com/_app/Dynamic/CatchUpData.ashx?ViewType=1&Filter=" + programmeId + "&moduleID=262033&columnWidth=2");
			
			HtmlNavigator htmlNavigator = new HtmlNavigator(Reader);
			List<Element> content = htmlNavigator.allElementsMatching("//div[@class='content']");
			
			addEpisodesTo(program, htmlNavigator, content);
			
		} catch (Exception e) {
			throw new FetchException("Error fetching ITV programme with id: " + programmeId, e);
		}
		
	}

	private void addEpisodesTo(ItvProgramme program, HtmlNavigator htmlNavigator, List<Element> content) {
	
		for (Element element : content) {
			
			Element link = htmlNavigator.firstElementOrNull("./h3/a", element);
			String episodePage = link.getAttributeValue("href");
			Element description = htmlNavigator.firstElementOrNull("./p[@class='progDesc']", element);
			Element date = htmlNavigator.firstElementOrNull("./p[@class='date']", element);
			
			program.addEpisode(new ItvEpisode(date.getText(), description.getText(), episodePage));
		}
	}

}
