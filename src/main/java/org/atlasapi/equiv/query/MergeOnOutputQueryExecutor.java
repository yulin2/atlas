package org.atlasapi.equiv.query;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.atlasapi.content.criteria.AtomicQuery;
import org.atlasapi.content.criteria.AttributeQuery;
import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.content.criteria.attribute.Attributes;
import org.atlasapi.content.criteria.operator.Operators;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Clip;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Description;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Playlist;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.mongo.QuerySplitter;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.query.Selection;

public class MergeOnOutputQueryExecutor implements KnownTypeQueryExecutor {

	private static final Ordering<Episode> SERIES_ORDER = Ordering.from(new SeriesOrder());
	
	private final QuerySplitter splitter = new QuerySplitter();
	
	private final KnownTypeQueryExecutor delegate;

	public MergeOnOutputQueryExecutor(KnownTypeQueryExecutor delegate) {
		this.delegate = delegate;
	}
	
	@Override
	public List<Brand> executeBrandQuery(ContentQuery query) {
		
		List<Brand> brands = delegate.executeBrandQuery(query);
		
		if (!query.getConfiguration().precedenceEnabled()) {
			return brands;
		}
		
		Iterable<AtomicQuery> nonBrandAttributes =  nonBrandAttributes(query);
		
		List<Brand> merged = mergeDuplicates(query, brands, BRAND_MERGER);
		
		for (Brand brand : merged) {
			if (brand.getEquivalentTo().isEmpty()) {
				continue;
			}
			AttributeQuery<String> uriEquals = Attributes.BRAND_URI.createQuery(Operators.EQUALS, brand.getEquivalentTo());
			ContentQuery findEquivalent = query.copyWithOperands(Iterables.concat(ImmutableList.of(uriEquals), nonBrandAttributes)).copyWithSelection(Selection.ALL);

			List<Brand> equivalentBrands = Lists.newArrayList(delegate.executeBrandQuery(findEquivalent));
			equivalentBrands.remove(brand);
			
			sortByPrefs(query, equivalentBrands);
			
			mergeIn(brand, equivalentBrands);
		}
		return merged;
	}

	private <T extends Content> List<T> mergeDuplicates(ContentQuery query, List<T> brands, Merger<T> merger) {
		List<T> merged = Lists.newArrayListWithCapacity(brands.size());
		Set<T> processed = Sets.newHashSet();
		
		for (T brand : brands) {
			if (processed.contains(brand)) {
				continue;
			}
			List<T> same = findSame(brand, brands);
			processed.addAll(same);
			sortByPrefs(query, same);
			T chosen = same.get(0);
			merger.merge(chosen, same.subList(1, same.size()));
			merged.add(chosen);
		}
		return merged;
	}
	
	private static interface Merger<T extends Content> {
		
		void merge(T chosen, List<T> notChosen);
		
	}
	
	@SuppressWarnings("unchecked")
	private <T extends Content> List<T> findSame(T brand, Iterable<T> contents) {
		List<T> same = Lists.newArrayList(brand);
		for (T possiblyEquivalent : contents) {
			if (!brand.equals(possiblyEquivalent) && possiblyEquivalent.isEquivalentTo(brand)) {
				same.add(possiblyEquivalent);
			}
		}
		return same;
	}

	private void sortByPrefs(ContentQuery query, List<? extends Content> equivalentBrands) {
		final Ordering<Publisher> byPublisher = query.getConfiguration().publisherPrecedenceOrdering();
		Collections.sort(equivalentBrands, new Comparator<Content>() {
			@Override
			public int compare(Content o1, Content o2) {
				return byPublisher.compare(o1.getPublisher(), o2.getPublisher());
			}
		});
	}

	private static final Merger<Item> ITEM_MERGER = new Merger<Item>() {
		
		@Override
		public void merge(Item chosen, List<Item> notChosen) {
			for (Item notChosenItem : notChosen) {
				for (Clip clip : notChosenItem.getClips()) {
					chosen.addClip(clip);
				}
			}
		}
	};
	
