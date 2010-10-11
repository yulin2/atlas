package org.atlasapi.query.content;

import java.util.List;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.content.criteria.AtomicQuery;
import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.content.criteria.attribute.Attributes;
import org.atlasapi.content.criteria.operator.Operators;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Playlist;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class ApplicationConfigurationQueryExecutor implements
		KnownTypeQueryExecutor {
	
	private final KnownTypeQueryExecutor delegate;

	public ApplicationConfigurationQueryExecutor(KnownTypeQueryExecutor delegate) {
		this.delegate = delegate;
	}		
		
	@Override
	public List<Item> executeItemQuery(ContentQuery query) {
		return delegate.executeItemQuery(ContentQuery.joinTo(query, queryForConfiguration(query.getConfiguration())));
	}

	@Override
	public List<Playlist> executePlaylistQuery(ContentQuery query) {
		return delegate.executePlaylistQuery(ContentQuery.joinTo(query, queryForConfiguration(query.getConfiguration())));
	}

	@Override
	public List<Brand> executeBrandQuery(ContentQuery query) {
		return delegate.executeBrandQuery(ContentQuery.joinTo(query, queryForConfiguration(query.getConfiguration())));
	}

	private ContentQuery queryForConfiguration(ApplicationConfiguration configuration) {
		Iterable<String> publisherKeys = Iterables.transform(configuration.getIncludedPublishers(), Publisher.TO_KEY);
		Iterable<AtomicQuery> queryAtoms = ImmutableSet.of((AtomicQuery)
			Attributes.BRAND_PUBLISHER.createQuery(Operators.EQUALS, publisherKeys),
			Attributes.ITEM_PUBLISHER.createQuery(Operators.EQUALS, publisherKeys)
		);
		return new ContentQuery(queryAtoms);
	}
}
