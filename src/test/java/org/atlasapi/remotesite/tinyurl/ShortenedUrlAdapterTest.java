package org.atlasapi.remotesite.tinyurl;
///* Copyright 2009 British Broadcasting Corporation
//   Copyright 2009 Meta Broadcast Ltd
//
//Licensed under the Apache License, Version 2.0 (the "License"); you
//may not use this file except in compliance with the License. You may
//obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
//implied. See the License for the specific language governing
//permissions and limitations under the License. */
//
//package org.atlasapi.remotesite.tinyurl;
//
//import static org.hamcrest.MatcherAssert.assertThat;
//import static org.hamcrest.Matchers.is;
//
//import java.util.List;
//
//import org.jherd.beans.Representation;
//import org.jherd.remotesite.Fetcher;
//import org.jherd.remotesite.timing.RequestTimer;
//import org.jherd.util.testing.EnvironmentTest;
//import org.jmock.Expectations;
//import org.jmock.integration.junit3.MockObjectTestCase;
//
//import com.google.common.collect.Lists;
//
///**
// * Test for {@link ShortenedUrlAdapter}.
// * @author Robert Chatley (robert@metabroadcast.com)
// */
//@EnvironmentTest
//public class ShortenedUrlAdapterTest extends MockObjectTestCase {
//	
//	Representation representation;
//	
//	Fetcher fetcher = mock(Fetcher.class);
//	RequestTimer timer = mock(RequestTimer.class);
//	ShortenedUrlAdapter adapter = new ShortenedUrlAdapter(fetcher);
//	
//	@Override
//	protected void setUp() throws Exception {
//		super.setUp();
//		representation = new Representation();
//		adapter.setSupportedDomains(Lists.newArrayList("http://tinyurl.com", "http://bit.ly"));
//	}
//	
//	public void testResolvesTinyUrlRedirectsAndFetchesResultingUrl() throws Exception {
//	
//		checking(new Expectations(){{ 
//			one(fetcher).fetch("http://en.wikipedia.org/wiki/The_Poison_Sky", timer); will(returnValue(representation));
//			ignoring(timer);
//		}});
//		
//		adapter.fetch("http://tinyurl.com/bvnsmh", timer);
//	}
//	
//	public void testCanFetchUrlsFromSupportedDomains() throws Exception {
//		
//		assertTrue(adapter.canFetch("http://tinyurl.com/bvnsmh"));
//		assertTrue(adapter.canFetch("http://bit.ly/bvnsmh"));
//		assertFalse(adapter.canFetch("http://bbc.co.uk"));
//	}
//	
//	public void testAddsSameAsForShortUrl() throws Exception {
//		
//		checking(new Expectations(){{ 
//			allowing(fetcher).fetch("http://en.wikipedia.org/wiki/The_Poison_Sky", timer); will(returnValue(representation));
//			ignoring(timer);
//		}});
//		
//		Representation representation = adapter.fetch("http://tinyurl.com/bvnsmh", timer);
//		
//		assertThat(representation.getSameAs().get("http://en.wikipedia.org/wiki/The_Poison_Sky"), is((List) Lists.newArrayList("http://en.wikipedia.org/wiki/The_Poison_Sky", "http://tinyurl.com/bvnsmh")));
//	}
//	
//}
