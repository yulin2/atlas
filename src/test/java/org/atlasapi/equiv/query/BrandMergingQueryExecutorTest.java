package org.atlasapi.equiv.query;


import java.util.List;

import junit.framework.TestCase;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.content.criteria.attribute.Attributes;
import org.atlasapi.content.criteria.operator.Operators;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Playlist;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;

import com.google.common.collect.ImmutableList;

public class BrandMergingQueryExecutorTest extends TestCase {
	
	private final Brand brand1 = new Brand("1", "c:1", Publisher.BBC);
	private final Brand brand2 = new Brand("2", "c:2", Publisher.BBC);
	private final Brand brand3 = new Brand("3", "c:3", Publisher.YOUTUBE);
	
	private MergeOnOutputQueryExecutor merger;

	@Override
	protected void setUp() throws Exception {
		brand3.addEquivalentTo(brand1);
		this.merger = new MergeOnOutputQueryExecutor(delegate());
	}
	
	public void testTheCode() throws Exception {
		ContentQuery query = new ContentQuery(Attributes.BRAND_TITLE.createQuery(Operators.EQUALS, ImmutableList.of("test")));
		query = query.copyWithApplicationConfiguration(new ApplicationConfiguration(null, ImmutableList.of(Publisher.YOUTUBE, Publisher.BBC)));
		List<Brand> merged = merger.executeBrandQuery(query);
		assertEquals(ImmutableList.of(brand3, brand2), merged);
	}

	private KnownTypeQueryExecutor delegate() {
		return new KnownTypeQueryExecutor() {
			
			@Override
			public List<Playlist> executePlaylistQuery(ContentQuery query) {
				throw new UnsupportedOperationException();
			}
			
			@Override
			public List<Item> executeItemQuery(ContentQuery query) {
				throw new UnsupportedOperationException();
			}
			
			@Override
			public List<Brand> executeBrandQuery(ContentQuery query) {
				return ImmutableList.of(brand1, brand2, brand3);
			}
		};
	}
}
