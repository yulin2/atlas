package org.atlasapi.query.content;

import java.util.List;
import java.util.Set;

import org.atlasapi.content.criteria.AtomicQuery;
import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.content.criteria.QueryVisitorAdapter;
import org.atlasapi.content.criteria.StringAttributeQuery;
import org.atlasapi.content.criteria.attribute.Attribute;
import org.atlasapi.content.criteria.attribute.Attributes;
import org.atlasapi.content.criteria.operator.Operators;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Playlist;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.metabroadcast.common.base.Maybe;

public class CurieResolvingQueryExecutor implements KnownTypeQueryExecutor {

	private final KnownTypeQueryExecutor delegate;
	private final CurieExpander curieExpander = new PerPublisherCurieExpander();
	
	public CurieResolvingQueryExecutor(KnownTypeQueryExecutor delegate) {
		this.delegate = delegate;
	}
	
	public List<Item> executeItemQuery(ContentQuery query) {
		return delegate.executeItemQuery(resolve(query));
	}

	public List<Brand> executeBrandQuery(ContentQuery query) {
		return delegate.executeBrandQuery(resolve(query));
	}
	
	public List<Playlist> executePlaylistQuery(ContentQuery query) {
		return delegate.executePlaylistQuery(resolve(query));
	}
	
	private static final Set<Attribute<String>> uriAttributes = ImmutableSet.of(Attributes.ITEM_URI, Attributes.BRAND_URI, Attributes.PLAYLIST_URI);
	
	private ContentQuery resolve(ContentQuery query) {
		
		List<AtomicQuery> conjuncts = query.accept(new QueryVisitorAdapter<AtomicQuery>() {

			@Override
			@SuppressWarnings("unchecked")
			public AtomicQuery visit(StringAttributeQuery query) {

				if (!(uriAttributes.contains(query.getAttribute()) && query.getOperator().equals(Operators.EQUALS))) {
					return defaultValue(query);
				}
				
				List<String> uris = Lists.newArrayList(); 
				for (String value : (List<String>) query.getValue()) {
					Maybe<String> curieExpanded = curieExpander.expand(value);
					if (curieExpanded.hasValue()) {
						uris.add(curieExpanded.requireValue());
					} else {
						uris.add(value);
					}
				}
				return new StringAttributeQuery(query.getAttribute(), query.getOperator(), uris);
			}
			
			@Override
			protected AtomicQuery defaultValue(AtomicQuery query) {
				return query;
			}
		});
		
		return query.copyWithOperands(conjuncts);
	}
}
