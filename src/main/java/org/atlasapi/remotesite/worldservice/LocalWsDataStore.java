package org.atlasapi.remotesite.worldservice;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.atlasapi.remotesite.worldservice.WsDataFile.AUDIO_ITEM;
import static org.atlasapi.remotesite.worldservice.WsDataFile.AUDIO_ITEM_PROG_LINK;
import static org.atlasapi.remotesite.worldservice.WsDataFile.GENRE;
import static org.atlasapi.remotesite.worldservice.WsDataFile.PROGRAMME;
import static org.atlasapi.remotesite.worldservice.WsDataFile.SERIES;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
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
    public Maybe<WsData> latestData() {
        return dataForFile(Ordering.natural().max(ImmutableSet.copyOf(dataDir.listFiles(dayFilter))));
    }

    @Override
    public Maybe<WsData> dataForDay(DateTime day) {
        return dataForFile(new File(dataDir, dayFormat.print(day)));
    }

    private Maybe<WsData> dataForFile(File file) {
        if(file == null || !file.isDirectory()) {
            return Maybe.nothing();
        }
        return Maybe.<WsData>just(new FileBackedWsData(file));
    }

    private static class FileBackedWsData implements WsData {

        private final File parent;

        public FileBackedWsData(File parent) {
            this.parent = checkNotNull(parent);
        }
        
        private InputStream getFileStream(WsDataFile file) {
            try {
                return new GZIPInputStream(new FileInputStream(new File(parent, file.filename()+".gz")));
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public InputStream getAudioItem() {
            return getFileStream(AUDIO_ITEM);
        }

        @Override
        public InputStream getAudioItemProgLink() {
            return getFileStream(AUDIO_ITEM_PROG_LINK);
        }

        @Override
        public InputStream getGenre() {
            return getFileStream(GENRE);
        }

        @Override
        public InputStream getProgramme() {
            return getFileStream(PROGRAMME);
        }

        @Override
        public InputStream getSeries() {
            return getFileStream(SERIES);
        }

    }
}
