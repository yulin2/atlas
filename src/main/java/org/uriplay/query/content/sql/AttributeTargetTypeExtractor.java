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

package org.uriplay.query.content.sql;

import java.util.Collections;
import java.util.Set;

import org.uriplay.content.criteria.AttributeQuery;
import org.uriplay.content.criteria.BooleanAttributeQuery;
import org.uriplay.content.criteria.ConjunctiveQuery;
import org.uriplay.content.criteria.ContentQuery;
import org.uriplay.content.criteria.DateTimeAttributeQuery;
import org.uriplay.content.criteria.EnumAttributeQuery;
import org.uriplay.content.criteria.IntegerAttributeQuery;
import org.uriplay.content.criteria.LogicalOperatorQuery;
import org.uriplay.content.criteria.MatchesNothing;
import org.uriplay.content.criteria.QueryVisitor;
import org.uriplay.content.criteria.StringAttributeQuery;
import org.uriplay.media.entity.Description;

import com.google.common.collect.Sets;

public class AttributeTargetTypeExtractor {

	public Set<Class<? extends Description>> extract(ContentQuery query) {
		
		return query.accept(new QueryVisitor<Set<Class<? extends Description>>>() {

			@Override
			public Set<Class<? extends Description>> visit(IntegerAttributeQuery query) {
				return type(query);
			}

			@Override
			public Set<Class<? extends Description>> visit(StringAttributeQuery query) {
				return type(query);
			}

			@Override
			public Set<Class<? extends Description>> visit(EnumAttributeQuery<?> query) {
				return type(query);
			}

			@Override
			public Set<Class<? extends Description>> visit(DateTimeAttributeQuery query) {
				return type(query);
			}

			@Override
			public Set<Class<? extends Description>> visit(BooleanAttributeQuery query) {
				return type(query);
			}
			
			@Override
			public Set<Class<? extends Description>> visit(MatchesNothing noOp) {
				return Collections.emptySet();
			}
			
			@Override
			public Set<Class<? extends Description>> visit(ConjunctiveQuery conjunctiveQuery) {
				return visitJunction(conjunctiveQuery);
			}

			private Set<Class<? extends Description>> visitJunction(LogicalOperatorQuery query) {
				Set<Class<? extends Description>> merged = Sets.newHashSet();
				for (ContentQuery operand : query.operands()) {
					merged.addAll(extract(operand));
				}
				return merged;
			}


			private Set<Class<? extends Description>> type(AttributeQuery<?> query) {
				return Collections.<Class<? extends Description>>singleton(query.getAttribute().target());
			}
		});
	}
}
