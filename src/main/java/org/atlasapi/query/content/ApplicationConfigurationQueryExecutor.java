package org.atlasapi.query.content;

import java.util.List;

import org.atlasapi.content.criteria.AtomicQuery;
import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.content.criteria.attribute.Attributes;
import org.atlasapi.content.criteria.operator.Operators;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Playlist;
import org.atlasapi.persistence.content.mongo.QueryConcernsTypeDecider;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public class ApplicationConfigurationQueryExecutor implements
		KnownTypeQueryExecutor {
	
	private final KnownTypeQueryExecutor delegate;

	public ApplicationConfigurationQueryExecutor(KnownTypeQueryExecutor delegate) {
		this.delegate = delegate;
	}		
		
	@Override
	public List<Item> executeItemQuery(ContentQuery query) {
		return delegate.executeItemQuery(queryForItems(query));
	}

	@Override
	public List<Playlist> executePlaylistQuery(ContentQuery query) {
		return delegate.executePlaylistQuery(queryForPlaylists(query));
	}

	@Override
	public List<Brand> executeBrandQuery(ContentQuery query) {
		return delegate.executeBrandQuery(queryForBrands(query));
	}

	private ContentQuery queryForItems(ContentQuery query) {
		AtomicQuery atom = Attributes.VERSION_PROVIDER.createQuery(Operators.EQUALS, query.getConfiguration().getIncludedPublishers());
		if (QueryConcernsTypeDecider.concernsVersionOrBelow(query)) {
			query = ContentQuery.joinTo(query, new ContentQuery(atom));
		} else {
			query.setSoftConstraints(ImmutableSet.of(atom));
		}
		
		Iterable<AtomicQuery> queryAtoms = ImmutableSet.of((AtomicQuery)
			Attributes.ITEM_PUBLISHER.createQuery(Operators.EQUALS, query.getConfiguration().getIncludedPublishers())
		);
		return ContentQuery.joinTo(query, new ContentQuery(queryAtoms));
	}
	
	private ContentQuery queryForBrands(ContentQuery query) {
		Iterable<AtomicQuery> softs = ImmutableList.of((AtomicQuery)
			 Attributes.VERSION_PROVIDER.createQuery(Operators.EQUALS, query.getConfiguration().getIncludedPublishers()),
			 Attributes.ITEM_PUBLISHER.createQuery(Operators.EQUALS, query.getConfiguration().getIncludedPublishers())
		);
		if (QueryConcernsTypeDecider.concernsItemOrBelow(query)) {
			query = ContentQuery.joinTo(query, new ContentQuery(softs));
		} else {
			query.setSoftConstraints(softs);
		}
		
		Iterable<AtomicQuery> queryAtoms = ImmutableSet.of((AtomicQuery)
			Attributes.BRAND_PUBLISHER.createQuery(Operators.EQUALS, query.getConfiguration().getIncludedPublishers())
		);
		return ContentQuery.joinTo(query, new ContentQuery(queryAtoms));
	}
	
	private ContentQuery queryForPlaylists(ContentQuery query) {
		Iterable<AtomicQuery> softs = ImmutableList.of((AtomicQuery)
			Attributes.VERSION_PROVIDER.createQuery(Operators.EQUALS, query.getConfiguration().getIncludedPublishers()),
			Attributes.ITEM_PUBLISHER.createQuery(Operators.EQUALS, query.getConfiguration().getIncludedPublishers()),
			Attributes.BRAND_PUBLISHER.createQuery(Operators.EQUALS, query.getConfiguration().getIncludedPublishers())
		);
		if (QueryConcernsTypeDecider.concernsBrandOrBelow(query)) {
			query = ContentQuery.joinTo(query, new ContentQuery(softs));
		} else {
			query.setSoftConstraints(softs);
		}
		
		Iterable<AtomicQuery> queryAtoms = ImmutableSet.of((AtomicQuery)
			Attributes.PLAYLIST_PUBLISHER.createQuery(Operators.EQUALS, query.getConfiguration().getIncludedPublishers())
		);
		return ContentQuery.joinTo(query, new ContentQuery(queryAtoms));
	}
}
