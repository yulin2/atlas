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

import java.util.List;
import java.util.Set;

import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.content.criteria.QueryVisitorAdapter;
import org.atlasapi.content.criteria.StringAttributeQuery;
import org.atlasapi.content.criteria.attribute.Attribute;
import org.atlasapi.content.criteria.attribute.Attributes;
import org.atlasapi.content.criteria.operator.Operators;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.metabroadcast.common.base.Maybe;

/**
 * Visits clauses of a query and extracts and URI parameters.
 * 
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class UriExtractor extends QueryVisitorAdapter<Void> {

	private final Set<String> uris;
	public final static Set<Attribute<String>> URI_ATTRIBUTES = ImmutableSet.of(Attributes.ITEM_URI, Attributes.BRAND_URI, Attributes.PLAYLIST_URI);
	
	private final CurieExpander curieExpander = new PerPublisherCurieExpander();

	private UriExtractor(Set<String> uris) {
		this.uris = uris;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Void visit(StringAttributeQuery query) {

		if (URI_ATTRIBUTES.contains(query.getAttribute()) && query.getOperator().equals(Operators.EQUALS)) {
			for (String value : (List<String>) query.getValue()) {
				Maybe<String> curieExpanded = curieExpander.expand(value);
				if (curieExpanded.hasValue()) {
					uris.add(curieExpanded.requireValue());
				} else {
					uris.add(value);
				}
			}
			
		}
		return null;
	}

	public static Set<String> extractFrom(ContentQuery query) {

		Set<String> uris = Sets.newHashSet();
		query.accept(new UriExtractor(uris));
		return uris;
	}
}
