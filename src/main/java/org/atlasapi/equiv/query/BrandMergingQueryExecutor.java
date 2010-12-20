package org.atlasapi.equiv.query;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.atlasapi.content.criteria.AtomicQuery;
import org.atlasapi.content.criteria.AttributeQuery;
import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.content.criteria.attribute.Attributes;
import org.atlasapi.content.criteria.operator.Operators;
import org.atlasapi.media.entity.Brand;
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
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.query.Selection;

public class BrandMergingQueryExecutor implements KnownTypeQueryExecutor {

	private final QuerySplitter splitter = new QuerySplitter();
	
	private final KnownTypeQueryExecutor delegate;

	public BrandMergingQueryExecutor(KnownTypeQueryExecutor delegate) {
		this.delegate = delegate;
	}
	
	@Override
	public List<Brand> executeBrandQuery(ContentQuery query) {
		
		List<Brand> brands = delegate.executeBrandQuery(query);
		
		if (!query.getConfiguration().precedenceEnabled()) {
			return brands;
		}
		
		Iterable<AtomicQuery> nonBrandAttributes =  nonBrandAttributes(query);
		
		List<Brand> merged = mergeDuplicateBrands(query, brands);
		
		for (Brand brand : merged) {
			if (brand.getEquivalentTo().isEmpty()) {
				continue;
			}
			AttributeQuery<String> uriEquals = Attributes.BRAND_URI.createQuery(Operators.EQUALS, brand.getEquivalentTo());
			ContentQuery findEquivalent = query.copyWithOperands(Iterables.concat(ImmutableList.of(uriEquals), nonBrandAttributes)).copyWithSelection(Selection.ALL);

			List<Brand> equivalentBrands = delegate.executeBrandQuery(findEquivalent);
			equivalentBrands.remove(brand);
			
			sortByPrefs(query, equivalentBrands);
			
			mergeIn(brand, equivalentBrands);
		}
		return merged;
	}

	private List<Brand> mergeDuplicateBrands(ContentQuery query, List<Brand> brands) {
		List<Brand> merged = Lists.newArrayListWithCapacity(brands.size());
		Set<Brand> processed = Sets.newHashSet();
		
		for (Brand brand : brands) {
			if (processed.contains(brand)) {
				continue;
			}
			List<Brand> same = findSame(brand, Sets.difference(ImmutableSet.copyOf(brands), processed));
			processed.addAll(same);
			sortByPrefs(query, same);
			merged.add(same.get(0));
		}
		return merged;
	}

	private List<Brand> findSame(Brand brand, Set<Brand> brands) {
		List<Brand> same = Lists.newArrayList(brand);
		for (Brand possiblyEquivalent : brands) {
			if (!brand.equals(possiblyEquivalent) && possiblyEquivalent.isEquivalentTo(brand)) {
				same.add(possiblyEquivalent);
			}
		}
		return same;
	}

	private void sortByPrefs(ContentQuery query, List<Brand> equivalentBrands) {
		final Ordering<Publisher> byPublisher = query.getConfiguration().publisherPrecedenceOrdering();
		Collections.sort(equivalentBrands, new Comparator<Brand>() {
			@Override
			public int compare(Brand o1, Brand o2) {
				return byPublisher.compare(o1.getPublisher(), o2.getPublisher());
			}
		});
	}

	@Override
	public List<Item> executeItemQuery(ContentQuery query) {
		return delegate.executeItemQuery(query);
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
			public Iterable<Item> merge(List<Item> items, List<Item> matches) {
				Set<SeriesAndEpisodeNumber> seen = Sets.newHashSet();
				List<Item> merged = Lists.newArrayList();
				for (Item item : Iterables.concat(items, matches)) {
					SeriesAndEpisodeNumber se = new SeriesAndEpisodeNumber((Episode) item);
					if (!seen.contains(se)) {
						seen.add(se);
						merged.add(item);
					}
				}
				return merged;
			}
		};
		
		protected abstract Predicate<Item> match();
		
		static ItemIdStrategy findBest(Iterable<Item> items) {
			if (Iterables.all(items, ItemIdStrategy.SERIES_EPISODE_NUMBER.match())) {
				return SERIES_EPISODE_NUMBER;
			}
			return null;
		}
		
		public abstract Iterable<Item> merge(List<Item> items, List<Item> matches);
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
		List<Item> merged = Lists.newArrayList(strategy.merge(items, matches));
		brand.setItems(merged);
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
