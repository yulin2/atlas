package org.atlasapi.remotesite.rovi.populators;

import static org.atlasapi.remotesite.rovi.RoviTestUtils.resolvedContent;
import static org.atlasapi.remotesite.rovi.RoviCanonicalUriGenerator.canonicalUriForProgram;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.LookupRef;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.remotesite.rovi.RoviTestUtils;
import org.atlasapi.remotesite.rovi.model.ActionType;
import org.atlasapi.remotesite.rovi.model.RoviProgramLine;
import org.atlasapi.remotesite.rovi.model.RoviShowType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

@RunWith(MockitoJUnitRunner.class)
public class BaseContentPopulatorTest {

    private static final String PROGRAM_ID = "123456";
    private static final String PARENT_PROGRAM_ID = "987654";
    private static final String PARENT_PROGRAM_CANONICAL_URI = canonicalUriForProgram(PARENT_PROGRAM_ID);
    
    @Mock
    private ContentResolver contentResolver;
    
    @Before
    public void init() {
        when(contentResolver.findByCanonicalUris(ImmutableList.of(PARENT_PROGRAM_CANONICAL_URI))).thenReturn(resolvedContent(parentBrand()));
    }
    
    @Test
    public void testPopulation() {
        RoviProgramLine.Builder programLine = RoviProgramLine.builder();
        
        programLine.withShowType(RoviShowType.SERIES_MASTER);
        programLine.withProgramId(PROGRAM_ID);
        programLine.withLongTitle("This is a brand");
        programLine.withActionType(ActionType.INSERT);
        programLine.withTitleParentId(PARENT_PROGRAM_ID);
        
        BaseContentPopulator<Brand> populator = new BaseContentPopulator<Brand>(
                Optional.of(programLine.build()),
                RoviTestUtils.descriptions(PROGRAM_ID),
                contentResolver);
        
        Brand brand = new Brand();
        populator.populateContent(brand);
        
        assertEquals(canonicalUriForProgram(PROGRAM_ID), brand.getCanonicalUri());
        assertEquals(RoviTestUtils.LONG_DESCRIPTION, brand.getLongDescription());
        assertEquals(RoviTestUtils.MEDIUM_DESCRIPTION, brand.getMediumDescription());
        assertEquals(RoviTestUtils.SHORT_DESCRIPTION, brand.getShortDescription());
        assertEquals(RoviTestUtils.LONG_DESCRIPTION, brand.getDescription());
        assertThat(brand.getEquivalentTo(), hasItem(LookupRef.from(parentBrand())));
    }
    
    @Test(expected=RuntimeException.class)
    public void testPopulationFromDeletionShouldThrowAnException() {
        RoviProgramLine.Builder programLine = RoviProgramLine.builder();
        
        programLine.withProgramId(PROGRAM_ID);
        programLine.withActionType(ActionType.DELETE);
        
        BaseContentPopulator<Brand> populator = new BaseContentPopulator<Brand>(
                Optional.of(programLine.build()),
                RoviTestUtils.descriptions(PROGRAM_ID),
                contentResolver);  
        
        populator.populateContent(new Brand());
    }
    
    @Test
    public void testUnsettingExplicitEquivalenceWithAnUpdate() {
       RoviProgramLine.Builder programLine = RoviProgramLine.builder();
        
        programLine.withShowType(RoviShowType.SERIES_MASTER);
        programLine.withProgramId(PROGRAM_ID);
        programLine.withLongTitle("This is a brand");
        programLine.withActionType(ActionType.UPDATE);
        
        BaseContentPopulator<Brand> populator = new BaseContentPopulator<Brand>(
                Optional.of(programLine.build()),
                RoviTestUtils.descriptions(PROGRAM_ID),
                contentResolver);
        
        Brand brand = new Brand();
        brand.setEquivalentTo(Sets.newHashSet(LookupRef.from(new Brand("uri", "curie", Publisher.ROVI_EN_GB))));
        populator.populateContent(brand);
        
        assertTrue(brand.getEquivalentTo().isEmpty());
    }
    
    private Brand parentBrand() {
        Brand parentBrand = new Brand();
        String parentCanonicalUri = canonicalUriForProgram(PARENT_PROGRAM_ID);
        parentBrand.setCanonicalUri(parentCanonicalUri);
        parentBrand.setPublisher(Publisher.ROVI_IT);
        return parentBrand;
    }
}
