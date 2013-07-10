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
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.NullAdapterLog;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.metabroadcast.common.base.Maybe;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class TheSunTvPicksParserTest {
    private final AdapterLog log = new NullAdapterLog();
    private Document tvPicksFeed;
    private TheSunTvPicksEntryProcessor entryProcessor;
    private final Builder builder = new Builder(new TheSunTvPicksElementFactory());
    
    @Before
    public void setUp() throws ValidityException, ParsingException, IOException {
        ContentWriter contentWriter = mock(ContentWriter.class);
        ResolvedContent resolvedContent = mock(ResolvedContent.class);
        ContentResolver resolver = mock(ContentResolver.class);
        when(resolver.findByCanonicalUris(ImmutableSet.of(anyString()))).thenReturn(resolvedContent);
        when(resolvedContent.get(anyString())).thenReturn(Maybe.<Identified>nothing());
        tvPicksFeed = builder.build(new InputStreamReader(Resources.getResource("thesun-tvpicks-2013-07-02.xml").openStream()));
        entryProcessor = new TheSunTvPicksEntryProcessor(contentWriter, resolver, log);
    }
    
    @Test
    public void testParse() {
        Nodes entryNodes = tvPicksFeed.query("rss/channel/item");
        Collection<Item> entries = entryProcessor.convertToItems(entryNodes);
        assertEquals(entries.size(), 5);
        Item item = Lists.newArrayList(entries).get(0);
        assertEquals(item.getCanonicalUri(), "http://thesun.co.uk/guid/4988220");
        assertEquals(item.getDescription(), "THEY love 4x4s Down Under so Oz was perfect for testing the new Range Rover Sport ");
        assertEquals(item.getLongDescription().length(), 5145);
        assertEquals(item.getImage(), "http://img.thesun.co.uk/multimedia/archive/01754/RangeRover_01_1754163a.jpg");
        assertEquals(item.getPublisher(), Publisher.THE_SUN);
    }

}
