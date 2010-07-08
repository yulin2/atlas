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

package org.atlasapi.query.uri;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.system.Fetcher;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;

/**
 * @author Robert Chatley (robert@metabroadcast.com)
 */
@SuppressWarnings("unchecked")
public class SavingFetcherTest extends MockObjectTestCase {

	Fetcher<Content> delegateFetcher = mock(Fetcher.class);
	ContentWriter store = mock(ContentWriter.class);
	
	String URI = "http://example.com";
	Item item1 = new Item();
	Brand brand = new Brand();
	
	public void testFetchesItemsFromDelegateAndSavesToStore() throws Exception {
		
		checking(new Expectations() {{ 
			one(delegateFetcher).fetch(URI); will(returnValue(item1));
			one(store).createOrUpdateItem(item1);
		}});
		
		new SavingFetcher(delegateFetcher, store).fetch(URI);
	}
	
}
