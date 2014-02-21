package org.atlasapi.remotesite.rovi;

import static org.atlasapi.remotesite.rovi.RoviPredicates.IS_INSERT;

import java.io.File;

import javax.annotation.PostConstruct;

import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.remotesite.rovi.program.RoviProgramDescriptionLine;
import org.atlasapi.remotesite.rovi.program.RoviProgramDescriptionLineParser;
import org.atlasapi.remotesite.rovi.schedule.ItemBroadcastUpdater;
import org.atlasapi.remotesite.rovi.schedule.ScheduleFileProcessor;
import org.atlasapi.remotesite.rovi.schedule.ScheduleLineBroadcastExtractor;
import org.atlasapi.remotesite.rovi.series.RoviEpisodeSequenceLine;
import org.atlasapi.remotesite.rovi.series.RoviEpisodeSequenceLineParser;
import org.atlasapi.remotesite.rovi.series.RoviSeasonHistoryLine;
import org.atlasapi.remotesite.rovi.series.RoviSeasonHistoryLineParser;
import org.atlasapi.remotesite.rovi.series.RoviSeriesLine;
import org.atlasapi.remotesite.rovi.series.RoviSeriesLineParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;

@Configuration
public class RoviModule {

    private static final String PROGRAMS_FILE = "/data/rovi/Program.txt";
    private static final String PROGRAM_DESCRIPTION = "/data/rovi/Program_Description.txt";
    private static final String EPISODE_SEQUENCE = "/data/rovi/Episode_Sequence.txt";
    private static final String SEASON_HISTORY_SEQUENCE = "/data/rovi/Season_History.txt";
    private static final String SERIES = "/data/rovi/Series.txt";
    private static final String SCHEDULE_FILE = "/data/rovi/Schedule.txt";

    private @Autowired SimpleScheduler scheduler;
    private @Autowired ContentWriter contentWriter;
    private @Autowired ContentResolver contentResolver;
    private @Autowired ChannelResolver channelResolver;
    
    @Bean
    public MapBasedKeyedFileIndexer<String, RoviProgramDescriptionLine> descriptionsIndexer() {
        return new MapBasedKeyedFileIndexer<>(
                new File(PROGRAM_DESCRIPTION),
                RoviConstants.FILE_CHARSET,
                new RoviProgramDescriptionLineParser());
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
    public RoviContentWriter roviContentWriter() {
        return new RoviContentWriter(contentWriter);
    }
    
    @Bean
    public RoviProgramsProcessor programsProcessor() {
        return new RoviProgramsProcessor(
                descriptionsIndexer(),
                episodeSequenceIndexer(),
                seriesIndexer(),
                roviContentWriter(),
                contentResolver,
                scheduleProcessor(),
                IS_INSERT);
    }
    
    @Bean
    public RoviFullIngestProcessor fullIngestProcessor() {
        return new RoviFullIngestProcessor(
                descriptionsIndexer(),
                episodeSequenceIndexer(),
                roviContentWriter(),
                contentResolver,
                scheduleProcessor(),
                auxCacheSupplier());
    }
    
    @Bean
    public AuxiliaryCacheSupplier auxCacheSupplier() {
        return new AuxiliaryCacheSupplier(contentResolver);
    }
    
    @Bean
    public RoviUpdater roviUpdater() {
        return new RoviUpdater(programsProcessor(), new File(PROGRAMS_FILE), new File(
                SEASON_HISTORY_SEQUENCE), new File(SCHEDULE_FILE));
    }
    
    @Bean RoviFullIngestTask roviFullIngestTask() {
        return new RoviFullIngestTask(fullIngestProcessor(), new File(PROGRAMS_FILE), new File(
                SEASON_HISTORY_SEQUENCE),  new File(SCHEDULE_FILE));
    }
    
    @Bean
    public ScheduleFileProcessor scheduleProcessor() {
        return new ScheduleFileProcessor(
                new ItemBroadcastUpdater(contentResolver, contentWriter),
                new ScheduleLineBroadcastExtractor(channelResolver));
    }
    
    @PostConstruct
    public void init() {
        scheduler.schedule(roviUpdater(), RepetitionRules.NEVER);
        scheduler.schedule(roviFullIngestTask(), RepetitionRules.NEVER);
    }

}
