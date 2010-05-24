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

package org.uriplay.remotesite;

import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.uriplay.beans.Representation;
import org.uriplay.persistence.system.Fetcher;
import org.uriplay.persistence.system.RequestTimer;
import org.uriplay.remotesite.timing.TimedFetcher;

/**
 * Unit test for {@link TimedFetcher}.
 * 
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class TimedFetcherTest extends MockObjectTestCase {

	static final String URI = "http://example.com";
	
	static final Representation REPRESENTATION = null;

	@SuppressWarnings("unchecked")
	public void testRecordsResponseTimeUsingTimer() throws Exception {
		
		final RequestTimer timer = mock(RequestTimer.class);
		final Fetcher<Representation> delegate = mock(Fetcher.class);
		
		final TimedFetcher<Representation> fetcher = new TimedFetcher<Representation>() {
			@Override
			protected Representation fetchInternal(String uri, RequestTimer timer) {
				return delegate.fetch(uri, timer);
			}};
		
		final Sequence ofCall = sequence("in callstack order");
		
		checking(new Expectations() {{
			one(timer).start(fetcher, URI); inSequence(ofCall);
			one(delegate).fetch(URI, timer); inSequence(ofCall); will(returnValue(REPRESENTATION));
			one(timer).stop(fetcher, URI); inSequence(ofCall);
		}});
		
		fetcher.fetch(URI, timer);
	}
}
