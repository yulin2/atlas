package org.atlasapi.remotesite.rovi.populators;

import static org.atlasapi.remotesite.rovi.RoviCanonicalUriGenerator.canonicalUriForProgram;
import static org.atlasapi.remotesite.rovi.RoviCanonicalUriGenerator.canonicalUriForSeason;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.ParentRef;
import org.atlasapi.media.entity.Series;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.remotesite.rovi.RoviTestUtils;
import org.atlasapi.remotesite.rovi.indexing.IndexAccessException;
import org.atlasapi.remotesite.rovi.model.ActionType;
import org.atlasapi.remotesite.rovi.model.RoviEpisodeSequenceLine;
import org.atlasapi.remotesite.rovi.model.RoviProgramLine;
import org.atlasapi.remotesite.rovi.model.RoviShowType;
import org.atlasapi.remotesite.rovi.populators.EpisodePopulator;
import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Optional;
import com.google.common.cache.LoadingCache;

@RunWith(MockitoJUnitRunner.class)
public class EpisodePopulatorTest {

    private static final String EPISODE_ID = "12345";
    private static final String SERIES_ID = "23456";
    private static final String SEASON_ID = "34567";
    private static final String EPISODE_TITLE = "This is the episode title";
    private static final String EPISODE_SEQUENCE_TITLE = "This is the episode title inside the episode sequence";
    private static final Integer EPISODE_SEQUENCE_NUMBER = 15;
    private static final Duration EPISODE_DURATION = Duration.standardMinutes(60);
    private static final Integer SEASON_NUMBER = 3;

    @Mock
    private ContentResolver contentResolver;
    
    @Mock
    private LoadingCache<String, Optional<Integer>> seasonNumberCache;
    
    @Before
    public void init() throws IOException, IndexAccessException {
        when(seasonNumberCache.getUnchecked(SEASON_ID)).thenReturn(Optional.of(SEASON_NUMBER));
    }
    
    @Test
    public void testExtractionWithEpisodeSequencePresent() throws IOException, IndexAccessException {
        EpisodePopulator populator = new EpisodePopulator(
                Optional.of(episode()),
                RoviTestUtils.descriptions(EPISODE_ID),
                contentResolver,
                episodeSequence(),
                seasonNumberCache);
        
        Episode episode = new Episode();
        populator.populateContent(episode);
        
        assertEquals(EPISODE_SEQUENCE_TITLE, episode.getTitle());
        assertEquals(EPISODE_SEQUENCE_NUMBER, episode.getEpisodeNumber());    
        assertEquals(SEASON_NUMBER, episode.getSeriesNumber());
        
        checkBaseEpisodeData(episode);
    }
    
    @Test
    public void testExtractionWithEpisodeSequenceNotPresent() throws IOException, IndexAccessException {
        EpisodePopulator populator = new EpisodePopulator(
                Optional.of(episode()),
                RoviTestUtils.descriptions(EPISODE_ID),
                contentResolver,
                Optional.<RoviEpisodeSequenceLine>absent(),
                seasonNumberCache);
        
        Episode episode = new Episode();
        populator.populateContent(episode);
        
        assertEquals(EPISODE_TITLE, episode.getTitle());
        assertEquals(SEASON_NUMBER, episode.getSeriesNumber());

        checkBaseEpisodeData(episode);
    }

    private void checkBaseEpisodeData(Episode extracted) {
        assertEquals(canonicalUriForProgram(EPISODE_ID), extracted.getCanonicalUri());
        assertEquals(ParentRef.parentRefFrom(brand()), extracted.getContainer());
        assertEquals(ParentRef.parentRefFrom(series()), extracted.getSeriesRef());
        
        Integer extractedVersionDuration = extracted.getVersions().iterator().next().getDuration();
        assertEquals(Integer.valueOf((int) EPISODE_DURATION.getStandardSeconds()), extractedVersionDuration);
        
        verify(seasonNumberCache, times(1)).getUnchecked(SEASON_ID);
    }
    
    private RoviProgramLine episode() {
        RoviProgramLine.Builder builder = RoviProgramLine.builder();
        
        builder.withShowType(RoviShowType.SERIES_EPISODE);
        builder.withProgramId(EPISODE_ID);
        builder.withLongTitle("This is the program title");
        builder.withEpisodeTitle(EPISODE_TITLE);
        builder.withSeriesId(SERIES_ID);
        builder.withSeasonId(SEASON_ID);
        builder.withDuration(EPISODE_DURATION);
        builder.withActionType(ActionType.INSERT);
        
        return builder.build();
    }
    
    private Brand brand() {
        Brand brand = new Brand();
        brand.setCanonicalUri(canonicalUriForProgram(SERIES_ID));
        return brand;
    }

    private Series series() {
        Series series = new Series();
        series.setCanonicalUri(canonicalUriForSeason(SEASON_ID));
        return series;
    }

    private Optional<RoviEpisodeSequenceLine> episodeSequence() {
        RoviEpisodeSequenceLine.Builder builder = RoviEpisodeSequenceLine.builder();
        
        builder.withSeriesId(SERIES_ID);
        builder.withSeasonProgramId(SEASON_ID);
        builder.withEpisodeTitle(EPISODE_SEQUENCE_TITLE);
        builder.withEpisodeSeasonSequence(EPISODE_SEQUENCE_NUMBER);
        builder.withEpisodeSeasonNumber(SEASON_NUMBER);
        builder.withProgramId(EPISODE_ID);
        builder.withActionType(ActionType.INSERT);
        
        return Optional.of(builder.build());
    }
    
}
