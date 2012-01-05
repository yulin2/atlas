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

import junit.framework.TestCase;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.system.Fetcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Robert Chatley (robert@metabroadcast.com)
 */
@SuppressWarnings("unchecked")
@RunWith(JMock.class)
public class SavingFetcherTest extends TestCase {

    private final Mockery context = new Mockery();
	Fetcher<Identified> delegateFetcher = context.mock(Fetcher.class);
	ContentWriter store = context.mock(ContentWriter.class);
	
	String URI = "http://example.com";
	Item item1 = new Item();
	Brand brand = new Brand();

    @Test
	public void testFetchesItemsFromDelegateAndSavesToStore() throws Exception {
		
		context.checking(new Expectations() {{ 
			one(delegateFetcher).fetch(URI); will(returnValue(item1));
			one(store).createOrUpdate(item1);
		}});
		
		new SavingFetcher(delegateFetcher, store).fetch(URI);
	}
	
}
