package org.atlasapi.remotesite.thesun;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentGroupResolver;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.ResolvedContent;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.metabroadcast.common.base.Maybe;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class TheSunTvPicksEntryProcessorTest {
    private Document tvPicksFeed;
    private TheSunTvPicksEntryProcessor entryProcessor;
    private final Builder builder = new Builder(new TheSunTvPicksElementFactory());
    
    @Before
    public void setUp() throws ValidityException, ParsingException, IOException {
        ContentWriter contentWriter = mock(ContentWriter.class);
        ResolvedContent resolvedContent1234 = ResolvedContent
                .builder()
                .put("http://pressassociation.com/programmes/abc1234", new Identified("http://pressassociation.com/programmes/abc1234"))
                .build();
        ResolvedContent resolvedContent1235 = ResolvedContent
                .builder()
                .put("http://pressassociation.com/programmes/abc1235", new Identified("http://pressassociation.com/programmes/abc1235"))
                .build();
        ContentResolver resolver = mock(ContentResolver.class);
        
        when(resolver.findByCanonicalUris(ImmutableSet.of("http://pressassociation.com/programmes/abc1234")))
            .thenReturn(resolvedContent1234);
        when(resolver.findByCanonicalUris(ImmutableSet.of("http://pressassociation.com/programmes/abc1235")))
            .thenReturn(resolvedContent1235);
        
        tvPicksFeed = builder.build(new InputStreamReader(Resources.getResource("thesun-tvpicks-2013-07-02.xml").openStream()));
        entryProcessor = new TheSunTvPicksEntryProcessor(contentWriter, resolver);
    }
    
    @Test
    public void testParse() {
        Nodes entryNodes = tvPicksFeed.query("rss/channel/item");
        Collection<Item> entries = entryProcessor.convertToItems(entryNodes);
        assertEquals(entries.size(), 2);
        Item item = Lists.newArrayList(entries).get(0);
        assertEquals(item.getCanonicalUri(), "http://thesun.co.uk/guid/4988220");
        assertEquals(item.getDescription(), "Programme 1 is a great programme ");
        assertEquals(item.getLongDescription().length(), 31);
        assertEquals(item.getImage(), "http://example.com/image123.jpg");
        assertEquals(item.getPublisher(), Publisher.THE_SUN);
    }

}
