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
import org.atlasapi.content.criteria.QueryVisitorAdapter;
import org.atlasapi.content.criteria.StringAttributeQuery;
import org.atlasapi.content.criteria.attribute.Attributes;
import org.atlasapi.content.criteria.operator.Operators;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
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

	@Override
	public List<Content> discover(ContentQuery query) {
		if (!isFuzzy(query)) {
			return nonFuzzyQueryDelegate.discover(query);
		}
		return query.getSelection().applyTo(loadAll(query));
	}

	@SuppressWarnings("unchecked")
	private List<Content> loadAll(ContentQuery query) {
		Selection originalSelection = query.getSelection();
		int numberToLoad = originalSelection.getOffset() + originalSelection.limitOrDefaultValue(BATCH_SIZE);
		
		List<Content> all = Lists.newArrayList();
		
		for (int offset = 0; all.size() < numberToLoad; offset+= BATCH_SIZE) {
			
			Iterable<String> urisForThisBatch = defuzz(query.copyWithSelection(new Selection(offset, BATCH_SIZE)));
			
			if (Iterables.isEmpty(urisForThisBatch)) {
				break;
			}
			
			all.addAll((List) fuzzyQueryDelegate.executeUriQuery(urisForThisBatch, query));
		}
		return all;
	}

	public List<Identified> executeUriQuery(Iterable<String> uris, ContentQuery query) {
		if (isFuzzy(query)) {
			// Fuzzy uri queries not supported
			return ImmutableList.of();
		}
		return nonFuzzyQueryDelegate.executeUriQuery(uris, query);
	}
	
	private Iterable<String> defuzz(ContentQuery query) {
		return Iterables.concat(query.accept(new TitleDefuzzingQueryVisitor(query.getSelection(), query.includedPublishers())));
	}
	
	private class TitleDefuzzingQueryVisitor extends QueryVisitorAdapter<List<String>> {

		private final Selection selection;
		private final Iterable<Publisher> publishers;

		public TitleDefuzzingQueryVisitor(Selection selection, Iterable<Publisher> publishers) {
			this.selection = selection;
			this.publishers = publishers;
		}

		@SuppressWarnings("unchecked")
		@Override
		public List<String> visit(StringAttributeQuery query) {
			if(!Operators.SEARCH.equals(query.getOperator())) {
				return defaultValue(query);
			}
			
			List<String> titleSearches = (List<String>) query.getValue();

			if (Attributes.DESCRIPTION_TITLE.equals(query.getAttribute())) {
				List<String> uris = Lists.newArrayList();
				
				for (String title : titleSearches) {
					uris.addAll(fuzzySearcher.contentSearch(title, selection, publishers).toUris());
				}
				return uris;
			}
			return defaultValue(query);
		}
		
		@Override
		protected ImmutableList<String> defaultValue(AtomicQuery query) {
			return ImmutableList.of();
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
