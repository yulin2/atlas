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

import junit.framework.TestCase;

import org.atlasapi.media.content.ContentResolver;
import org.atlasapi.media.content.Identified;
import org.atlasapi.media.content.Item;
import org.atlasapi.media.content.Publisher;
import org.atlasapi.persistence.system.Fetcher;
import org.atlasapi.persistence.testing.StubContentResolver;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit test for {@link LocalOrRemoteFetcher}.
 * @author Robert Chatley (robert@metabroadcast.com)
 */
@RunWith(JMock.class)
public class LocalOrRemoteFetcherTest extends TestCase {

	static final String URI = "http://example.com";
    
	Fetcher<Identified> remoteFetcher;
	
	Item bean = new Item(URI, URI, Publisher.BBC);

    private final Mockery context = new Mockery();
    
    @Before
	@Override
	@SuppressWarnings("unchecked")
	public void setUp() throws Exception {
		super.setUp();
		remoteFetcher = context.mock(Fetcher.class);
	}

    @Test
	public void testQueriesRemoteFetcherForNewUri() throws Exception {
		ContentResolver resources = StubContentResolver.RESOLVES_NOTHING;
		LocalOrRemoteFetcher localOrRemoteFetcher = new LocalOrRemoteFetcher(resources, remoteFetcher);
		
		context.checking(new Expectations() {{ 
			one(remoteFetcher).fetch(URI); will(returnValue(bean));
		}});
		
		localOrRemoteFetcher.fetch(URI);
	}

    @Test
	public void testLoadsKnownResourcesFromDatabaseAndDoesNotFetch() throws Exception {
		ContentResolver resources = new StubContentResolver().respondTo(bean);
		LocalOrRemoteFetcher localOrRemoteFetcher = new LocalOrRemoteFetcher(resources, remoteFetcher);
		localOrRemoteFetcher.fetch(URI);
	}
	
}
