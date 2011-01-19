package org.atlasapi.query.content;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.atlasapi.content.criteria.AtomicQuery;
import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.content.criteria.attribute.Attribute;
import org.atlasapi.content.criteria.attribute.Attributes;
import org.atlasapi.content.criteria.operator.Operators;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Schedule;
import org.atlasapi.persistence.content.mongo.QueryConcernsTypeDecider;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class ApplicationConfigurationQueryExecutor implements KnownTypeQueryExecutor {
	
	private final KnownTypeQueryExecutor delegate;

	public ApplicationConfigurationQueryExecutor(KnownTypeQueryExecutor delegate) {
		this.delegate = delegate;
	}		
		
	@Override
	public List<Content> discover(ContentQuery query) {
		return delegate.discover(queryForContent(query));
	}

	@Override
	public List<Identified> executeUriQuery(Iterable<String> uris, ContentQuery query) {
		return delegate.executeUriQuery(uris, queryForContent(query));
	}
	
	private ContentQuery queryForContent(ContentQuery query) {
		Iterable<AtomicQuery> softs = ImmutableList.of((AtomicQuery)
			mergeAttribute(Attributes.VERSION_PROVIDER, query),
			mergeAttribute(Attributes.DESCRIPTION_PUBLISHER,query)
		);
		
		query.setSoftConstraints(softs);

		if(!QueryConcernsTypeDecider.concernsItemOrBelow(query)) {
			Iterable<AtomicQuery> queryAtoms = ImmutableSet.of((AtomicQuery)
				mergeAttribute(Attributes.DESCRIPTION_PUBLISHER, query)
			);
			return ContentQuery.joinTo(query, new ContentQuery(queryAtoms));
		} 
		
		return query;
	}

	private AtomicQuery mergeAttribute(Attribute<?> attr, ContentQuery query){
		Set<Publisher> configPublishers = query.getConfiguration().getIncludedPublishers();
		Map<Attribute<?>, List<?>> operandMap = query.operandMap();
		
		Set<?> values;
		List<?> queryValues = operandMap.get(attr);
		if (queryValues == null) {
			values = configPublishers;
		} else {
			Set<Publisher> pubIntersection = Sets.intersection(configPublishers, ImmutableSet.copyOf(queryValues));
			values = pubIntersection.isEmpty() ? configPublishers : pubIntersection;
		}
		
		return attr.createQuery(Operators.EQUALS, values);
	}

	@Override
	public Schedule schedule(ContentQuery query) {
		throw new UnsupportedOperationException("TODO");
	}
}
