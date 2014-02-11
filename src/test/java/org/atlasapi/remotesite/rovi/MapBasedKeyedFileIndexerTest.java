package org.atlasapi.remotesite.rovi;

import static org.atlasapi.remotesite.rovi.RoviConstants.FILE_CHARSET;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;

import org.atlasapi.remotesite.rovi.series.RoviSeriesLine;
import org.atlasapi.remotesite.rovi.series.RoviSeriesLineParser;
import org.junit.Test;

import com.google.common.io.Resources;


public class MapBasedKeyedFileIndexerTest {
     
    private static final String SMALL_FILE = "org/atlasapi/remotesite/rovi/rovi_series_small.txt";

    @Test
    public void testIndexing() throws IOException, IndexAccessException {
        URL fileUrl = Resources.getResource(SMALL_FILE);
        File file = new File(fileUrl.getPath());
        MapBasedKeyedFileIndexer<String, RoviSeriesLine> indexer = createIndexer(file);

        String key = "919489";
        KeyedFileIndex<String, RoviSeriesLine> index = indexer.index();
        
        Collection<RoviSeriesLine> seriesLines = index.getLinesForKey(key);
        
        assertEquals(1, seriesLines.size());
        
        RoviSeriesLine seriesLine = seriesLines.iterator().next();
        
        assertEquals(key, seriesLine.getKey());
        assertEquals(key, seriesLine.getSeriesId());
        assertEquals("Empires of Industry", seriesLine.getFullTitle());
        assertEquals("Chronicling the trades and commodities that made America an industrial power.", seriesLine.getSynopsis().get());
    }

    @Test
    public void testIndexingFirstLine() throws IOException, IndexAccessException {
        URL fileUrl = Resources.getResource(SMALL_FILE);
        File file = new File(fileUrl.getPath());
        MapBasedKeyedFileIndexer<String, RoviSeriesLine> indexer = createIndexer(file);
        
        String key = "99";
        KeyedFileIndex<String, RoviSeriesLine> index = indexer.index();
        
        Collection<RoviSeriesLine> lines = index.getLinesForKey(key);
        
        assertEquals(1, lines.size());
    }
    
    public MapBasedKeyedFileIndexer<String, RoviSeriesLine> createIndexer(File file) {
        RoviSeriesLineParser parser = new RoviSeriesLineParser();
        return new MapBasedKeyedFileIndexer<>(file, FILE_CHARSET, parser);
    }
    
}
