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

package org.atlasapi.query.uri;

import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.system.Fetcher;
import org.atlasapi.persistence.testing.StubContentResolver;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;

/**
 * Unit test for {@link LocalOrRemoteFetcher}.
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class LocalOrRemoteFetcherTest extends MockObjectTestCase {

	static final String URI = "http://example.com";
    
	Fetcher<Identified> remoteFetcher;
	
	Item bean = new Item(URI, URI, Publisher.BBC);
	
	@Override
	@SuppressWarnings("unchecked")
	protected void setUp() throws Exception {
		super.setUp();
		remoteFetcher = mock(Fetcher.class);
	}

	public void testQueriesRemoteFetcherForNewUri() throws Exception {
		ContentResolver resources = StubContentResolver.RESOLVES_NOTHING;
		LocalOrRemoteFetcher localOrRemoteFetcher = new LocalOrRemoteFetcher(resources, remoteFetcher);
		
		checking(new Expectations() {{ 
			one(remoteFetcher).fetch(URI); will(returnValue(bean));
		}});
		
		localOrRemoteFetcher.fetch(URI);
	}
	
	public void testLoadsKnownResourcesFromDatabaseAndDoesNotFetch() throws Exception {
		ContentResolver resources = new StubContentResolver().respondTo(bean);
		LocalOrRemoteFetcher localOrRemoteFetcher = new LocalOrRemoteFetcher(resources, remoteFetcher);
		localOrRemoteFetcher.fetch(URI);
	}
	
}
