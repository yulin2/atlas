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

package org.atlasapi.query.content.sql;

import java.util.List;
import java.util.Set;

import org.atlasapi.content.criteria.AttributeQuery;
import org.atlasapi.content.criteria.BooleanAttributeQuery;
import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.content.criteria.DateTimeAttributeQuery;
import org.atlasapi.content.criteria.EnumAttributeQuery;
import org.atlasapi.content.criteria.IntegerAttributeQuery;
import org.atlasapi.content.criteria.MatchesNothing;
import org.atlasapi.content.criteria.QueryVisitor;
import org.atlasapi.content.criteria.StringAttributeQuery;
import org.atlasapi.media.entity.Description;

import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.base.Maybe;

public class AttributeTargetTypeExtractor {

	public Set<Class<? extends Description>> extract(ContentQuery query) {
		 return ImmutableSet.copyOf(Maybe.filterValues(typesFrom(query)));
	}

	private List<Maybe<Class<? extends Description>>> typesFrom(ContentQuery query) {
		
		return query.accept(new QueryVisitor<Maybe<Class<? extends Description>>>() {

			@Override
			public Maybe<Class<? extends Description>> visit(IntegerAttributeQuery query) {
				return type(query);
			}

			@Override
			public Maybe<Class<? extends Description>> visit(StringAttributeQuery query) {
				return type(query);
			}

			@Override
			public Maybe<Class<? extends Description>> visit(EnumAttributeQuery<?> query) {
				return type(query);
			}

			@Override
			public Maybe<Class<? extends Description>> visit(DateTimeAttributeQuery query) {
				return type(query);
			}

			@Override
			public Maybe<Class<? extends Description>> visit(BooleanAttributeQuery query) {
				return type(query);
			}
			
			@Override
			public Maybe<Class<? extends Description>> visit(MatchesNothing noOp) {
				return Maybe.nothing();
			}

			private Maybe<Class<? extends Description>> type(AttributeQuery<?> query) {
				return Maybe.<Class<? extends Description>>just(query.getAttribute().target());
			}
		});
	}
}
