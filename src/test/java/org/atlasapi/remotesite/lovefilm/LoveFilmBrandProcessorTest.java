package org.atlasapi.remotesite.lovefilm;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.google.common.io.CharStreams;


public class LoveFilmBrandProcessorTest {

    private LoveFilmFileStore store = mock(LoveFilmFileStore.class);
    
    private final LoveFilmBrandProcessor brandProcessor = mock(LoveFilmBrandProcessor.class);
    
    @Test
    public void testDetectsTopLevelSeries() {
        LoveFilmData data = new LoveFilmData(CharStreams.newReaderSupplier("\"header\",\"row\"\n\"value\",\"row\""));
        when(store.fetchLatestData()).thenReturn(data);
        
        
    }

}
