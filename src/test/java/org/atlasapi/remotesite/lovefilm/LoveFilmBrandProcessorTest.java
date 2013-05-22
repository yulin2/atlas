package org.atlasapi.remotesite.lovefilm;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.google.common.io.CharStreams;
import com.metabroadcast.common.scheduling.ScheduledTask;


public class LoveFilmBrandProcessorTest {

    private LoveFilmFileStore store = mock(LoveFilmFileStore.class);
    private LoveFilmDataRowHandler dataHandler = mock(LoveFilmDataRowHandler.class);
    private LoveFilmFileUpdater updater = mock(LoveFilmFileUpdater.class);
    
    private final LoveFilmBrandProcessor brandProcessor = new DefaultLoveFilmBrandProcessor();
    
    private ScheduledTask task = new LoveFilmCsvUpdateTask(updater, store, dataHandler, brandProcessor);
    
    @Test
    public void testDetectsBrandSeriesEpisode() {
        LoveFilmData data = new LoveFilmData(CharStreams.newReaderSupplier(
                "\"sku\",\"series_id\",\"show_id\",\"item_name\",\"entity\",\"access_method\",\"item_type_keyword\"\n" +
        		"\"brand\",\"\",\"\",\"a brand\",\"show\",\"\",\"\"\n" +
        		"\"series1\",\"\",\"brand\",\"series 1 title\",\"season\",\"\",\"\"\n" +
        		"\"series2\",\"\",\"brand\",\"series 2 title\",\"season\",\"\",\"\"\n" +
        		"\"episode1\",\"series1\",\"brand\",\"episode 1 title\",\"episode\",\"vod\",\"television-video-recordings\""));
        when(store.fetchLatestData()).thenReturn(data);
        
        task.run();
        
        assertEquals(BrandType.BRAND_SERIES_EPISODE, brandProcessor.getBrandType("http://lovefilm.com/shows/brand"));
    }

    @Test
    public void testDetectsTopLevelSeries() {
        LoveFilmData data = new LoveFilmData(CharStreams.newReaderSupplier(
                "\"sku\",\"series_id\",\"show_id\",\"item_name\",\"entity\",\"access_method\",\"item_type_keyword\"\n" +
                "\"brand\",\"\",\"\",\"shared title\",\"show\",\"\",\"\"\n" +
                "\"series\",\"\",\"brand\",\"shared title\",\"season\",\"\",\"\"\n" +
                "\"episode1\",\"series\",\"brand\",\"episode 1 title\",\"episode\",\"vod\",\"television-video-recordings\"\n" +
                "\"episode2\",\"series\",\"brand\",\"episode 2 title\",\"episode\",\"vod\",\"television-video-recordings\""));
        when(store.fetchLatestData()).thenReturn(data);
        
        task.run();
        
        assertEquals(BrandType.TOP_LEVEL_SERIES, brandProcessor.getBrandType("http://lovefilm.com/shows/brand"));
    }

    @Test
    public void testDetectsBrandSeriesEpisodeWithSingleSeries() {
        LoveFilmData data = new LoveFilmData(CharStreams.newReaderSupplier(
                "\"sku\",\"series_id\",\"show_id\",\"item_name\",\"entity\",\"access_method\",\"item_type_keyword\"\n" +
                "\"brand\",\"\",\"\",\"brand title\",\"show\",\"\",\"\"\n" +
                "\"series\",\"\",\"brand\",\"series title\",\"season\",\"\",\"\"\n" +
                "\"episode1\",\"series\",\"brand\",\"episode 1 title\",\"episode\",\"vod\",\"television-video-recordings\"\n" +
                "\"episode2\",\"series\",\"brand\",\"episode 2 title\",\"episode\",\"vod\",\"television-video-recordings\""));
        when(store.fetchLatestData()).thenReturn(data);
        
        task.run();
        
        assertEquals(BrandType.BRAND_SERIES_EPISODE, brandProcessor.getBrandType("http://lovefilm.com/shows/brand"));
    }

    @Test
    public void testDetectsStandAloneEpisode() {
        LoveFilmData data = new LoveFilmData(CharStreams.newReaderSupplier(
                "\"sku\",\"series_id\",\"show_id\",\"item_name\",\"entity\",\"access_method\",\"item_type_keyword\"\n" +
                "\"brand\",\"\",\"\",\"shared title\",\"show\",\"\",\"\"\n" +
                "\"series\",\"\",\"brand\",\"shared title\",\"season\",\"\",\"\"\n" +
                "\"episode\",\"series\",\"brand\",\"shared title\",\"episode\",\"vod\",\"television-video-recordings\""));
        when(store.fetchLatestData()).thenReturn(data);
        
        task.run();
        
        assertEquals(BrandType.TOP_LEVEL_SERIES, brandProcessor.getBrandType("http://lovefilm.com/shows/brand"));
    }

    @Test
    public void testDetectsBrandSeriesEpisodeWithSingleSeriesAndSingleEpisode() {
        LoveFilmData data = new LoveFilmData(CharStreams.newReaderSupplier(
                "\"sku\",\"series_id\",\"show_id\",\"item_name\",\"entity\",\"access_method\",\"item_type_keyword\"\n" +
                "\"brand\",\"\",\"\",\"brand title\",\"show\",\"\",\"\"\n" +
                "\"series\",\"\",\"brand\",\"series title\",\"season\",\"\",\"\"\n" +
                "\"episode\",\"series\",\"brand\",\"episode title\",\"episode\",\"vod\",\"television-video-recordings\""));
        when(store.fetchLatestData()).thenReturn(data);
        
        task.run();
        
        assertEquals(BrandType.BRAND_SERIES_EPISODE, brandProcessor.getBrandType("http://lovefilm.com/shows/brand"));
    }

    @Test
    public void testDetectsTopLevelSeriesWithSingleEpisode() {
        LoveFilmData data = new LoveFilmData(CharStreams.newReaderSupplier(
                "\"sku\",\"series_id\",\"show_id\",\"item_name\",\"entity\",\"access_method\",\"item_type_keyword\"\n" +
                "\"brand\",\"\",\"\",\"shared title\",\"show\",\"\",\"\"\n" +
                "\"series\",\"\",\"brand\",\"shared title\",\"season\",\"\",\"\"\n" +
                "\"episode\",\"series\",\"brand\",\"episode title\",\"episode\",\"vod\",\"television-video-recordings\""));
        when(store.fetchLatestData()).thenReturn(data);
        
        task.run();
        
        assertEquals(BrandType.TOP_LEVEL_SERIES, brandProcessor.getBrandType("http://lovefilm.com/shows/brand"));
    }
}
