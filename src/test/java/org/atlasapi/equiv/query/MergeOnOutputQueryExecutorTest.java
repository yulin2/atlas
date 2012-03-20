package org.atlasapi.equiv.query;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.media.content.Brand;
import org.atlasapi.media.content.Clip;
import org.atlasapi.media.content.Content;
import org.atlasapi.media.content.Episode;
import org.atlasapi.media.content.Identified;
import org.atlasapi.media.content.Publisher;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

public class MergeOnOutputQueryExecutorTest extends TestCase {
	
	private final Brand brand1 = new Brand("1", "c:1", Publisher.BBC);
	private final Brand brand2 = new Brand("2", "c:2", Publisher.BBC);
	private final Brand brand3 = new Brand("3", "c:3", Publisher.YOUTUBE);

	private final Episode item1 = new Episode("i1", "c:i1", Publisher.BBC);
	private final Episode item2 = new Episode("i2", "c:i2", Publisher.YOUTUBE);

	private final Clip clip1 = new Clip("c1", "c:c1", Publisher.YOUTUBE);
	
	@Override
	protected void setUp() throws Exception {
	    item1.setContainer(brand1);
	    item2.setContainer(brand3);
	    
		brand3.addEquivalentTo(brand1);
		item1.addEquivalentTo(item2);
		item2.addClip(clip1);
		
		brand1.setId(1l);
		brand2.setId(2l);
		brand3.setId(3l);
		item1.setId(4l);
		item2.setId(5l);
		clip1.setId(6l);
	}
	
	public void dontTestMergingBrands() throws Exception {
		fail("refactor this test");
//		MergeOnOutputQueryExecutor merger = new MergeOnOutputQueryExecutor(delegate(brand1, brand2, brand3));
//		ContentQuery query = ContentQuery.MATCHES_EVERYTHING.copyWithApplicationConfiguration(new ApplicationConfiguration(null, ImmutableList.of(Publisher.YOUTUBE, Publisher.BBC)));
////		assertEquals(ImmutableList.of(brand3, brand2), merger.discover(query));
//		
//		Map<String, List<Identified>> identified = merger.executeUriQuery(ImmutableList.of("1", "2", "3"), query);
//        assertEquals(ImmutableList.of(brand3, brand2), ImmutableList.copyOf(Iterables.concat(identified.values())));
//        Brand brand = (Brand) Iterables.get(identified.get(brand3.getCanonicalUri()), 0);
//        assertEquals(brand3, brand);
//        Episode item = Iterables.getOnlyElement(brand.getContents());
//        assertEquals(item.getClips(), ImmutableList.of(clip1));
	}
	
	public void testMergingEpisodes() throws Exception {
		MergeOnOutputQueryExecutor merger = new MergeOnOutputQueryExecutor(delegate(item1, item2));

		ContentQuery query = ContentQuery.MATCHES_EVERYTHING.copyWithApplicationConfiguration(ApplicationConfiguration.DEFAULT_CONFIGURATION.copyWithPrecedence(ImmutableList.of(Publisher.BBC, Publisher.YOUTUBE)));
		Map<String, List<Identified>> merged = ImmutableMap.copyOf(merger.executeUriQuery(ImmutableList.of(item1.getCanonicalUri()), query));
		
		assertEquals(ImmutableList.of(item1), merged.get(item1.getCanonicalUri()));
		assertEquals(ImmutableList.of(clip1), ((Episode)Iterables.getOnlyElement(merged.get(item1.getCanonicalUri()))).getClips());
	}

	private KnownTypeQueryExecutor delegate(final Content... respondWith) {
		return new KnownTypeQueryExecutor() {

			@Override
			public Map<String, List<Identified>> executeUriQuery(Iterable<String> uris, ContentQuery query) {
				return ImmutableMap.<String, List<Identified>>of(respondWith[0].getCanonicalUri(), ImmutableList.<Identified>copyOf(respondWith));
			}

            @Override
            public Map<String, List<Identified>> executeIdQuery(Iterable<Long> ids, ContentQuery query) {
                return ImmutableMap.<String, List<Identified>>of(respondWith[0].getCanonicalUri(), ImmutableList.<Identified>copyOf(respondWith));
            }
		};
	}
}
