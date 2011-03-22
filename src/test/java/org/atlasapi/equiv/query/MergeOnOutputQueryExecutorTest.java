package org.atlasapi.equiv.query;

import java.util.List;

import junit.framework.TestCase;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Clip;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
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
	
	@Override
	protected void setUp() throws Exception {
		brand3.addEquivalentTo(brand1);
		item1.addEquivalentTo(item2);
		item2.addClip(clip1);
	}
	
	public void testMergingBrands() throws Exception {
		MergeOnOutputQueryExecutor merger = new MergeOnOutputQueryExecutor(delegate(brand1, brand2, brand3));
		ContentQuery query = ContentQuery.MATCHES_EVERYTHING.copyWithApplicationConfiguration(new ApplicationConfiguration(null, ImmutableList.of(Publisher.YOUTUBE, Publisher.BBC)));
		assertEquals(ImmutableList.of(brand3, brand2), merger.discover(query));
		assertEquals(ImmutableList.of(brand3, brand2), merger.executeUriQuery(ImmutableList.of("1", "2", "3"), query));
	}
	
	public void testMergingItems() throws Exception {
		MergeOnOutputQueryExecutor merger = new MergeOnOutputQueryExecutor(delegate(item1, item2));

		ContentQuery query = ContentQuery.MATCHES_EVERYTHING.copyWithApplicationConfiguration(new ApplicationConfiguration(null, ImmutableList.of(Publisher.BBC, Publisher.YOUTUBE)));
		List<Content> merged = merger.discover(query);
		assertEquals(ImmutableList.of(item1), merged);
		assertEquals(ImmutableList.of(clip1), Iterables.getOnlyElement(merged).getClips());
	}

	private KnownTypeQueryExecutor delegate(final Content... respondWith) {
		return new KnownTypeQueryExecutor() {

			@Override
			public List<Content> discover(ContentQuery query) {
				return ImmutableList.copyOf(respondWith);
			}

			@Override
			public List<Identified> executeUriQuery(Iterable<String> uris, ContentQuery query) {
				return ImmutableList.<Identified>copyOf(respondWith);
			}
		};
	}
}
