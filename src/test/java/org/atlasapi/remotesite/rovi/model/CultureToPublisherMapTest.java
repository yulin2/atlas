package org.atlasapi.remotesite.rovi.model;

import static org.atlasapi.remotesite.rovi.model.CultureToPublisherMap.ENGLISH_NA_CULTURE;
import static org.atlasapi.remotesite.rovi.model.CultureToPublisherMap.ENGLISH_UK_CULTURE;
import static org.atlasapi.remotesite.rovi.model.CultureToPublisherMap.FRENCH_CA_CULTURE;
import static org.atlasapi.remotesite.rovi.model.CultureToPublisherMap.FRENCH_FR_CULTURE;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Set;

import org.atlasapi.remotesite.rovi.model.CultureToPublisherMap;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Sets;


public class CultureToPublisherMapTest {

    @Test
    public void testCulturesOrdering() {
        Set<String> cultures; 
        List<String> sorted;
        
        cultures = Sets.newHashSet(ENGLISH_NA_CULTURE, ENGLISH_UK_CULTURE);
        sorted = CultureToPublisherMap.culturesOrdering().sortedCopy(cultures);
        
        Assert.assertEquals(sorted.get(0), ENGLISH_UK_CULTURE);
        Assert.assertEquals(sorted.get(1), ENGLISH_NA_CULTURE);

        cultures = Sets.newHashSet(ENGLISH_UK_CULTURE, ENGLISH_NA_CULTURE);
        sorted = CultureToPublisherMap.culturesOrdering().sortedCopy(cultures);
        
        Assert.assertEquals(sorted.get(0), ENGLISH_UK_CULTURE);
        Assert.assertEquals(sorted.get(1), ENGLISH_NA_CULTURE);

        cultures = Sets.newHashSet(FRENCH_CA_CULTURE, ENGLISH_NA_CULTURE, ENGLISH_UK_CULTURE, FRENCH_FR_CULTURE, "Spanish Generic");
        sorted = CultureToPublisherMap.culturesOrdering().sortedCopy(cultures);
        
        assertEquals(sorted.get(0), ENGLISH_UK_CULTURE);
        assertEquals(sorted.get(1), ENGLISH_NA_CULTURE);
        assertEquals(sorted.get(2), FRENCH_FR_CULTURE);
        assertEquals(sorted.get(3), FRENCH_CA_CULTURE);
        assertEquals(sorted.get(4), "Spanish Generic");
    }
    
}
