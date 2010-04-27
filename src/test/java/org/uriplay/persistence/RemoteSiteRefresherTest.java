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

package org.uriplay.persistence;

import org.jherd.naming.ResourceMapping;
import org.jherd.persistence.BeanStore;
import org.jherd.remotesite.Fetcher;
import org.jmock.integration.junit3.MockObjectTestCase;
  
/**
 * Unit test for Refresher.
 * 
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class RemoteSiteRefresherTest extends MockObjectTestCase {

	static final String URI_1 = "http://example.com/1";
	static final String URI_2 = "http://example.com/2";
	
 	ResourceMapping externalResources = mock(ResourceMapping.class);
  	BeanStore beanStore = mock(BeanStore.class);
  	Fetcher fetcher = mock(Fetcher.class);
  	
//	Refresher refresher = new RemoteSiteRefresher(externalResources, beanStore, fetcher);
  
 	public void testReadsAllExistingObjectsFromDatabase() throws Exception {
  		
//		final Set<Object> listOfNamed = Sets.newHashSet(new TestBean(URI_1), new TestBean(URI_2));
//		
//		checking(new Expectations() {{ 
//			one(beanStore).getAllResources(); will(returnValue(listOfNamed));
//			one(fetcher).fetch(URI_1);
//			one(fetcher).fetch(URI_2);
//		}});
//		
//		refresher.refreshAll();
  	}
	
}
