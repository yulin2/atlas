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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import junit.framework.TestCase;

import org.atlasapi.remotesite.bbc.SlashProgrammesVersionRdf.BbcBroadcast;
import org.joda.time.DateTime;

import com.google.common.io.Resources;
import com.metabroadcast.common.http.FixedResponseHttpClient;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.time.DateTimeZones;

/**
 * Unit test for {@link BbcSlashProgrammesVersionRdfClient}.
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class BbcSlashProgrammesVersionRdfClientTest extends TestCase {
	
	String URI = "http://example.com";
	
	private final SimpleHttpClient httpClient = FixedResponseHttpClient.respondTo(URI, Resources.getResource("top-gear-version-rdf.xml"));

	public void testBindsRetrievedXmlDocumentToObjectModel() throws Exception {
		
		SlashProgrammesVersionRdf version = new BbcSlashProgrammesRdfClient<SlashProgrammesVersionRdf>(httpClient, SlashProgrammesVersionRdf.class).get(URI);
		
		assertThat(version.broadcastSlots().size(), is(22));
		
		BbcBroadcast firstBroadcast = version.broadcastSlots().get(0);
		assertThat(firstBroadcast.broadcastDateTime(), is(new DateTime("2007-11-25T20:00:00Z", DateTimeZones.UTC)));
		assertThat(firstBroadcast.broadcastOn(), is("/services/bbctwo/ni_analogue#service"));
		assertThat(firstBroadcast.broadcastEndDateTime(), is(new DateTime("2007-11-25T21:00:00Z", DateTimeZones.UTC)));
		assertThat(firstBroadcast.broadcastType().isRepeatType(), is(false));
		assertThat(version.broadcastSlots().get(0).scheduleDate(), is("2007-11-25"));
	}

}