	private static final Merger<Brand> BRAND_MERGER = new Merger<Brand>() {
		
		@Override
		public void merge(Brand chosen, List<Brand> notChosen) {
			// no op, we don't merge brand attributes
		}
	};
	@Override
	public List<Item> executeItemQuery(ContentQuery query) {
		List<Item> items = delegate.executeItemQuery(query);
		
		if (!query.getConfiguration().precedenceEnabled()) {
			return items;
		}
		return mergeDuplicates(query, items, ITEM_MERGER);
	}

	@Override
	public List<Playlist> executePlaylistQuery(ContentQuery query) {
		return delegate.executePlaylistQuery(query);
	}
	
	enum ItemIdStrategy {
		SERIES_EPISODE_NUMBER {
			@Override
			public Predicate<Item> match() {
				return new Predicate<Item>() {
					@Override
					public boolean apply(Item item) {
						if (item instanceof Episode) {
							Episode episode = (Episode) item;
							return episode.getSeriesNumber() != null && episode.getEpisodeNumber() != null;
						}
						return false;
					}
				};
			}

			@Override
			public Iterable<? extends Item> merge(List<Item> items, List<Item> matches) {
				Map<SeriesAndEpisodeNumber, Episode> chosenItemLookup = Maps.newHashMap();
				for (Item item : Iterables.concat(items, matches)) {
					Episode episode = (Episode) item;
					SeriesAndEpisodeNumber se = new SeriesAndEpisodeNumber(episode);
					if (!chosenItemLookup.containsKey(se)) {
						chosenItemLookup.put(se, episode);
					} else {
						Item chosen = chosenItemLookup.get(se);
						for (Clip clip : item.getClips()) {
							chosen.addClip(clip);
						}
					}
				}
				return SERIES_ORDER.immutableSortedCopy(chosenItemLookup.values());
			}
		};
		

		protected abstract Predicate<Item> match();
		
		static ItemIdStrategy findBest(Iterable<Item> items) {
			if (Iterables.all(items, ItemIdStrategy.SERIES_EPISODE_NUMBER.match())) {
				return SERIES_EPISODE_NUMBER;
			}
			return null;
		}
		
		public abstract Iterable<? extends Item> merge(List<Item> items, List<Item> matches);
	}
	
	private void mergeIn(Brand brand, List<Brand> equivalentBrands) {
		List<Item> items = findItemsSuitableForMerging(brand, equivalentBrands);
		if (items.isEmpty()) {
			// nothing to merge
			return;
		}
		ItemIdStrategy strategy = ItemIdStrategy.findBest(items);
		
		if (strategy == null) {
			return;
		}
		List<Item> matches = Lists.newArrayList();
		for (Brand equivalent : equivalentBrands) {
			if (strategy.equals(ItemIdStrategy.findBest(equivalent.getItems()))) {
				matches.addAll(matches);
			}
		}
		brand.setItems(strategy.merge(items, matches));
	}

	private List<Item> findItemsSuitableForMerging(Brand brand, List<Brand> equivalentBrands) {
		List<Item> items = brand.getItems();
		if (items.isEmpty()) {
			for (Brand equivalent : equivalentBrands) {
				if (!equivalent.getItems().isEmpty()) {
					if (ItemIdStrategy.findBest(equivalent.getItems()) != null) {
						return equivalent.getItems();
					}
				}
			}
		}
		return items;
	}

	private Iterable<AtomicQuery> nonBrandAttributes(ContentQuery query) {
		Maybe<ContentQuery> nonBrandAttributes = splitter.discard(query, ImmutableSet.<Class<? extends Description>>of(Brand.class));
		
		if (nonBrandAttributes.hasValue()) {
			return nonBrandAttributes.requireValue().operands();
		}
		return ImmutableList.of();
	}
}
