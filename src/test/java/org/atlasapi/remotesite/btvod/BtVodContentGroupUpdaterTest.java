package org.atlasapi.remotesite.btvod;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Map;

import javax.annotation.Nullable;

import org.atlasapi.media.entity.ContentGroup;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.testing.ComplexItemTestDataBuilder;
import org.atlasapi.persistence.content.ContentGroupResolver;
import org.atlasapi.persistence.content.ContentGroupWriter;
import org.atlasapi.persistence.content.ResolvedContent.ResolvedContentBuilder;
import org.atlasapi.remotesite.btvod.BtVodData.BtVodDataRow;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;


@RunWith( MockitoJUnitRunner.class )
public class BtVodContentGroupUpdaterTest {

    private static final String uriPrefix = "http://example.org/";
    private static final Publisher PUBLISHER = Publisher.BT_VOD;
    
    private final Item item1 = ComplexItemTestDataBuilder.complexItem().build();
    private final Item item2 = ComplexItemTestDataBuilder.complexItem().build();
    
    private final ContentGroupResolver contentGroupResolver = mock(ContentGroupResolver.class);
    private final ContentGroupWriter contentGroupWriter = mock(ContentGroupWriter.class);
    
    private Map<String, Predicate<VodDataAndContent>> contentGroupsAndCriteria = 
            ImmutableMap.of();
    
    private BtVodContentGroupUpdater updater;
    
    @Test
    public void testCreatesContentGroupWithCorrectContents() {
        String key = "a";
        contentGroupsAndCriteria = ImmutableMap.of(key, item1Predicate);
        updater = new BtVodContentGroupUpdater(contentGroupResolver, 
                contentGroupWriter, contentGroupsAndCriteria, uriPrefix, PUBLISHER);
        
        when(contentGroupResolver.findByCanonicalUris(ImmutableSet.of(uriPrefix + key)))
            .thenReturn(new ResolvedContentBuilder().build());
        
        BtVodDataRow dummyDataRow = new BtVodDataRow(ImmutableList.<String>of(), 
                ImmutableList.<String>of());
        
        updater.start();
        updater.onContent(item1, dummyDataRow);
        updater.onContent(item2, dummyDataRow);
        updater.finish();
        
        ArgumentCaptor<ContentGroup> contentGroupCaptor = ArgumentCaptor.forClass(ContentGroup.class);
        verify(contentGroupWriter).createOrUpdate(contentGroupCaptor.capture());
        
        assertEquals(item1.getCanonicalUri(), 
                Iterables.getOnlyElement(contentGroupCaptor.getValue().getContents()).getUri());
        
    }
    
    private final Predicate<VodDataAndContent> item1Predicate = 
            new Predicate<VodDataAndContent>() {

        @Override
        public boolean apply(VodDataAndContent input) {
            return item1.getCanonicalUri().equals(input.getContent().getCanonicalUri());
        }
    };
}
