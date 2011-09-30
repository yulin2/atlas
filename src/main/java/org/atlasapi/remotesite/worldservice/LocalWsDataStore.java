package org.atlasapi.remotesite.worldservice;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.atlasapi.remotesite.worldservice.WsDataFile.AUDIO_ITEM;
import static org.atlasapi.remotesite.worldservice.WsDataFile.AUDIO_ITEM_PROG_LINK;
import static org.atlasapi.remotesite.worldservice.WsDataFile.GENRE;
import static org.atlasapi.remotesite.worldservice.WsDataFile.PROGRAMME;
import static org.atlasapi.remotesite.worldservice.WsDataFile.SERIES;
import static org.atlasapi.remotesite.worldservice.WsDataSource.sourceForFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.zip.GZIPInputStream;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import com.metabroadcast.common.base.Maybe;

public class LocalWsDataStore implements WsDataStore {
    
    private static final DateTimeFormatter dayFormat = DateTimeFormat.forPattern("yyyyMMdd");
    
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
        return Maybe.<WsDataSet>just(new FileBackedWsData(file));
    }

    private static class FileBackedWsData implements WsDataSet {

        private final File parent;

        public FileBackedWsData(File parent) {
            this.parent = checkNotNull(parent);
        }
        
        private WsDataSource getFileStream(WsDataFile file) {
            try {
                return sourceForFile(file, new GZIPInputStream(new FileInputStream(new File(parent, file.filename()+".gz"))));
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public String getName() {
            return parent.getName();
        }
        
        @Override
        public WsDataSource getAudioItem() {
            return getFileStream(AUDIO_ITEM);
        }

        @Override
        public WsDataSource getAudioItemProgLink() {
            return getFileStream(AUDIO_ITEM_PROG_LINK);
        }

        @Override
        public WsDataSource getGenre() {
            return getFileStream(GENRE);
        }

        @Override
        public WsDataSource getProgramme() {
            return getFileStream(PROGRAMME);
        }

        @Override
        public WsDataSource getSeries() {
            return getFileStream(SERIES);
        }

    }
}
