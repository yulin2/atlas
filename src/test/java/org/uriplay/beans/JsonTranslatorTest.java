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

import com.google.common.collect.Sets;

/**
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class JsonTranslatorTest extends TestCase {

	OutputStream stream = new ByteArrayOutputStream();
	Set<Object> graph = Sets.newHashSet();

	public void testCanOutputSimpleItemObjectModelAsXml() throws Exception {

		Item item = new Item();
		item.setTitle("Blue Peter");
		Location location = new Location();
		location.setUri("http://www.bbc.co.uk/bluepeter");
		item.addLocation(location);
		graph.add(item);
		
		new JsonTranslator().writeTo(graph, stream);
		
		String output = stream.toString();
		assertThat(output, containsString("\"uri\":\"http://www.bbc.co.uk/bluepeter\""));
		assertThat(output, containsString("\"title\":\"Blue Peter\""));
	}

}
