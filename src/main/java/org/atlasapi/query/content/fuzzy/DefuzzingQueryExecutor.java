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

package org.atlasapi.query.content.fuzzy;

import java.util.List;

import org.atlasapi.content.criteria.AtomicQuery;
import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.content.criteria.MatchesNothing;
import org.atlasapi.content.criteria.QueryVisitorAdapter;
import org.atlasapi.content.criteria.StringAttributeQuery;
import org.atlasapi.content.criteria.attribute.Attributes;
import org.atlasapi.content.criteria.operator.Operators;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Playlist;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
import org.atlasapi.query.content.DelegateQueryExecutor;

import com.google.common.collect.Lists;
import com.metabroadcast.common.query.Selection;

public class DefuzzingQueryExecutor implements KnownTypeQueryExecutor {

	private static final int BATCH_SIZE = 1000;
	
	private final KnownTypeQueryExecutor nonFuzzyQueryDelegate;
	private final KnownTypeQueryExecutor fuzzyQueryDelegate;
	private final FuzzySearcher fuzzySearcher;

	public DefuzzingQueryExecutor(KnownTypeQueryExecutor nonFuzzyQueryDelegate, KnownTypeQueryExecutor fuzzyQueryDelegate, FuzzySearcher fuzzySearcher) {
		this.nonFuzzyQueryDelegate = nonFuzzyQueryDelegate;
		this.fuzzyQueryDelegate = fuzzyQueryDelegate;
		this.fuzzySearcher = fuzzySearcher;
	}
	
	public List<Brand> executeBrandQuery(ContentQuery query) {
		return executeContentQuery(query, new DelegateQueryExecutor<Brand>() {

			@Override
			public List<Brand> executeQuery(KnownTypeQueryExecutor executor, ContentQuery query) {
				return executor.executeBrandQuery(query);
			}
		});
	}
	
	public List<Item> executeItemQuery(ContentQuery query) {
		return executeContentQuery(query, new DelegateQueryExecutor<Item>() {

			@Override
			public List<Item> executeQuery(KnownTypeQueryExecutor executor, ContentQuery query) {
				return executor.executeItemQuery(query);
			}
		});
	}

	public <T extends Content> List<T> executeContentQuery(ContentQuery query, DelegateQueryExecutor<T> exec) {
		if (!isFuzzy(query)) {
			return exec.executeQuery(nonFuzzyQueryDelegate, query);
		}
		return query.getSelection().applyTo(loadAll(query, exec));
	}

	private <T extends Content> List<T> loadAll(ContentQuery query, DelegateQueryExecutor<T> exec) {
		Selection originalSelection = query.getSelection();
		int numberToLoad = originalSelection.getOffset() + originalSelection.limitOrDefaultValue(BATCH_SIZE);
		
		List<T> all = Lists.newArrayList();
		
		for (int offset = 0; all.size() < numberToLoad; offset+= BATCH_SIZE) {
			
			ContentQuery queryForThisBatch = defuzz(query.copyWithSelection(new Selection(offset, BATCH_SIZE)));
			
			if (MatchesNothing.isEquivalentTo(queryForThisBatch)) {
				break;
			}
		
			all.addAll(exec.executeQuery(fuzzyQueryDelegate, queryForThisBatch));
		}
		
		return all;
	}

	public List<Playlist> executePlaylistQuery(ContentQuery query) {
		if (isFuzzy(query)) {
			// Fuzzy playlist queries not supported
			return Lists.newArrayList();
		}
		return nonFuzzyQueryDelegate.executePlaylistQuery(query);
	}
	
	private ContentQuery defuzz(ContentQuery query) {
		return query.copyWithOperands(query.accept(new TitleDefuzzingQueryVisitor(query.getSelection())));
	}
	
	private class TitleDefuzzingQueryVisitor extends QueryVisitorAdapter<AtomicQuery> {
		
		private final Selection selection;

		public TitleDefuzzingQueryVisitor(Selection selection) {
			this.selection = selection;
		}

		@SuppressWarnings("unchecked")
		@Override
		public AtomicQuery visit(StringAttributeQuery query) {
			if(!Operators.SEARCH.equals(query.getOperator())) {
				return query;
			}
			
			List<String> titleSearches = (List<String>) query.getValue();

			if (Attributes.BRAND_TITLE.equals(query.getAttribute())) {
				
				List<String> uris = Lists.newArrayList();
				
				for (String title : titleSearches) {
					uris.addAll(fuzzySearcher.brandTitleSearch(title, selection));
				}
				if (uris.isEmpty()) {
					return MatchesNothing.get();
				}
				return Attributes.BRAND_URI.createQuery(Operators.EQUALS, uris);
			}
			if (Attributes.ITEM_TITLE.equals(query.getAttribute())) {

				List<String> uris = Lists.newArrayList();
				
				for (String title : titleSearches) {
					uris.addAll(fuzzySearcher.itemTitleSearch(title, selection));
				}
				if (uris.isEmpty()) {
					return MatchesNothing.get();
				}
				return Attributes.ITEM_URI.createQuery(Operators.EQUALS, uris);
			}
			return query;
		}
		
		@Override
		protected AtomicQuery defaultValue(AtomicQuery query) {
			return query;
		}
	}
	
	private static boolean isFuzzy(ContentQuery query) {
		return query.accept(new IsFuzzyVisitor()).contains(Boolean.TRUE);
	}
	
	private static class IsFuzzyVisitor extends QueryVisitorAdapter<Boolean> {

		@Override
		public Boolean visit(StringAttributeQuery query) {
			return Operators.SEARCH.equals(query.getOperator());
			
		}
		
		@Override
		protected Boolean defaultValue(AtomicQuery query) {
			return false;
		}
	}
}
