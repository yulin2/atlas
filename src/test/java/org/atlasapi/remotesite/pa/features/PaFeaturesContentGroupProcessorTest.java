package org.atlasapi.remotesite.pa.features;

import static org.junit.Assert.*;
import static org.mockito.Mockito.times;

import org.atlasapi.media.entity.ContentGroup;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentGroupResolver;
import org.atlasapi.persistence.content.ContentGroupWriter;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.remotesite.pa.features.PaFeaturesContentGroupProcessor.FeatureSetContentGroups;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;


public class PaFeaturesContentGroupProcessorTest {

    private static final String FEATURE_SET_URI_BASE = "featureSetBase";
    private static final String FEATURE_SET_ID = "FeatureSetId";
    
    private ContentGroupResolver contentGroupResolver = Mockito.mock(ContentGroupResolver.class);
    private ContentGroupWriter contentGroupWriter = Mockito.mock(ContentGroupWriter.class);
    private PaFeaturesConfiguration config = new PaFeaturesConfiguration(ImmutableMap.of(
            FEATURE_SET_ID, new ContentGroupDetails(Publisher.PA_FEATURES, FEATURE_SET_URI_BASE)
            ));
    
    private final PaFeaturesContentGroupProcessor processor = new PaFeaturesContentGroupProcessor(contentGroupResolver, contentGroupWriter, config);
    
    @Test
    public void testCreationOrFetchingOfContentGroups() {
        
        String todayUri = FEATURE_SET_URI_BASE;
        String allUri = FEATURE_SET_URI_BASE + "/all";
        
        ContentGroup todayGroup = mockResolution(todayUri);
        ContentGroup allGroup = mockResolution(allUri);
        
        processor.prepareUpdate();
        
        FeatureSetContentGroups contentGroups = processor.getContentGroups(FEATURE_SET_ID);
        
        assertEquals(todayGroup, contentGroups.getTodayContentGroup());
        assertEquals(allGroup, contentGroups.getAllFeaturedContentContentGroup());
        
        Mockito.verify(contentGroupResolver, times(1)).findByCanonicalUris(ImmutableList.of(todayUri));
        Mockito.verify(contentGroupResolver, times(1)).findByCanonicalUris(ImmutableList.of(allUri));
    }
    
    @Test
    public void testFetchingNonExistentFeatureSet() {
        String todayUri = FEATURE_SET_URI_BASE;
        String allUri = FEATURE_SET_URI_BASE + "/all";
        
        mockResolution(todayUri);
        mockResolution(allUri);
        
        processor.prepareUpdate();
        
        assertNull(processor.getContentGroups("invalidFeatureSet"));
    }
    
    @Test
    public void testWritingOfContentGroups() {
        String todayUri = FEATURE_SET_URI_BASE;
        String allUri = FEATURE_SET_URI_BASE + "/all";
        
        ContentGroup todayGroup = mockResolution(todayUri);
        ContentGroup allGroup = mockResolution(allUri);
        
        processor.prepareUpdate();
        processor.finishUpdate();
        
        Mockito.verify(contentGroupWriter, times(1)).createOrUpdate(todayGroup);
        Mockito.verify(contentGroupWriter, times(1)).createOrUpdate(allGroup);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testContentGroupFetchBeforePreparationThrowsException() {
        processor.getContentGroups(FEATURE_SET_ID);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFinishBeforePreparationThrowsException() {
        processor.finishUpdate();
    }

    private ContentGroup mockResolution(String cgUri) {
        ContentGroup cg = new ContentGroup(cgUri);
        Mockito.when(contentGroupResolver.findByCanonicalUris(ImmutableList.of(cgUri))).thenReturn(ResolvedContent.builder().put(cgUri, cg).build());
        return cg;
    }

}
