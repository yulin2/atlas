package org.atlasapi.equiv.query;

import java.util.List;

import junit.framework.TestCase;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.content.criteria.attribute.Attributes;
import org.atlasapi.content.criteria.operator.Operators;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Clip;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Playlist;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class MergeOnOutputQueryExecutorTest extends TestCase {
	
	private final Brand brand1 = new Brand("1", "c:1", Publisher.BBC);
	private final Brand brand2 = new Brand("2", "c:2", Publisher.BBC);
	private final Brand brand3 = new Brand("3", "c:3", Publisher.YOUTUBE);

	private final Item item1 = new Item("i1", "c:i1", Publisher.BBC);
	private final Item item2 = new Item("i2", "c:i2", Publisher.YOUTUBE);

	private final Clip clip1 = new Clip("c1", "c:c1", Publisher.YOUTUBE);
	
	private MergeOnOutputQueryExecutor merger;

	@Override
	protected void setUp() throws Exception {
		brand3.addEquivalentTo(brand1);
		item1.addEquivalentTo(item2);
		item2.addClip(clip1);
		this.merger = new MergeOnOutputQueryExecutor(delegate());
	}
	
	public void testMergingBrands() throws Exception {
		ContentQuery query = new ContentQuery(Attributes.BRAND_TITLE.createQuery(Operators.EQUALS, ImmutableList.of("test")));
		query = query.copyWithApplicationConfiguration(new ApplicationConfiguration(null, ImmutableList.of(Publisher.YOUTUBE, Publisher.BBC)));
		List<Brand> merged = merger.executeBrandQuery(query);
		assertEquals(ImmutableList.of(brand3, brand2), merged);
	}
	
	public void testMergingItems() throws Exception {
		ContentQuery query = new ContentQuery(Attributes.ITEM_TITLE.createQuery(Operators.EQUALS, ImmutableList.of("test")));
		query = query.copyWithApplicationConfiguration(new ApplicationConfiguration(null, ImmutableList.of(Publisher.BBC, Publisher.YOUTUBE)));
		List<Item> merged = merger.executeItemQuery(query);
		assertEquals(ImmutableList.of(item1), merged);
		assertEquals(ImmutableList.of(clip1), Iterables.getOnlyElement(merged).getClips());
	}

	private KnownTypeQueryExecutor delegate() {
		return new KnownTypeQueryExecutor() {
			
			@Override
			public List<Playlist> executePlaylistQuery(ContentQuery query) {
				throw new UnsupportedOperationException();
			}
			
			@Override
			public List<Item> executeItemQuery(ContentQuery query) {
				return ImmutableList.of(item1, item2);
			}
			
			@Override
			public List<Brand> executeBrandQuery(ContentQuery query) {
				return ImmutableList.of(brand1, brand2, brand3);
			}
		};
	}
}
