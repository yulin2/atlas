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

package org.atlasapi.query.content.sql;

import static org.atlasapi.content.criteria.ContentQueryBuilder.query;
import static org.atlasapi.content.criteria.attribute.Attributes.BROADCAST_TRANSMISSION_TIME;
import static org.atlasapi.content.criteria.attribute.Attributes.DESCRIPTION_TITLE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Set;

import junit.framework.TestCase;

import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Content;
import org.joda.time.DateTime;

import com.google.common.collect.Sets;

@SuppressWarnings("unchecked")
public class AttributeTargetTypeExtractorTest extends TestCase {

	AttributeTargetTypeExtractor extractor = new AttributeTargetTypeExtractor();

	public void testExtractingItemAttribute() throws Exception {
		ContentQuery query = query().beginning(DESCRIPTION_TITLE, "bob").build();
		assertThat(extractor.extract(query), is((Set) Sets.newHashSet(Content.class)));
	}
	
	public void testCompositeQuery() {
		ContentQuery query = query().beginning(DESCRIPTION_TITLE, "bob").after(BROADCAST_TRANSMISSION_TIME, new DateTime()).build();
		assertThat(extractor.extract(query), is((Set) Sets.newHashSet(Content.class, Broadcast.class)));
	}
}
