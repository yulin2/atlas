package org.atlasapi.remotesite.rovi;

import static org.atlasapi.remotesite.rovi.RoviUtils.canonicalUriForProgram;
import static org.atlasapi.remotesite.rovi.RoviUtils.canonicalUriForSeason;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyString;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.ParentRef;
import org.atlasapi.media.entity.Series;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.remotesite.rovi.program.ProgramLineEpisodeExtractor;
import org.atlasapi.remotesite.rovi.program.RoviProgramDescriptionLine;
import org.atlasapi.remotesite.rovi.program.RoviProgramLine;
import org.atlasapi.remotesite.rovi.series.RoviEpisodeSequenceLine;
import org.atlasapi.remotesite.rovi.series.RoviSeasonHistoryLine;
import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class ProgramLineEpisodeExtractorTest {
    
    private static final String EPISODE_ID = "12345";
    private static final String SERIES_ID = "23456";
    private static final String SEASON_ID = "34567";
    private static final String EPISODE_NUMBER = "10";
    private static final String EPISODE_TITLE = "This is the episode title";
    private static final String EPISODE_SEQUENCE_TITLE = "This is the episode title inside the episode sequence";
    private static final Integer EPISODE_SEQUENCE_NUMBER = 15;
    private static final Duration EPISODE_DURATION = Duration.standardMinutes(60);
    private static final Integer SEASON_NUMBER = 3;

    @Mock
    private KeyedFileIndex<String, RoviProgramDescriptionLine> descriptionIndex;
    @Mock
    private KeyedFileIndex<String, RoviEpisodeSequenceLine> episodeSequenceIndex;
    @Mock
    private KeyedFileIndex<String, RoviSeasonHistoryLine> seasonHistoryIndex;
    @Mock
    private ContentResolver contentResolver;
    
    private ProgramLineEpisodeExtractor extractor;
    
    @Before
    public void init() throws IOException {
        Mockito.reset(descriptionIndex, episodeSequenceIndex, seasonHistoryIndex, contentResolver);
        when(descriptionIndex.getLinesForKey(anyString())).thenReturn(RoviTestUtils.descriptions(EPISODE_ID));
        
        extractor = new ProgramLineEpisodeExtractor(
                descriptionIndex,
                episodeSequenceIndex,
                seasonHistoryIndex,
                contentResolver);
    }
    
    @Test
    public void testExtractionWithEpisodeSequencePresent() throws IOException {
        when(episodeSequenceIndex.getLinesForKey(EPISODE_ID)).thenReturn(episodeSequence());
        
        Episode extracted = extractor.extract(episode());
        
        assertEquals(EPISODE_SEQUENCE_TITLE, extracted.getTitle());
        assertEquals(EPISODE_SEQUENCE_NUMBER, extracted.getEpisodeNumber());    
        assertEquals(SEASON_NUMBER, extracted.getSeriesNumber());
        verify(seasonHistoryIndex, never()).getLinesForKey(anyString());
        
        checkBaseEpisodeData(extracted);
    }
    
    @Test
    public void testExtractionWithEpisodeSequenceNotPresent() throws IOException {
        when(episodeSequenceIndex.getLinesForKey(EPISODE_ID)).thenReturn(episodeSequenceNotFound());
        when(seasonHistoryIndex.getLinesForKey(SEASON_ID)).thenReturn(seasonHistory());
        
        Episode extracted = extractor.extract(episode());
        
        assertEquals(EPISODE_TITLE, extracted.getTitle());
        assertEquals(Integer.valueOf(EPISODE_NUMBER), extracted.getEpisodeNumber());
        assertEquals(SEASON_NUMBER, extracted.getSeriesNumber());
        verify(seasonHistoryIndex, times(1)).getLinesForKey(SEASON_ID);

        checkBaseEpisodeData(extracted);
    }

    private void checkBaseEpisodeData(Episode extracted) {
        assertEquals(canonicalUriForProgram(EPISODE_ID), extracted.getCanonicalUri());
        assertEquals(ParentRef.parentRefFrom(brand()), extracted.getContainer());
        assertEquals(ParentRef.parentRefFrom(series()), extracted.getSeriesRef());
        
        Integer extractedVersionDuration = extracted.getVersions().iterator().next().getDuration();
        assertEquals(Integer.valueOf((int) EPISODE_DURATION.getStandardSeconds()), extractedVersionDuration);
    }
    
    private RoviProgramLine episode() {
        RoviProgramLine.Builder builder = RoviProgramLine.builder();
        
        builder.withShowType(RoviShowType.SE);
        builder.withProgramId(EPISODE_ID);
        builder.withLongTitle("This is the program title");
        builder.withEpisodeNumber(EPISODE_NUMBER);
        builder.withEpisodeTitle(EPISODE_TITLE);
        builder.withSeriesId(SERIES_ID);
        builder.withSeasonId(SEASON_ID);
        builder.withDuration(EPISODE_DURATION);
        
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

    private Collection<RoviEpisodeSequenceLine> episodeSequence() {
        RoviEpisodeSequenceLine.Builder builder = RoviEpisodeSequenceLine.builder();
        
        builder.withSeriesId(SERIES_ID);
        builder.withSeasonProgramId(SEASON_ID);
        builder.withEpisodeTitle(EPISODE_SEQUENCE_TITLE);
        builder.withEpisodeSeasonSequence(EPISODE_SEQUENCE_NUMBER);
        builder.withEpisodeSeasonNumber(SEASON_NUMBER);
        builder.withProgramId(EPISODE_ID);
        
        return ImmutableList.of(builder.build());
    }
    
    private Collection<RoviSeasonHistoryLine> seasonHistory() {
        RoviSeasonHistoryLine.Builder builder = RoviSeasonHistoryLine.builder();
        
        builder.withSeasonHistoryId("999999");
        builder.withSeasonName("Season name");
        builder.withSeasonProgramId(SEASON_ID);
        builder.withSeasonNumber(SEASON_NUMBER);
        builder.withSeriesId(SERIES_ID);
        
        return ImmutableList.of(builder.build());
    }

    private Collection<RoviEpisodeSequenceLine> episodeSequenceNotFound() {
        List<RoviEpisodeSequenceLine> resultCollection = Lists.newArrayList();
        return resultCollection;
    }
    
}
