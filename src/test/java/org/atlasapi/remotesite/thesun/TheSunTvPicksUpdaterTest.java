package org.atlasapi.remotesite.thesun;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;

import junit.framework.TestCase;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.atlasapi.media.entity.ChildRef;
import org.atlasapi.media.entity.ContentGroup;
import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.content.ContentGroupResolver;
import org.atlasapi.persistence.content.ContentGroupWriter;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.NullAdapterLog;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.persistence.testing.StubContentResolver;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import com.metabroadcast.common.base.Maybe;


public class TheSunTvPicksUpdaterTest extends TestCase {
    protected static final int ContentGroup = 0;
    private static String FEED_URL = "http://www.thesun.co.uk/sol/homepage/feeds/smartphone/newsection/";
    private final Mockery context = new Mockery();
    private final Builder builder = new Builder(new TheSunTvPicksElementFactory());
    private final AdapterLog log = new NullAdapterLog();
    private final ContentWriter contentWriter = context.mock(ContentWriter.class);
    private final ContentResolver resolver = StubContentResolver.RESOLVES_NOTHING; // TODO make this resolve to PA?
    private final ContentGroupResolver contentGroupResolver = context.mock(ContentGroupResolver.class);
    private final ContentGroupWriter contentGroupWriter = context.mock(ContentGroupWriter.class);
    private TheSunTvPicksEntryProcessor entryProcessor;
    private TheSunTvPicksContentGroupUpdater groupUpdater;
    
    @SuppressWarnings("unchecked")
    private final RemoteSiteClient<Document> rssFetcher = context.mock(RemoteSiteClient.class);
    private Document tvPicksFeed;
    private TheSunTvPicksUpdater updater;
    
    @Before
    public void setUp() throws ValidityException, ParsingException, IOException {
        tvPicksFeed = builder.build(new InputStreamReader(Resources.getResource("thesun-tvpicks-2013-07-02.xml").openStream()));
        entryProcessor = new TheSunTvPicksEntryProcessor(contentWriter, resolver, log);
        groupUpdater = new TheSunTvPicksContentGroupUpdater(contentGroupResolver, contentGroupWriter);
        updater = new TheSunTvPicksUpdater(rssFetcher, entryProcessor, groupUpdater, log);
    }
    
    @Test
    public void testExtraction() throws Exception {
        context.checking(new Expectations() {{
            oneOf(rssFetcher).get(FEED_URL);
            will(returnValue(tvPicksFeed));
            will(returnValue(Maybe.<ContentGroup>nothing()));
            allowing(contentWriter).createOrUpdate(with(any(Item.class)));
        }});
        updater.run();
    }
}
