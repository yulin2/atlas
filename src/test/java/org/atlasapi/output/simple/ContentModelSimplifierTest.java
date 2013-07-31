package org.atlasapi.output.simple;

import static org.junit.Assert.assertEquals;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.simple.Playlist;
import org.atlasapi.media.product.ProductResolver;
import org.atlasapi.media.segment.SegmentResolver;
import org.atlasapi.output.Annotation;
import org.atlasapi.persistence.content.ContentGroupResolver;
import org.atlasapi.persistence.content.PeopleQueryResolver;
import org.atlasapi.persistence.output.AvailableChildrenResolver;
import org.atlasapi.persistence.output.ContainerSummaryResolver;
import org.atlasapi.persistence.output.RecentlyBroadcastChildrenResolver;
import org.atlasapi.persistence.output.UpcomingChildrenResolver;
import org.atlasapi.persistence.topic.TopicQueryResolver;
import org.junit.Test;
import org.mockito.Mockito;

import com.metabroadcast.common.ids.NumberToShortStringCodec;


public class ContentModelSimplifierTest {
    
    private ApplicationConfiguration applicationConfig = Mockito.mock(ApplicationConfiguration.class);

    @SuppressWarnings("unchecked")
    private final ContainerModelSimplifier containerSimplifier = new ContainerModelSimplifier(
        Mockito.mock(ModelSimplifier.class), 
        "", 
        Mockito.mock(ContentGroupResolver.class), 
        Mockito.mock(TopicQueryResolver.class), 
        Mockito.mock(AvailableChildrenResolver.class), 
        Mockito.mock(UpcomingChildrenResolver.class), 
        Mockito.mock(ProductResolver.class), 
        Mockito.mock(RecentlyBroadcastChildrenResolver.class), 
        Mockito.mock(ImageSimplifier.class),
        Mockito.mock(PeopleQueryResolver.class)
    );
    
    private final ItemModelSimplifier itemSimplifier = new ItemModelSimplifier(
        "", 
        Mockito.mock(ContentGroupResolver.class), 
        Mockito.mock(TopicQueryResolver.class), 
        Mockito.mock(ProductResolver.class), 
        Mockito.mock(SegmentResolver.class), 
        Mockito.mock(ContainerSummaryResolver.class), 
        Mockito.mock(ChannelResolver.class), 
        Mockito.mock(NumberToShortStringCodec.class), 
        Mockito.mock(NumberToShortStringCodec.class), 
        Mockito.mock(ImageSimplifier.class),
        Mockito.mock(PeopleQueryResolver.class)
    );

    @Test
    public void testUsesLowerCaseIdCodec() {
        Container container = new Container();
        container.setId(1234l);
        containerSimplifier.exposeIds(true);
        Playlist simplified = containerSimplifier.simplify(container, Annotation.defaultAnnotations(), applicationConfig);
        String lowerCasedId = simplified.getId().toLowerCase();
        assertEquals(lowerCasedId, simplified.getId());
        
        Item item = new Item();
        item.setId(1234l);
        itemSimplifier.exposeIds(true);
        org.atlasapi.media.entity.simple.Item simpleItem = itemSimplifier.simplify(item , Annotation.defaultAnnotations(), applicationConfig);
        lowerCasedId = simplified.getId().toLowerCase();
        assertEquals(lowerCasedId, simpleItem.getId());
    }

}
