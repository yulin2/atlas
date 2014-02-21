package org.atlasapi.remotesite.rovi.indexing;

import static org.atlasapi.remotesite.rovi.RoviConstants.FILE_CHARSET;
import static org.atlasapi.remotesite.rovi.RoviTestUtils.fileFromResource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;

import org.atlasapi.remotesite.rovi.RoviPredicates;
import org.atlasapi.remotesite.rovi.indexing.IndexAccessException;
import org.atlasapi.remotesite.rovi.indexing.KeyedFileIndex;
import org.atlasapi.remotesite.rovi.indexing.MapBasedKeyedFileIndexer;
import org.atlasapi.remotesite.rovi.model.RoviEpisodeSequenceLine;
import org.atlasapi.remotesite.rovi.model.RoviSeriesLine;
import org.atlasapi.remotesite.rovi.parsers.RoviEpisodeSequenceLineParser;
import org.atlasapi.remotesite.rovi.parsers.RoviSeriesLineParser;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.io.Resources;


public class MapBasedKeyedFileIndexerTest {
     
    private static final String SMALL_FILE = "org/atlasapi/remotesite/rovi/rovi_series_small.txt";
    private static final String NOT_PARSABLE_FILE = "org/atlasapi/remotesite/rovi/not_parsable_episode_sequence.txt";

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
        assertEquals("Empires of Industry", seriesLine.getFullTitle().get());
        assertEquals("Chronicling the trades and commodities that made America an industrial power.", seriesLine.getSynopsis().get());
    }

    @Test
    public void testIndexingWithPredicate() throws IOException, IndexAccessException {
        URL fileUrl = Resources.getResource(SMALL_FILE);
        File file = new File(fileUrl.getPath());
        MapBasedKeyedFileIndexer<String, RoviSeriesLine> indexer = createIndexer(file);
        
        String key = "20521012";
        KeyedFileIndex<String, RoviSeriesLine> index = indexer.indexWithPredicate(RoviPredicates.IS_INSERT);
        
        Collection<RoviSeriesLine> seriesLines = index.getLinesForKey(key);
        
        assertTrue(seriesLines.isEmpty());
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
    
    @Test(expected=RuntimeException.class) 
    public void testStopProcessIfErrorWhileIndexing() throws IOException {
        File file = fileFromResource(NOT_PARSABLE_FILE);
        
        MapBasedKeyedFileIndexer<String, RoviEpisodeSequenceLine> indexer = new MapBasedKeyedFileIndexer<>(file, FILE_CHARSET, new RoviEpisodeSequenceLineParser());
        indexer.index();
    }
    
    public MapBasedKeyedFileIndexer<String, RoviSeriesLine> createIndexer(File file) {
        RoviSeriesLineParser parser = new RoviSeriesLineParser();
        return new MapBasedKeyedFileIndexer<>(file, FILE_CHARSET, parser);
    }
    
}
