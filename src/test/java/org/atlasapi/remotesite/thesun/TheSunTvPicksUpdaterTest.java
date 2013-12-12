package org.atlasapi.remotesite.thesun;

import static org.mockito.Mockito.*;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.content.ContentGroupResolver;
import org.atlasapi.persistence.content.ContentGroupWriter;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;


public class TheSunTvPicksUpdaterTest {
    protected static final int ContentGroup = 0;
    private static String FEED_URL = "http://example.org/sunfeed";
    private static String TEST_CONTENT_ID = "http://pressassociation.com/programmes/abc1234";    
    private static String CONTENT_GROUP_URI = "http://example.org/sunfeed";   
    private final Builder builder = new Builder(new TheSunTvPicksElementFactory());
    private ContentWriter contentWriter;
    private ContentResolver resolver;
    private ContentGroupResolver contentGroupResolver;
    private ContentGroupWriter contentGroupWriter;
    private ResolvedContent paResolvedContent;
    private ResolvedContent theSunResolvedContent;
    private ResolvedContent groupResolvedContent;
    private TheSunTvPicksEntryProcessor entryProcessor;
    private TheSunTvPicksContentGroupUpdater groupUpdater;
    private RemoteSiteClient<Document> rssFetcher;
    private Document tvPicksFeed;
    private TheSunTvPicksUpdater updater;
    
    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws ValidityException, ParsingException, IOException {
        contentWriter = mock(ContentWriter.class);
        resolver = mock(ContentResolver.class);
        contentGroupResolver = mock(ContentGroupResolver.class);
        contentGroupWriter = mock(ContentGroupWriter.class);
        rssFetcher = (RemoteSiteClient<Document>) mock(RemoteSiteClient.class);
        paResolvedContent = generatePaResolvedContent();
        theSunResolvedContent = generateTheSunResolvedContent();
        groupResolvedContent = generateGroupResolvedContent();
        tvPicksFeed = builder.build(new InputStreamReader(Resources.getResource("thesun-tvpicks-2013-07-02.xml").openStream()));
        entryProcessor = new TheSunTvPicksEntryProcessor(contentWriter, resolver);
        groupUpdater = new TheSunTvPicksContentGroupUpdater(contentGroupResolver, contentGroupWriter);
        updater = new TheSunTvPicksUpdater(FEED_URL, CONTENT_GROUP_URI, rssFetcher, entryProcessor, groupUpdater);
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
        Set<String> contentUris =ImmutableSet.of("http://pressassociation.com/programmes/abc1234",
                "http://pressassociation.com/programmes/abc1235", "http://thesun.co.uk/guid/4988220", "http://thesun.co.uk/guid/4968490");
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
        when(rssFetcher.get(FEED_URL)).thenReturn(tvPicksFeed);
        when(resolver.findByCanonicalUris(ImmutableSet.of(TEST_CONTENT_ID))).thenReturn(paResolvedContent);
        when(resolver.findByCanonicalUris(ImmutableSet.of(anyString()))).thenReturn(theSunResolvedContent);
        when(contentGroupResolver.findByCanonicalUris(ImmutableList.of(CONTENT_GROUP_URI))).thenReturn(groupResolvedContent);
        updater.run();
        verify(rssFetcher, times(1)).get(FEED_URL);
        verify(resolver).findByCanonicalUris(ImmutableSet.of(TEST_CONTENT_ID));
        verify(resolver, times(4)).findByCanonicalUris(ImmutableSet.of(anyString()));
        verify(contentGroupResolver, times(1)).findByCanonicalUris(ImmutableList.of(CONTENT_GROUP_URI));
        assertEquals(2, updater.getNumberOfItemsProcessed());
        assertEquals(2, updater.getGroupSize());
    }
}

