package org.atlasapi.remotesite.thesun;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.ValidityException;
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
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;

@RunWith(JMock.class)
public class TheSunTvPicksUpdaterTest {
    protected static final int ContentGroup = 0;
    private static String FEED_URL = "http://www.thesun.co.uk/sol/homepage/feeds/smartphone/newsection/";
    private static String TEST_CONTENT_ID = "http://pressassociation.com/programmes/abc1234";    
    private static String CONTENT_GROUP_URI = "http://www.thesun.co.uk/sol/homepage/feeds/smartphone/newsection/";    
    private final Mockery context = new Mockery();
    private final Builder builder = new Builder(new TheSunTvPicksElementFactory());
    private final AdapterLog log = new NullAdapterLog();
    private final ContentWriter contentWriter = context.mock(ContentWriter.class);
    private final ContentResolver resolver = context.mock(ContentResolver.class);
    private final ContentGroupResolver contentGroupResolver = context.mock(ContentGroupResolver.class);
    private final ContentGroupWriter contentGroupWriter = context.mock(ContentGroupWriter.class);
    private ResolvedContent paResolvedContent;
    private ResolvedContent theSunResolvedContent;
    private ResolvedContent groupResolvedContent;
    private TheSunTvPicksEntryProcessor entryProcessor;
    private TheSunTvPicksContentGroupUpdater groupUpdater;
    
    @SuppressWarnings("unchecked")
    private final RemoteSiteClient<Document> rssFetcher = context.mock(RemoteSiteClient.class);
    private Document tvPicksFeed;
    private TheSunTvPicksUpdater updater;
    
    @Before
    public void setUp() throws ValidityException, ParsingException, IOException {
        paResolvedContent = generatePaResolvedContent();
        theSunResolvedContent = generateTheSunResolvedContent();
        groupResolvedContent = generateGroupResolvedContent();
        tvPicksFeed = builder.build(new InputStreamReader(Resources.getResource("thesun-tvpicks-2013-07-02.xml").openStream()));
        entryProcessor = new TheSunTvPicksEntryProcessor(contentWriter, resolver, log);
        groupUpdater = new TheSunTvPicksContentGroupUpdater(contentGroupResolver, contentGroupWriter);
        updater = new TheSunTvPicksUpdater(rssFetcher, entryProcessor, groupUpdater, log);
    }
    
    private Item generateResolvedItem(long id, String uri) {
        Item resolved = new Item();
        resolved.setId(id);
        resolved.setCanonicalUri(uri);
        return resolved;
    }
    
    private ResolvedContent generatePaResolvedContent() {
        return ResolvedContent.builder()
                .put(TEST_CONTENT_ID, generateResolvedItem(555555L, TEST_CONTENT_ID))
                .build();
    }
    
    private ResolvedContent generateTheSunResolvedContent() {
        Set<String> contentUris =ImmutableSet.of("http://thesun.co.uk/guid/4988220",
                "http://thesun.co.uk/guid/4968490",
                "http://thesun.co.uk/guid/4958310",
                "http://thesun.co.uk/guid/4959236",
                "http://thesun.co.uk/guid/4949550");
        
        ResolvedContent.ResolvedContentBuilder builder = ResolvedContent.builder();
        long id = 600L;
        for (String contentUri : contentUris) {
            builder.put(contentUri, generateResolvedItem(id++, contentUri));
        }
        return builder.build();
    }
    
    private ResolvedContent generateGroupResolvedContent() {
        return ResolvedContent.builder().build();
    }
    
    @Test
    public void testExtraction() throws Exception {
        // Two lookups per item, first when looking for resolved content
        // second when looking for ids for the content group
        context.checking(new Expectations() {{
            oneOf(rssFetcher).get(FEED_URL);
            will(returnValue(tvPicksFeed));
            atMost(1).of(resolver).findByCanonicalUris(with(ImmutableSet.of(TEST_CONTENT_ID)));
            will(returnValue(paResolvedContent));
            atMost(59).of(resolver).findByCanonicalUris(with(any(Iterable.class)));
            will(returnValue(theSunResolvedContent));
            atMost(2).of(contentGroupResolver).findByCanonicalUris(ImmutableList.of(CONTENT_GROUP_URI));
            will(returnValue(groupResolvedContent));
            allowing(contentWriter).createOrUpdate(with(any(Item.class)));
            oneOf(contentGroupWriter).createOrUpdate(with(any(ContentGroup.class)));
        }});
        updater.run();
        assertEquals(updater.getNumberOfItemsProcessed(), 5);
        assertEquals(updater.getGroupSize(), 5);
    }
}

