package org.atlasapi.remotesite.talktalk;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.ParentRef;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Series;
import org.atlasapi.remotesite.talktalk.vod.bindings.ItemDetailType;
import org.atlasapi.remotesite.talktalk.vod.bindings.ItemTypeType;
import org.junit.Test;

import com.google.common.base.Optional;


public class TalkTalkItemDetailContainerExtractorTest {
    
    private final TalkTalkItemDetailContainerExtractor extractor
        = new TalkTalkItemDetailContainerExtractor();
    
    @Test
    public void testExtractingBrand() {
        
        ItemDetailType detail = new ItemDetailType();
        detail.setItemType(ItemTypeType.BRAND);
        detail.setId("id");
        
        Brand brand = extractor.extractBrand(detail);
        
        assertThat(brand.getCanonicalUri(), is("http://talktalk.net/brands/id"));
        assertThat(brand.getPublisher(), is(Publisher.TALK_TALK));
        
    }

    @Test
    public void testExtractingTopLevelSeries() {
        
        ItemDetailType detail = new ItemDetailType();
        detail.setItemType(ItemTypeType.SERIES);
        detail.setId("id");
        
        Series series = extractor.extractSeries(detail, Optional.<Brand>absent());
        
        assertThat(series.getCanonicalUri(), is("http://talktalk.net/series/id"));
        assertThat(series.getPublisher(), is(Publisher.TALK_TALK));
        assertThat(series.getParent(), is(nullValue()));
        
    }

    @Test
    public void testExtractingNonTopLevelSeries() {
        
        ItemDetailType detail = new ItemDetailType();
        detail.setItemType(ItemTypeType.SERIES);
        detail.setId("id");
        
        Brand brand = new Brand("brand","brand", Publisher.TALK_TALK);
        Series series = extractor.extractSeries(detail, Optional.of(brand));
        
        assertThat(series.getCanonicalUri(), is("http://talktalk.net/series/id"));
        assertThat(series.getPublisher(), is(Publisher.TALK_TALK));
        assertThat(series.getParent(), is(ParentRef.parentRefFrom(brand)));
        
    }

    @Test
    public void testExtractingSeriesNumber() {
        
        ItemDetailType detail = new ItemDetailType();
        detail.setItemType(ItemTypeType.SERIES);
        detail.setTitle("No Ordinary Family S101");
        
        Series series = extractor.extractSeries(detail, Optional.<Brand>absent());
        
        assertThat(series.getSeriesNumber(), is(101));
        assertThat(series.getTitle(), is("No Ordinary Family"));
        
    }
    
}
