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

package org.uriplay.query.uri;

import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.uriplay.media.entity.Content;
import org.uriplay.media.entity.Description;
import org.uriplay.media.entity.Item;
import org.uriplay.persistence.content.ContentStore;
import org.uriplay.persistence.system.Fetcher;

/**
 * Unit test for {@link LocalOrRemoteFetcher}.
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class LocalOrRemoteFetcherTest extends MockObjectTestCase {

	static final String URI = "http://example.com";
    
	Fetcher<Content> remoteFetcher;
	ContentStore resources;
	
	Fetcher<Content> localOrRemoteFetcher;
	
	Description bean = new Item();
	
	@Override
	@SuppressWarnings("unchecked")
	protected void setUp() throws Exception {
		super.setUp();
		remoteFetcher = mock(Fetcher.class);
		resources = mock(ContentStore.class);
		localOrRemoteFetcher = new LocalOrRemoteFetcher(resources, remoteFetcher);
	}

	public void testQueriesRemoteFetcherForNewUri() throws Exception {
		
		checking(new Expectations() {{ 
			one(resources).findByUri(URI); will(returnValue(null));
			one(remoteFetcher).fetch(URI); will(returnValue(bean));
		}});
		
		localOrRemoteFetcher.fetch(URI);
	}
	
	public void testLoadsKnownResourcesFromDatabaseAndDoesNotFetch() throws Exception {
		
		checking(new Expectations() {{ 
			one(resources).findByUri(URI); will(returnValue(bean));
		}});
		
		localOrRemoteFetcher.fetch(URI);
	}
	
}
