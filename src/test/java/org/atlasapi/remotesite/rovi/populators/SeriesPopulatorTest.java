package org.atlasapi.remotesite.rovi.populators;


import static org.atlasapi.remotesite.rovi.RoviCanonicalUriGenerator.canonicalUriForProgram;
import static org.atlasapi.remotesite.rovi.RoviCanonicalUriGenerator.canonicalUriForSeason;
import static org.atlasapi.remotesite.rovi.RoviCanonicalUriGenerator.canonicalUriForSeasonHistory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Series;
import org.atlasapi.remotesite.rovi.model.ActionType;
import org.atlasapi.remotesite.rovi.model.RoviSeasonHistoryLine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Optional;
import com.google.common.cache.LoadingCache;

@RunWith(MockitoJUnitRunner.class)
public class SeriesPopulatorTest {
    
    private static final String SEASON_ID = "12345";
    private static final String SEASON_HISTORY_ID = "99999";
    private static final String SERIES_ID = "98765";
    private static final int SEASON_NUMBER = 3;
    private static final String SEASON_NAME = "This is a season";

    @Mock
    private LoadingCache<String, Optional<Publisher>> parentPublisherCache;
    
    @Before
    public void init() {
        when(parentPublisherCache.getUnchecked(canonicalUriForProgram(SERIES_ID))).thenReturn(Optional.of(Publisher.ROVI_EN_GB));
    }
    
    @Test
    public void testPopulation() {
        RoviSeasonHistoryLine.Builder season = RoviSeasonHistoryLine.builder();
        
        season.withActionType(ActionType.INSERT);
        season.withSeasonHistoryId(SEASON_HISTORY_ID);
        season.withSeasonNumber(SEASON_NUMBER);
        season.withSeriesId(SERIES_ID);
        season.withSeasonProgramId(SEASON_ID);
        season.withSeasonName(SEASON_NAME);
        
        SeriesPopulator populator = new SeriesPopulator(season.build(), parentPublisherCache);
        
        Series series = new Series();
        populator.populateContent(series);
        
        assertEquals(canonicalUriForSeason(SEASON_ID), series.getCanonicalUri());
        assertEquals(SEASON_NUMBER, series.getSeriesNumber().intValue());
        assertEquals(SEASON_NAME, series.getTitle());
        assertEquals(canonicalUriForProgram(SERIES_ID), series.getParent().getUri());
        
        Set<String> aliasUrls = series.getAliasUrls();
        assertThat(aliasUrls, hasItem(canonicalUriForSeasonHistory(SEASON_HISTORY_ID)));
    }
    
    @Test(expected=RuntimeException.class) 
    public void testPopulationWithDeletionShouldThrowAnException() {
        RoviSeasonHistoryLine.Builder season = RoviSeasonHistoryLine.builder();
        
        season.withActionType(ActionType.DELETE);
        season.withSeasonHistoryId(SEASON_HISTORY_ID);
        
        SeriesPopulator populator = new SeriesPopulator(season.build(), parentPublisherCache);
        populator.populateContent(new Series());        
    }
    
}
