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

package org.atlasapi.remotesite.ted;

import org.atlasapi.remotesite.ted.TedTalkAdapter.TedTalkCanonicaliser;
import org.jmock.integration.junit3.MockObjectTestCase;

/**
 * Unit test for {@link TedTalkAdapter}.
 * 
 * @author Robert Chatley (robert@metabroadcast.com)
 * @author John Ayres (john@metabroadcast.com)
 */
public class TedTalkAdapterTest extends MockObjectTestCase {
	
	
	public void testCanFetchResourcesForTedTalks() throws Exception {
		
		TedTalkAdapter adapter = new TedTalkAdapter(null, null);

		assertFalse(adapter.canFetch("http://www.channel4.com/"));
		assertFalse(adapter.canFetch("http://www.ted.com/"));
		assertTrue(adapter.canFetch("http://www.ted.com/talks/ray_kurzweil_on_how_technology_will_transform_us.html"));
		assertTrue(adapter.canFetch("http://www.ted.com/talks/joe_derisi_hunts_the_next_killer_virus.html"));
		
		// the canonicaliser will handle these
		assertFalse(adapter.canFetch("http://www.ted.com/talks/lang/eng/joe_derisi_hunts_the_next_killer_virus.html"));
	}
	
	public void testCanCanonicaliseAUri() throws Exception {
		TedTalkCanonicaliser canonicaliser = new TedTalkAdapter.TedTalkCanonicaliser();
		assertEquals("http://www.ted.com/talks/joe_derisi_hunts_the_next_killer_virus.html", canonicaliser.canonicalise("http://www.ted.com/talks/lang/eng/joe_derisi_hunts_the_next_killer_virus.html"));
	}
}
