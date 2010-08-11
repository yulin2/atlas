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

package org.atlasapi.query.content;

import static org.atlasapi.content.criteria.ContentQueryBuilder.query;

import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.content.criteria.attribute.Attributes;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
import org.atlasapi.persistence.system.Fetcher;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;

import com.google.common.collect.ImmutableList;

/**
 * @author Robert Chatley (robert@metabroadcast.com)
 */
@SuppressWarnings("unchecked")
public class UriFetchingQueryExecutorTest extends MockObjectTestCase {

	private static final Item item1 = new Item("item1", "curie:1", Publisher.BBC);
	private static final Item item2 = new Item("item2", "curie:2", Publisher.BBC);
	private static final ContentQuery queryForItem1 = query().equalTo(Attributes.ITEM_URI, "item1").build();

	private Fetcher<Content> fetcher = mock(Fetcher.class);
	private KnownTypeQueryExecutor delegate = mock(KnownTypeQueryExecutor.class);
	
	private UriFetchingQueryExecutor executor = new UriFetchingQueryExecutor(fetcher, delegate);

	public void testThatWhenTheQueryIsSatisfiedByTheDatabaseThatTheFetcherIsNotUsed() throws Exception {
		
		checking(new Expectations() {{ 
			one(delegate).executeItemQuery(queryForItem1); will(returnValue(ImmutableList.of(item1)));
			never(fetcher);
		}});
		
		executor.executeItemQuery(queryForItem1);
	}
	
	public void testThatWhenTheQueryIsNotSatisfiedByTheDatabaseTheFetcherIsUsed() throws Exception {
		
		checking(new Expectations() {{ 
			one(delegate).executeItemQuery(queryForItem1); will(returnValue(ImmutableList.<Item>of()));
			one(fetcher).fetch("item1"); will(returnValue(item1));
			one(delegate).executeItemQuery(queryForItem1); will(returnValue(ImmutableList.of(item1)));

		}});
		
		executor.executeItemQuery(queryForItem1);
	}
	
	public void testThatIfTheFetcherReturnsNothingTheTheDBIsNotRetried() throws Exception {
		
		checking(new Expectations() {{ 
			one(delegate).executeItemQuery(queryForItem1); will(returnValue(ImmutableList.<Item>of()));
			one(fetcher).fetch("item1"); will(returnValue(null));

		}});

		executor.executeItemQuery(queryForItem1);
	}
	
	public void testThatWhenSomeItemsAreInTheDatabaseAndSomeAreNotThatTheFetcherIsUsedOnTheMissingItems() throws Exception {
		final ContentQuery queryForItems1and2 = query().equalTo(Attributes.ITEM_URI, "item1", "item2").build();
		
		checking(new Expectations() {{ 
			one(delegate).executeItemQuery(queryForItems1and2); will(returnValue(ImmutableList.of(item1)));
			one(fetcher).fetch("item2"); will(returnValue(item2));
			one(delegate).executeItemQuery(queryForItems1and2); will(returnValue(ImmutableList.of(item1, item2)));
		}});

		executor.executeItemQuery(queryForItems1and2);
	}
	
	public void testThatItemInBrandQueriesWork() throws Exception {
		
		final Brand brand = new Brand("brand1", "brand:1", Publisher.BBC);
		brand.addItem(item1);
		
		
		checking(new Expectations() {{ 
			one(delegate).executeBrandQuery(queryForItem1); will(returnValue(ImmutableList.of()));
			one(fetcher).fetch("item1"); will(returnValue(item1));
			one(delegate).executeBrandQuery(queryForItem1); will(returnValue(ImmutableList.of(brand)));
		}});
		
		executor.executeBrandQuery(queryForItem1);
		
		checking(new Expectations() {{ 
			one(delegate).executeBrandQuery(queryForItem1); will(returnValue(ImmutableList.of(brand)));
		}});

		executor.executeBrandQuery(queryForItem1);
	}
}
