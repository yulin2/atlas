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

package org.uriplay.query.content.fuzzy;

import java.util.List;

import org.uriplay.content.criteria.BooleanAttributeQuery;
import org.uriplay.content.criteria.ConjunctiveQuery;
import org.uriplay.content.criteria.ContentQuery;
import org.uriplay.content.criteria.DateTimeAttributeQuery;
import org.uriplay.content.criteria.EnumAttributeQuery;
import org.uriplay.content.criteria.IntegerAttributeQuery;
import org.uriplay.content.criteria.MatchesNothing;
import org.uriplay.content.criteria.Queries;
import org.uriplay.content.criteria.QueryVisitor;
import org.uriplay.content.criteria.StringAttributeQuery;
import org.uriplay.content.criteria.attribute.Attributes;
import org.uriplay.content.criteria.operator.Operators;
import org.uriplay.media.entity.Brand;
import org.uriplay.media.entity.Item;
import org.uriplay.media.entity.Playlist;
import org.uriplay.persistence.content.query.KnownTypeQueryExecutor;

import com.google.common.collect.Lists;

public class DefuzzingQueryExecutor implements KnownTypeQueryExecutor {

	private final KnownTypeQueryExecutor delegate;
	private final FuzzySearcher fuzzySearcher;

	public DefuzzingQueryExecutor(KnownTypeQueryExecutor delegate, FuzzySearcher fuzzySearcher) {
		this.delegate = delegate;
		this.fuzzySearcher = fuzzySearcher;
	}
	
	public List<Brand> executeBrandQuery(ContentQuery query) {
		return delegate.executeBrandQuery(defuzz(query));
	}

	public List<Item> executeItemQuery(ContentQuery query) {
		return delegate.executeItemQuery(defuzz(query));
	}

	public List<Playlist> executePlaylistQuery(ContentQuery query) {
		return delegate.executePlaylistQuery(defuzz(query));
	}

	private ContentQuery defuzz(ContentQuery query) {
		return query.accept(new TitleDefuzzingQueryVisitor());
	}
	
	private class TitleDefuzzingQueryVisitor implements QueryVisitor<ContentQuery> {
		
		@SuppressWarnings("unchecked")
		@Override
		public ContentQuery visit(StringAttributeQuery query) {
			if(!Operators.SEARCH.equals(query.getOperator())) {
				return query;
			}
			
			List<String> titleSearches = (List<String>) query.getValue();

			if (Attributes.BRAND_TITLE.equals(query.getAttribute())) {
				List<String> uris = Lists.newArrayList();
				
				for (String title : titleSearches) {
					uris.addAll(fuzzySearcher.brandTitleSearch(title));
				}
				
				if (uris.isEmpty()) {
					return MatchesNothing.get();
				}
				return Queries.equalTo(Attributes.BRAND_URI, uris);
			}
			if (Attributes.ITEM_TITLE.equals(query.getAttribute())) {
				List<String> uris = Lists.newArrayList();
				
				for (String title : titleSearches) {
					uris.addAll(fuzzySearcher.itemTitleSearch(title));
				}
				if (uris.isEmpty()) {
					return MatchesNothing.get();
				}
				return Queries.equalTo(Attributes.ITEM_URI, uris);
			}
			return query;
		}
		
		@Override
		public ContentQuery visit(IntegerAttributeQuery query) {
			return query;
		}

		@Override
		public ContentQuery visit(BooleanAttributeQuery query) {
			return query;
		}

		@Override
		public ContentQuery visit(EnumAttributeQuery<?> query) {
			return query;
		}

		@Override
		public ContentQuery visit(DateTimeAttributeQuery query) {
			return query;
		}

		@Override
		public ContentQuery visit(ConjunctiveQuery query) {
			List<ContentQuery> newConjucts = Lists.newArrayList();
			for (ContentQuery operand : query.operands()) {
				newConjucts.add(defuzz(operand));
			}
			return query.copyWithOperands(newConjucts);
		}

		public ContentQuery visit(MatchesNothing noOp) {
			return noOp;
		}
	}
}
