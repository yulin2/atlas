package org.atlasapi.remotesite.btvod;

import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.remotesite.btvod.BtVodData.BtVodDataRow;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;

@RunWith(MockitoJUnitRunner.class)
public class BtVodBrandWriterTest {

    private static final String BRAND_TITLE = "Title";
    private static final String BRAND_ID = "1234";
    private static final Publisher PUBLISHER = Publisher.BT_VOD;
    private static final String URI_PREFIX = "http://example.org/";
    
    private final ContentWriter contentWriter = mock(ContentWriter.class);
    private final ContentResolver contentResolver = mock(ContentResolver.class);
    private final BtVodContentListener contentListener = mock(BtVodContentListener.class);
    private final ImageUriProvider imageUriProvider = mock(ImageUriProvider.class);
    
    private final BtVodBrandWriter brandExtractor 
                    = new BtVodBrandWriter(
                                contentWriter, 
                                contentResolver, 
                                PUBLISHER, URI_PREFIX,
                                contentListener,
                                new BtVodDescribedFieldsExtractor(imageUriProvider),
                                Sets.<String>newHashSet());
    
    @Test
    public void testExtractsBrands() {
        when(imageUriProvider.imageUriFor(Matchers.anyString())).thenReturn(Optional.<String>absent());
        when(contentResolver.findByCanonicalUris(ImmutableSet.of(brandUri())))
            .thenReturn(ResolvedContent.builder().build());
        
        brandExtractor.process(brandRow());
        
        verify(contentWriter).createOrUpdate(expectedBrand());
    }
    
    @Test 
    public void testCreatesSyntheticBrandFromEpisodeData() {
        when(imageUriProvider.imageUriFor(Matchers.anyString())).thenReturn(Optional.<String>absent());
        when(contentResolver.findByCanonicalUris(ImmutableSet.of(URI_PREFIX + "/synthesized/brands/perception")))
            .thenReturn(ResolvedContent.builder().build());
        
        brandExtractor.process(episodeRow());
        
        ArgumentCaptor<Brand> captor = ArgumentCaptor.forClass(Brand.class);
        verify(contentWriter).createOrUpdate(captor.capture());
        
        Brand saved = captor.getValue();
        assertThat(saved.getCanonicalUri(), is(URI_PREFIX + "/synthesized/brands/perception"));
        assertThat(saved.getTitle(), is("Perception"));
    }
    
    private BtVodDataRow episodeRow() {
        Builder<String, String> rows = ImmutableMap.builder();
        rows.put(BtVodFileColumn.PRODUCT_TITLE.key(), "Perception S1-E9 Shadow");
        rows.put(BtVodFileColumn.BRANDIA_ID.key(), "");
        rows.put(BtVodFileColumn.PRODUCT_ID.key(), "DIS000604904_RL1");
        rows.put(BtVodFileColumn.SERIES_NUMBER.key(), "2");
        rows.put(BtVodFileColumn.SYNOPSIS.key(), "");
        
        Map<String, String> map = rows.build();
        return new BtVodDataRow(ImmutableList.copyOf(map.values()), 
                ImmutableList.copyOf(map.keySet()));
    }
    
    private BtVodDataRow brandRow() {
        Builder<String, String> rows = ImmutableMap.builder();
        rows.put(BtVodFileColumn.BRANDIA_ID.key(), BRAND_ID);
        rows.put(BtVodFileColumn.BRAND_TITLE.key(), BRAND_TITLE);
        rows.put(BtVodFileColumn.PRODUCT_ID.key(), BRAND_ID);
        rows.put(BtVodFileColumn.SYNOPSIS.key(), "A synopsis");
        rows.put(BtVodFileColumn.PACKSHOT.key(), "");
        Map<String, String> map = rows.build();
        return new BtVodDataRow(ImmutableList.copyOf(map.values()), 
                ImmutableList.copyOf(map.keySet()));
    }
    
    private Brand expectedBrand() {
        Brand brand = new Brand(brandUri(), null, 
                PUBLISHER);
        brand.setTitle(BRAND_TITLE);
        return brand;
    }

    private String brandUri() {
        return URI_PREFIX + "brands/" + BRAND_ID;
    }
}
