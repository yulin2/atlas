package org.atlasapi.remotesite.worldservice;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import junit.framework.TestCase;

import org.joda.time.DateTime;

import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.time.DateTimeZones;

public class LocalWsDataStoreTest extends TestCase {

    public void testReadingAndWritingData() throws IOException {
        
        File parentDir = Files.createTempDir();
        parentDir.deleteOnExit();
        
        WritableWsDataStore store = new LocalWsDataStore(parentDir);
        
        final DateTime day = new DateTime(DateTimeZones.UTC).withTime(0, 0, 0, 0);
        
        WsDataSet simpleDataSet = simpleDataSetForDay(day);
        
        store.write(simpleDataSet);
        
        File setDir = new File(parentDir, day.toString("yyyyMMdd"));
        assertTrue(setDir.exists());
        
        for (WsDataFile file : WsDataFile.values()) {
            assertTrue(new File(setDir, file.filename(".xml.gz")).exists());
        }
        
        equivalent(simpleDataSetForDay(day), store.latestData());
        equivalent(simpleDataSet, store.dataForDay(day));
    }

    private void equivalent(WsDataSet simpleDataSet, Maybe<WsDataSet> possibleLatestData) throws IOException {
        assertTrue(possibleLatestData.hasValue());
        
        WsDataSet latestData = possibleLatestData.requireValue();
        
        assertEquals(simpleDataSet.getVersion(), latestData.getVersion());
        
        for (WsDataFile file : WsDataFile.values()) {
            assertEquals(readToString(simpleDataSet.getDataForFile(file)), readToString(latestData.getDataForFile(file)));
        }
    }

    private String readToString(WsDataSource source) throws IOException {
        return CharStreams.toString(new InputStreamReader(source.data()));
    }

    private WsDataSet simpleDataSetForDay(final DateTime day) {
        return new WsDataSet() {
            
            @Override
            public DateTime getVersion() {
                return day;
            }
            
            @Override
            public WsDataSource getDataForFile(WsDataFile file) {
                return new WsDataSource(file, new ByteArrayInputStream(file.filename(".xml.gz").getBytes()));
            }
        };
    }

}
