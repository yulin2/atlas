package org.atlasapi.remotesite.talktalk;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Series;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.remotesite.talktalk.TalkTalkClient.TalkTalkVodListCallback;
import org.atlasapi.remotesite.talktalk.vod.bindings.ItemDetailType;
import org.atlasapi.remotesite.talktalk.vod.bindings.ItemTypeType;
import org.atlasapi.remotesite.talktalk.vod.bindings.VODEntityType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableList;

@RunWith(MockitoJUnitRunner.class)
public class ContentUpdatingTalkTalkContentEntityProcessorTest {
    
    private final TalkTalkClient client = mock(TalkTalkClient.class);
    private final ContentResolver resolver = mock(ContentResolver.class);
    private final ContentWriter writer = mock(ContentWriter.class);
    private final ContentUpdatingTalkTalkVodEntityProcessor processor 
        = new ContentUpdatingTalkTalkVodEntityProcessor(client, resolver, writer);
    
    @Test
    public void testProcessingBrandVodEntity() throws TalkTalkException {
        
        VODEntityType entity = new VODEntityType();
        entity.setId("brand");
        entity.setItemType(ItemTypeType.BRAND);

        when(client.getItemDetail(groupType(entity), entity.getId()))
            .thenReturn(itemDetail(entity));
        when(resolver.findByCanonicalUris(argThat(hasItem("http://talktalk.net/brands/brand"))))
            .thenReturn(ResolvedContent.builder().build());
        when(client.processVodList(argThat(is(groupType(entity))), 
                argThat(is(entity.getId())), anyProcessor()))
            .thenReturn(ImmutableList.<Content>of());
        
        List<Content> content = processor.processEntity(entity);
        
        assertNotNull(content);
        assertThat(content.get(0), instanceOf(Brand.class));
        
        verify(client).getItemDetail(groupType(entity), entity.getId());
        verify(resolver).findByCanonicalUris(argThat(hasItem("http://talktalk.net/brands/brand")));
        verify(writer).createOrUpdate(any(Brand.class));
    }

    @Test
    public void testProcessingSeriesVodEntity() throws TalkTalkException {
        
        VODEntityType entity = new VODEntityType();
        entity.setId("series");
        entity.setItemType(ItemTypeType.SERIES);

        when(client.getItemDetail(groupType(entity), entity.getId()))
            .thenReturn(itemDetail(entity));
        when(resolver.findByCanonicalUris(argThat(hasItem("http://talktalk.net/series/series"))))
            .thenReturn(ResolvedContent.builder().build());
        when(client.processVodList(argThat(is(groupType(entity))), 
                argThat(is(entity.getId())), anyProcessor()))
            .thenReturn(ImmutableList.<Content>of());
        
        List<Content> content = processor.processEntity(entity);
        
        assertNotNull(content);
        assertThat(content.get(0), instanceOf(Series.class));
        
        verify(client).getItemDetail(groupType(entity), entity.getId());
        verify(resolver).findByCanonicalUris(argThat(hasItem("http://talktalk.net/series/series")));
        verify(writer).createOrUpdate(any(Series.class));
    }

    @SuppressWarnings("unchecked")
    private TalkTalkVodListCallback<List<Content>> anyProcessor() {
        return any(TalkTalkVodListCallback.class);
    }

    @Test
    public void testProcessingItemVodEntity() throws TalkTalkException {
        
        VODEntityType entity = new VODEntityType();
        entity.setId("item");
        entity.setItemType(ItemTypeType.EPISODE);
        
        when(client.getItemDetail(groupType(entity), entity.getId()))
            .thenReturn(itemDetail(entity));
        when(resolver.findByCanonicalUris(argThat(hasItem("http://talktalk.net/episodes/item"))))
            .thenReturn(ResolvedContent.builder().build());

        List<Content> contentList = processor.processEntity(entity);

        assertNotNull(contentList);

        verify(client).getItemDetail(groupType(entity), entity.getId());
        verify(resolver).findByCanonicalUris(argThat(hasItem("http://talktalk.net/episodes/item")));
        verify(writer).createOrUpdate(any(Item.class));
    }

    private GroupType groupType(VODEntityType entity) {
        return GroupType.fromItemType(entity.getItemType()).get();
    }

    private ItemDetailType itemDetail(VODEntityType entity) {
        ItemDetailType detail = new ItemDetailType();
        detail.setId(entity.getId());
        detail.setItemType(entity.getItemType());
        return detail;
    }
    
}
