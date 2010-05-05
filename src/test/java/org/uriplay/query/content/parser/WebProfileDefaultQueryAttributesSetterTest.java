/* Copyright 2010 Meta Broadcast Ltd

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License. */

package org.uriplay.query.content.parser;

import org.jmock.integration.junit3.MockObjectTestCase;
import org.uriplay.content.criteria.ContentQuery;
import org.uriplay.content.criteria.Queries;
import org.uriplay.content.criteria.attribute.Attributes;

public class WebProfileDefaultQueryAttributesSetterTest extends MockObjectTestCase {

	public void testSomeQueriesThatShoudntBeModified() throws Exception {
		ContentQuery locationAvailable = Queries.equalTo(Attributes.LOCATION_AVAILABLE, true);
		assertEquals(locationAvailable, new WebProfileDefaultQueryAttributesSetter().withDefaults(locationAvailable));
		
		ContentQuery locationAvailableAndTitleEqualTo = Queries.and(Queries.equalTo(Attributes.ITEM_TITLE, "test"), Queries.equalTo(Attributes.LOCATION_AVAILABLE, true));
		assertEquals(locationAvailableAndTitleEqualTo, new WebProfileDefaultQueryAttributesSetter().withDefaults(locationAvailableAndTitleEqualTo));
	}
	
	public void testSomeQueriesThatShoudBeModified() throws Exception {
		ContentQuery titleEquals = Queries.equalTo(Attributes.ITEM_TITLE, "test");
		assertEquals(Queries.and(titleEquals, Queries.equalTo(Attributes.LOCATION_AVAILABLE, true)), new WebProfileDefaultQueryAttributesSetter().withDefaults(titleEquals));
		
		ContentQuery titleEqualsAndDuration = Queries.and(Queries.equalTo(Attributes.ITEM_TITLE, "test"), Queries.lessThan(Attributes.VERSION_DURATION, 10));
		assertEquals(Queries.and(Queries.equalTo(Attributes.ITEM_TITLE, "test"), Queries.lessThan(Attributes.VERSION_DURATION, 10), Queries.equalTo(Attributes.LOCATION_AVAILABLE, true)), new WebProfileDefaultQueryAttributesSetter().withDefaults(titleEqualsAndDuration));
	}
}
