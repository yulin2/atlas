package org.atlasapi.remotesite.btvod;

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
import org.mockito.runners.MockitoJUnitRunner;

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
    
    private final BtVodBrandWriter brandExtractor 
                    = new BtVodBrandWriter(
                                contentWriter, 
                                contentResolver, 
                                PUBLISHER, URI_PREFIX,
                                contentListener,
                                new BtVodDescribedFieldsExtractor(),
                                Sets.<String>newHashSet());
    
    @Test
    public void testExtractsBrands() {
        when(contentResolver.findByCanonicalUris(ImmutableSet.of(brandUri())))
            .thenReturn(ResolvedContent.builder().build());
        
        brandExtractor.process(brandRow());
        
        verify(contentWriter).createOrUpdate(expectedBrand());
    }
    
    private BtVodDataRow brandRow() {
        Builder<String, String> rows = ImmutableMap.builder();
        rows.put(BtVodFileColumn.BRANDIA_ID.key(), BRAND_ID);
        rows.put(BtVodFileColumn.BRAND_TITLE.key(), BRAND_TITLE);
        
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
