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

package org.uriplay.remotesite.bbc;

import org.jherd.remotesite.Fetcher;
import org.jmock.integration.junit3.MockObjectTestCase;

@SuppressWarnings("unchecked")
public class BbcUriCanonicaliserTest extends MockObjectTestCase {

	Fetcher<Object> fetcher;
	BbcUriCanonicaliser canonicaliser;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		fetcher = mock(Fetcher.class);
		canonicaliser = new BbcUriCanonicaliser();
	}

	public void testThatTheAdapterCanFetchBbcAlternatives() throws Exception {
		check("http://www.bbc.co.uk/iplayer/episode/b00m0xk9/Rick_Steins_Far_Eastern_Odyssey_Episode_4", "b00m0xk9");
		check("http://bbc.co.uk/i/m70wf/", "b00m70wf");
		check("http://www.bbc.co.uk/iplayer/_proxy_/episode/b00m0xk9/", "b00m0xk9");
		check("http://www.bbc.co.uk/iplayer/events/Proms/b00ml71k/", "b00ml71k");
		check("http://www.bbc.co.uk/iplayer/episode/b00ksth7", "b00ksth7");
		checkIsNull("http://www.bbc.co.uk/iplayer/episode/b00ksth7.rdf");
		checkIsNull("http://www.bbc.co.uk/iplayer/episode/b00ksth7.foo");
		check("http://www.bbc.co.uk/programmes/b00n156z.rdf", "b00n156z");
		checkIsNull("http://www.bbc.co.uk/programmes/b00n156z.foo");
		check("http://fanhu.bz/b00mx9xb", "b00mx9xb");
		checkIsNull("http://not-a-bbc-uri");
		checkIsNull("http://bbc.co.uk/i/m70wf.rdf");
	}

	public void testCanGenerateCurieForUri() throws Exception {
		assertEquals("bbc:b00ksth7", BbcUriCanonicaliser.curieFor("http://www.bbc.co.uk/iplayer/episode/b00ksth7"));
		assertEquals("bbc:b00m0xk9", BbcUriCanonicaliser.curieFor("http://www.bbc.co.uk/iplayer/episode/b00m0xk9/Rick_Steins_Far_Eastern_Odyssey_Episode_4"));
	}
	
	private void checkIsNull(String uri) {
		assertNull(canonicaliser.canonicalise(uri));		
	}

	private void check(String alternate, final String expected) {
		final String canonicalUri = "http://www.bbc.co.uk/programmes/" + expected;
		assertEquals(canonicalUri, canonicaliser.canonicalise(alternate));
	}
}
