package org.atlasapi.remotesite.rovi;

import java.io.File;

import javax.annotation.PostConstruct;

import org.atlasapi.remotesite.rovi.program.RoviProgramDescriptionLine;
import org.atlasapi.remotesite.rovi.program.RoviProgramDescriptionLineParser;
import org.atlasapi.remotesite.rovi.program.RoviReleaseDatesLine;
import org.atlasapi.remotesite.rovi.program.RoviReleaseDatesLineParser;
import org.atlasapi.remotesite.rovi.series.RoviEpisodeSequenceLine;
import org.atlasapi.remotesite.rovi.series.RoviEpisodeSequenceLineParser;
import org.atlasapi.remotesite.rovi.series.RoviSeasonHistoryLine;
import org.atlasapi.remotesite.rovi.series.RoviSeasonHistoryLineParser;
import org.atlasapi.remotesite.rovi.series.RoviSeriesLine;
import org.atlasapi.remotesite.rovi.series.RoviSeriesLineParser;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;

@Configuration
public class RoviModule {

    private static final String PROGRAMS_FILE = "/Users/max/Documents/Rovi/Programs_20140115_Full/Program.txt";
    private static final String PROGRAM_DESCRIPTION = "/Users/max/Documents/Rovi/Programs_20140115_Full/Program_Description.txt";
    private static final String RELEASE_DATES = "/Users/max/Documents/Rovi/Programs_20140115_Full/Program_Release_Date.txt";
    private static final String EPISODE_SEQUENCE = "/Users/max/Documents/Rovi/Series_20140115_Full 2/Episode_Sequence.txt";
    private static final String SEASON_HISTORY_SEQUENCE = "/Users/max/Documents/Rovi/Series_20140115_Full 2/Season_History.txt";
    private static final String SERIES = "/Users/max/Documents/Rovi/Series_20140115_Full 2/Series.txt";

    private @Autowired SimpleScheduler scheduler;
    
    @Bean
    public MapBasedKeyedFileIndexer<String, RoviProgramDescriptionLine> descriptionsIndexer() {
        return new MapBasedKeyedFileIndexer<>(
                new File(PROGRAM_DESCRIPTION),
                RoviConstants.FILE_CHARSET,
                new RoviProgramDescriptionLineParser());
    }

    @Bean
    public MapBasedKeyedFileIndexer<String, RoviReleaseDatesLine> releaseDatesIndexer() {
        return new MapBasedKeyedFileIndexer<>(
                new File(RELEASE_DATES),
                RoviConstants.FILE_CHARSET,
                new RoviReleaseDatesLineParser());
    }

    @Bean
    public MapBasedKeyedFileIndexer<String, RoviEpisodeSequenceLine> episodeSequenceIndexer() {
        return new MapBasedKeyedFileIndexer<>(
                new File(EPISODE_SEQUENCE),
                RoviConstants.FILE_CHARSET,
                new RoviEpisodeSequenceLineParser());
    }

    @Bean
    public MapBasedKeyedFileIndexer<String, RoviSeasonHistoryLine> seasonHistoryIndexer() {
        return new MapBasedKeyedFileIndexer<>(
                new File(SEASON_HISTORY_SEQUENCE),
                RoviConstants.FILE_CHARSET,
                new RoviSeasonHistoryLineParser());
    }

    @Bean
    public MapBasedKeyedFileIndexer<String, RoviSeriesLine> seriesIndexer() {
        return new MapBasedKeyedFileIndexer<>(
                new File(SERIES),
                RoviConstants.FILE_CHARSET,
                new RoviSeriesLineParser());
    }
    
    @Bean
    public RoviProgramsProcessor programsProcessor() {
        return new RoviProgramsProcessor(
                descriptionsIndexer(),
                releaseDatesIndexer(),
                episodeSequenceIndexer(),
                seasonHistoryIndexer(),
                seriesIndexer());
    }
    
    @Bean
    public RoviUpdater roviUpdater() {
        return new RoviUpdater(programsProcessor(), new File(PROGRAMS_FILE));
    }
    
    @PostConstruct
    public void init() {
        // Starts processing one minute from now
        scheduler.schedule(roviUpdater(), RepetitionRules.daily(LocalTime.now().plusMinutes(1)));
    }

}
