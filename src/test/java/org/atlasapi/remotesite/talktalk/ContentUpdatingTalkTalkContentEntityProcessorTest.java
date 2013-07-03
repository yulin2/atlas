package org.atlasapi.remotesite.talktalk;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Series;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.remotesite.talktalk.vod.bindings.ItemDetailType;
import org.atlasapi.remotesite.talktalk.vod.bindings.ItemTypeType;
import org.atlasapi.remotesite.talktalk.vod.bindings.VODEntityType;
import org.junit.Test;

import com.metabroadcast.common.scheduling.UpdateProgress;


public class ContentUpdatingTalkTalkContentEntityProcessorTest {
    
    private final TalkTalkClient client = mock(TalkTalkClient.class);
    private final ContentResolver resolver = mock(ContentResolver.class);
    private final ContentWriter writer = mock(ContentWriter.class);
    private final ContentUpdatingTalkTalkContentEntityProcessor processor 
        = new ContentUpdatingTalkTalkContentEntityProcessor(client, resolver, writer);
    
    @Test
    public void testProcessingBrandVodEntity() throws TalkTalkException {
        
        VODEntityType entity = new VODEntityType();
        entity.setId("brand");
        entity.setItemType(ItemTypeType.BRAND);
        
        when(client.getItemDetail(entity.getItemType(), entity.getId()))
            .thenReturn(itemDetail(entity));
        when(resolver.findByCanonicalUris(argThat(hasItem("http://talktalk.net/brands/brand"))))
            .thenReturn(ResolvedContent.builder().build());
        when(client.processVodList(argThat(is(entity.getItemType())), 
                argThat(is(entity.getId())), anyVodEntityProcessor(), anyInt()))
            .thenReturn(UpdateProgress.SUCCESS);
        
        UpdateProgress progress = processor.processBrandEntity(entity);
        
        assertThat(progress, is(UpdateProgress.SUCCESS));
        
        verify(client).getItemDetail(entity.getItemType(), entity.getId());
        verify(resolver).findByCanonicalUris(argThat(hasItem("http://talktalk.net/brands/brand")));
        verify(client).processVodList(argThat(is(entity.getItemType())), 
            argThat(is(entity.getId())), anyVodEntityProcessor(), anyInt());
        verify(writer).createOrUpdate(any(Brand.class));
    }
    
    @Test
    public void testProcessingSeriesVodEntity() throws TalkTalkException {
        
        VODEntityType entity = new VODEntityType();
        entity.setId("series");
        entity.setItemType(ItemTypeType.SERIES);
        
        when(client.getItemDetail(entity.getItemType(), entity.getId()))
            .thenReturn(itemDetail(entity));
        when(resolver.findByCanonicalUris(argThat(hasItem("http://talktalk.net/series/series"))))
            .thenReturn(ResolvedContent.builder().build());
        when(client.processVodList(argThat(is(entity.getItemType())), 
                argThat(is(entity.getId())), anyVodEntityProcessor(), anyInt()))
                .thenReturn(UpdateProgress.SUCCESS);
        
        UpdateProgress progress = processor.processSeriesEntity(entity);
        
        assertThat(progress, is(UpdateProgress.SUCCESS));
        
        verify(client).getItemDetail(entity.getItemType(), entity.getId());
        verify(resolver).findByCanonicalUris(argThat(hasItem("http://talktalk.net/series/series")));
        verify(client).processVodList(argThat(is(entity.getItemType())), 
                argThat(is(entity.getId())), anyVodEntityProcessor(), anyInt());
        verify(writer).createOrUpdate(any(Series.class));
    }

    @Test
    public void testProcessingItemVodEntity() throws TalkTalkException {
        
        VODEntityType entity = new VODEntityType();
        entity.setId("item");
        entity.setItemType(ItemTypeType.EPISODE);
        
        when(client.getItemDetail(entity.getItemType(), entity.getId()))
            .thenReturn(itemDetail(entity));
        when(resolver.findByCanonicalUris(argThat(hasItem("http://talktalk.net/episodes/item"))))
            .thenReturn(ResolvedContent.builder().build());
        
        UpdateProgress progress = processor.processEpisodeEntity(entity);
        
        assertThat(progress, is(UpdateProgress.SUCCESS));
        
        verify(client).getItemDetail(entity.getItemType(), entity.getId());
        verify(resolver).findByCanonicalUris(argThat(hasItem("http://talktalk.net/episodes/item")));
        verify(writer).createOrUpdate(any(Item.class));
    }

    @SuppressWarnings("unchecked")
    private TalkTalkVodEntityProcessor<UpdateProgress> anyVodEntityProcessor() {
        return any(TalkTalkVodEntityProcessor.class);
    }

    private ItemDetailType itemDetail(VODEntityType entity) {
        ItemDetailType detail = new ItemDetailType();
        detail.setId(entity.getId());
        detail.setItemType(entity.getItemType());
        return detail;
    }
    
}
