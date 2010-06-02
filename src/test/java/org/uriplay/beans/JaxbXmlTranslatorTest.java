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

package org.uriplay.beans;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Set;

import junit.framework.TestCase;

import org.uriplay.media.entity.simple.Item;
import org.uriplay.media.entity.simple.Location;
import org.uriplay.media.entity.simple.Playlist;
import org.uriplay.media.entity.simple.UriplayQueryResult;

import com.google.common.collect.Sets;

/**
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class JaxbXmlTranslatorTest extends TestCase {

	OutputStream stream = new ByteArrayOutputStream();

	public void testCanOutputSimpleItemObjectModelAsXml() throws Exception {
		Set<Object> graph = Sets.newHashSet();

		Item item = new Item();
		item.setTitle("Blue Peter");
		item.setUri("http://www.bbc.co.uk/programmes/bluepeter");
		item.setAliases(Sets.newHashSet("http://www.bbc.co.uk/p/bluepeter"));
		Location location = new Location();
		location.setUri("http://www.bbc.co.uk/bluepeter");
		location.setEmbedCode("object><embed></embed></object>");
		item.addLocation(location);
		UriplayQueryResult uriplay = new UriplayQueryResult();
		uriplay.addItem(item);
		graph.add(uriplay);
		
		new JaxbXmlTranslator().writeTo(graph, stream);
		
		String output = stream.toString();
		assertThat(output, containsString("<play:item>" +
				                            "<aliases>" +
				                              "<alias>http://www.bbc.co.uk/p/bluepeter</alias>" +
				                            "</aliases>" +
				                            "<play:containedIn/>" +
				                            "<play:genres/>" +
				                            "<play:tags/>" +
                               				"<title>Blue Peter</title>" +
                               				"<uri>http://www.bbc.co.uk/programmes/bluepeter</uri>" +
											"<play:locations>" +
												"<play:location>" +
												"<available>true</available>" +
												"<embedCode><![CDATA[object><embed></embed></object>]]></embedCode>" +
												"<uri>http://www.bbc.co.uk/bluepeter</uri>" +
												"</play:location>" +
											"</play:locations>" +
										  "</play:item>"));
	}

	
	public void testCanOutputSimpleListObjectModelAsXml() throws Exception {
		Set<Object> graph = Sets.newHashSet();

		Playlist list = new Playlist();
		Item item = new Item();
		item.setTitle("Blue Peter");
		Location location = new Location();
		location.setUri("http://www.bbc.co.uk/bluepeter");
		item.addLocation(location);
		list.addItem(item);
		graph.add(list);
		
		new JaxbXmlTranslator().writeTo(graph, stream);
		
		assertThat(stream.toString(), containsString("<play:item>" +
														"<aliases/>" +
														"<play:containedIn/>" +
														"<play:genres/>" +
														"<play:tags/>" +
	                                         			"<title>Blue Peter</title>" +
														"<play:locations>" +
															"<play:location>" +
															"<available>true</available>" +
															"<uri>http://www.bbc.co.uk/bluepeter</uri>" +
															"</play:location>" +
														  "</play:locations>" +
													  "</play:item>"));
	}
}
