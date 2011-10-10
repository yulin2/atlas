package org.atlasapi.remotesite.worldservice;

import static com.google.common.base.Preconditions.checkArgument;
import static org.atlasapi.remotesite.worldservice.WsDataSource.sourceForFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import com.google.common.io.ByteStreams;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.time.DateTimeZones;

public class LocalWsDataStore implements WritableWsDataStore {
    
    private static final DateTimeFormatter dayFormat = DateTimeFormat.forPattern("yyyyMMdd").withZone(DateTimeZones.UTC);
    
    private static final FilenameFilter dayFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            try {
                dayFormat.parseDateTime(name);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    };

    private File dataDir;

    public LocalWsDataStore(File dataDir) {
        checkArgument(dataDir != null && dataDir.exists() && dataDir.isDirectory(), "Invalid data directory");
        this.dataDir = dataDir;
    }
    
    @Override
    public Maybe<WsDataSet> latestData() {
        return dataForFile(Ordering.natural().max(ImmutableSet.copyOf(dataDir.listFiles(dayFilter))));
    }

    @Override
    public Maybe<WsDataSet> dataForDay(DateTime day) {
        return dataForFile(new File(dataDir, dayFormat.print(day)));
    }

    private Maybe<WsDataSet> dataForFile(File file) {
        if(file == null || !file.isDirectory()) {
            return Maybe.nothing();
        }
        return Maybe.<WsDataSet>just(new FileBackedWsData(file, dayFormat.parseDateTime(file.getName())));
    }

    private static class FileBackedWsData implements WsDataSet {

        private final File parent;
        private final DateTime version;

        public FileBackedWsData(File parent, DateTime version) {
            this.parent = parent;
            this.version = version;
        }

        @Override
        public DateTime getVersion() {
            return version;
        }
        
        @Override
        public WsDataSource getDataForFile(WsDataFile file) {
            try {
                return sourceForFile(file, new FileInputStream(new File(parent, file.filename(".xml.gz"))));
            } catch (Exception e) {
                return null;
            }
        }

    }

    @Override
    public WsDataSet write(WsDataSet data) throws IOException {
        File setDir = new File(dataDir, dayFormat.print(data.getVersion()));
        setDir.mkdir();
        for (WsDataFile file : WsDataFile.values()) {
            WsDataSource fileData = data.getDataForFile(file);
            File outputFile = new File(setDir, file.filename(".xml.gz"));
            outputFile.createNewFile();
            ByteStreams.copy(fileData.data(), new FileOutputStream(outputFile)); 
        }
        
        return new FileBackedWsData(setDir, data.getVersion());
    }
    
}
