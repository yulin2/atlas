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

package org.uriplay.query.content;

import static org.hamcrest.Matchers.equalTo;


import org.jherd.remotesite.Fetcher;
import org.jherd.remotesite.timing.RequestTimer;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.uriplay.content.criteria.ContentQuery;
import org.uriplay.content.criteria.Queries;
import org.uriplay.content.criteria.attribute.Attributes;
import org.uriplay.media.entity.Description;
import org.uriplay.media.entity.Item;
import org.uriplay.persistence.content.query.KnownTypeQueryExecutor;


/**
 * @author Robert Chatley (robert@metabroadcast.com)
 */
@SuppressWarnings("unchecked")
public class UriFetchingQueryExecutorTest extends MockObjectTestCase {

	ContentQuery uriquery = Queries.equalTo(Attributes.ITEM_URI, "http://example.com");
	ContentQuery titlequery = Queries.equalTo(Attributes.BRAND_TITLE, "ex");
	ContentQuery query = Queries.and(uriquery, titlequery);

	Fetcher<Description> fetcher = mock(Fetcher.class);
	KnownTypeQueryExecutor delegate = mock(KnownTypeQueryExecutor.class);
	
	UriFetchingQueryExecutor executor = new UriFetchingQueryExecutor(fetcher, delegate);

	public void testDelegatesItemQuery() throws Exception {

		final Item item = new Item();
		
		item.setCanonicalUri("http://canonical.com");
		item.addAlias("http://example.com");
		
		checking(new Expectations() {{
			one(fetcher).fetch(with(equalTo("http://example.com")), with(any(RequestTimer.class))); will(returnValue(item));
			one(delegate).executeItemQuery(query);
		}});
			
		executor.executeItemQuery(query);
	}
	
	public void testDelegatesPlaylistQuery() throws Exception {

	final Item item = new Item();
		
		item.setCanonicalUri("http://canonical.com");
		item.addAlias("http://example.com");
		
		checking(new Expectations() {{
			one(fetcher).fetch(with(equalTo("http://example.com")), with(any(RequestTimer.class))); will(returnValue(item));
			one(delegate).executePlaylistQuery(query);
		}});
			
		executor.executePlaylistQuery(query);
		
	}

}
