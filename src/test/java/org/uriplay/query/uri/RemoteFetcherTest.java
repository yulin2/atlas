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

import static org.hamcrest.Matchers.equalTo;

import java.util.Collections;
import java.util.Set;

import org.jherd.beans.BeanGraphErrors;
import org.jherd.beans.GraphFactory;
import org.jherd.beans.Representation;
import org.jherd.remotesite.Fetcher;
import org.jherd.remotesite.timing.RequestTimer;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.uriplay.query.uri.RemoteFetcher;

/**
 * Unit test for {@link RemoteFetcher}.
 * 
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class RemoteFetcherTest extends MockObjectTestCase {

	static final String URI = "http://example.com#a";
    static final Representation REPRESENTATION = new Representation();
    
	Fetcher<Representation> delegateFetcher;
	GraphFactory graphFactory;
	RequestTimer timer;
	
	Fetcher<Set<Object>> remoteFetcher;
	
	Set<Object> beans = Collections.emptySet();
	
	@SuppressWarnings("unchecked")
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		delegateFetcher = mock(Fetcher.class);
		graphFactory = mock(GraphFactory.class);
		timer = mock(RequestTimer.class);
		remoteFetcher = new RemoteFetcher(delegateFetcher, graphFactory);
	}

	public void testQueriesRemoteFetcherForNewUriAndStoresInDatabase() throws Exception {
		
		checking(new Expectations() {{ 
			one(delegateFetcher).fetch(URI, timer); will(returnValue(REPRESENTATION));
 			one(graphFactory).createGraph(with(equalTo(REPRESENTATION)), with(any(BeanGraphErrors.class))); will(returnValue(beans));
			ignoring(timer);
		}});
		
		remoteFetcher.fetch(URI, timer);
	}
	
}
