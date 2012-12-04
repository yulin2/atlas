package org.atlasapi.remotesite.btvod;

import static org.atlasapi.media.entity.Publisher.BT;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import nu.xom.Element;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Series;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.remotesite.ContentExtractor;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public class BtVodWriteOrderTest {

    private static final ResolvedContent NOTHING_RESOLVED = ResolvedContent.builder().build();
    private static final Element EMPTY_ELEMENT = new Element("someName");
    
    private final ContentResolver resolver = mock(ContentResolver.class);
    private final ContentWriter writer = mock(ContentWriter.class);
    
    @SuppressWarnings("unchecked")
    private final ContentExtractor<Element, Set<? extends Content>> extractor = mock(ContentExtractor.class);
    
    private final DefaultBtVodXmlElementHandler handler = new DefaultBtVodXmlElementHandler(extractor, resolver, writer);
    
    @Test
    public void testWriteOrder() {
        Series series = new Series("series", "s", BT);
        series.withSeriesNumber(2);
        Episode episode = new Episode("episode", "e", BT);
        episode.setContainer(series);
        episode.setSeries(series);
        
        for(List<Content> contentOrdering : Collections2.permutations(ImmutableList.of(series,episode))) {
            OngoingStubbing<Set<? extends Content>> stubbing = Mockito.<Set<? extends Content>>when(extractor.extract(EMPTY_ELEMENT));
            
            stubbing = stubbing.thenReturn(ImmutableSet.copyOf(contentOrdering));
            
            when(resolver.findByCanonicalUris(Matchers.<Iterable<String>>any())).thenReturn(NOTHING_RESOLVED);
            
            handler.prepare();

            handler.handle(EMPTY_ELEMENT);
            
            handler.finish();
            
            InOrder inOrder = inOrder(writer);
            inOrder.verify(writer, times(1)).createOrUpdate(series);
            inOrder.verify(writer, times(1)).createOrUpdate(episode);
            inOrder.verifyNoMoreInteractions();
            reset(writer);
        }
    }

}
