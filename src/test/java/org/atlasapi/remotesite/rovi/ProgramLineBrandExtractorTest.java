package org.atlasapi.remotesite.rovi;

import static org.atlasapi.remotesite.rovi.RoviUtils.canonicalUriForProgram;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collection;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.remotesite.rovi.program.ProgramLineBrandExtractor;
import org.atlasapi.remotesite.rovi.program.RoviProgramDescriptionLine;
import org.atlasapi.remotesite.rovi.program.RoviProgramLine;
import org.atlasapi.remotesite.rovi.series.RoviSeriesLine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Lists;


@RunWith(MockitoJUnitRunner.class)
public class ProgramLineBrandExtractorTest {
    
    private static final String SYNOPSIS = "This is the synopsis";
    private static final String SHORT_DESCRIPTION = "This is the short description";
    private static final String MEDIUM_DESCRIPTION = "This is the medium description";
    private static final String LONG_DESCRIPTION = "This is the long description";
    private static final String PROGRAM_ID = "12345";
    private static final String TITLE = "This is a brand";
    
    @Mock
    private KeyedFileIndex<String, RoviProgramDescriptionLine> descriptionIndex;
    @Mock
    private KeyedFileIndex<String, RoviSeriesLine> seriesIndex;
    @Mock
    private ContentResolver contentResolver;
    
    private ProgramLineBrandExtractor extractor;
    
    @Before
    public void init() throws IOException, IndexAccessException {
        when(descriptionIndex.getLinesForKey(anyString())).thenReturn(RoviTestUtils.descriptions(PROGRAM_ID));
        when(seriesIndex.getLinesForKey(anyString())).thenReturn(series());
        extractor = new ProgramLineBrandExtractor(descriptionIndex, seriesIndex, contentResolver);
    }
    
    private Collection<RoviSeriesLine> series() {
        return Lists.newArrayList(new RoviSeriesLine(PROGRAM_ID, TITLE, SYNOPSIS, ActionType.INSERT));
    }

    @Test
    public void testExtractContent() throws IndexAccessException {
        RoviProgramLine.Builder programLine = RoviProgramLine.builder();
        
        programLine.withShowType(RoviShowType.SM);
        programLine.withProgramId(PROGRAM_ID);
        programLine.withLongTitle("This is a brand");
        programLine.withActionType(ActionType.INSERT);
        
        Brand brand = extractor.extract(programLine.build());
        
        assertEquals(canonicalUriForProgram(PROGRAM_ID), brand.getCanonicalUri());
        assertEquals(LONG_DESCRIPTION, brand.getLongDescription());
        assertEquals(MEDIUM_DESCRIPTION, brand.getMediumDescription());
        assertEquals(SHORT_DESCRIPTION, brand.getShortDescription());
        assertEquals(SYNOPSIS, brand.getDescription());
    }

}
