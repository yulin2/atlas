package org.atlasapi.remotesite.itunes.epf;

import static org.atlasapi.remotesite.itunes.epf.ArtistTable.ArtistTableColumn.ARTIST_ID;
import static org.atlasapi.remotesite.itunes.epf.ArtistTable.ArtistTableColumn.ARTIST_TYPE_ID;
import static org.atlasapi.remotesite.itunes.epf.ArtistTable.ArtistTableColumn.EXPORT_DATE;
import static org.atlasapi.remotesite.itunes.epf.ArtistTable.ArtistTableColumn.IS_ACTUAL_ARTIST;
import static org.atlasapi.remotesite.itunes.epf.ArtistTable.ArtistTableColumn.NAME;
import static org.atlasapi.remotesite.itunes.epf.ArtistTable.ArtistTableColumn.VIEW_URL;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import com.google.common.io.LineProcessor;
import com.metabroadcast.common.time.DateTimeZones;

public class ArtistTable {

    public enum ArtistTableColumn {
        EXPORT_DATE,
        ARTIST_ID,
        NAME,
        IS_ACTUAL_ARTIST,
        VIEW_URL,
        ARTIST_TYPE_ID;

        public String extractValue(List<String> lineParts) {
            return lineParts.get(ordinal());
        }
        
    };
    
    public enum ArtistType {
        ARTIST("1"),
        TV_SHOW("2"),
        STUDIO("3"),
        PODCAST_ARTIST("4"),
        AUTHOR("5"),
        SOFTWARE_ARTIST("7"),
        ITUNES_U_ARTIST("8"),
        MOVIE_ARTIST("6");
        
        private final String id;

        ArtistType(String id) {
            this.id = id;
        }
        
        private static final Function<ArtistType, String> TO_ID = new Function<ArtistType, String>() {
            @Override
            public String apply(ArtistType input) {
                return input.getId();
            }
        };
        private static final Map<String, ArtistType> idMap = Maps.uniqueIndex(ImmutableList.copyOf(values()), TO_ID);

        public static ArtistType artistTypeForId(String id) {
            return idMap.get(id);
        }

        public String getId() {
            return id;
        }
    };
    
    public final static class ArtistTableRow {
        
        private final DateTime exportDate;
        private final String artistId;
        private final String name;
        private final Boolean isActualArtist;
        private final String viewUrl;
        private final ArtistType artistTypeId;
        
        public ArtistTableRow(DateTime exportDate, String artistId, String name, Boolean isActualArtist, String viewUrl, ArtistType artistTypeId) {
            this.exportDate = exportDate;
            this.artistId = Preconditions.checkNotNull(artistId);
            this.name = name;
            this.isActualArtist = isActualArtist;
            this.viewUrl = viewUrl;
            this.artistTypeId = artistTypeId;
        }

        @Override
        public boolean equals(Object that) {
            if(this == that) {
                return true;
            }
            if(that instanceof ArtistTableRow) {
                ArtistTableRow other = (ArtistTableRow) that;
                return artistId.equals(other.artistId);
            }
            return false;
        }
        
        @Override
        public int hashCode() {
            return artistId.hashCode();
        }
        
        @Override
        public String toString() {
            return String.format("%s: %s (%s)", artistId, name, artistTypeId);
        }

        public DateTime getExportDate() {
            return exportDate;
        }

        public String getArtistId() {
            return artistId;
        }

        public String getName() {
            return name;
        }

        public Boolean getIsActualArtist() {
            return isActualArtist;
        }

        public String getViewUrl() {
            return viewUrl;
        }

        public ArtistType getArtistTypeId() {
            return artistTypeId;
        }
        
    }

    private final Reader artists;
    
    public ArtistTable(File file) throws FileNotFoundException {
        this(Files.newReader(file, Charsets.UTF_8));
    }
    
    public ArtistTable(Reader backingFile) {
        this.artists = backingFile;
    }
    
    public int processRows(final ArtistTableRowProcessor processor) throws IOException {
        LineProcessor<Integer> lineProcessor = new LineProcessor<Integer>() {

            int processed = 0;

            private boolean isComment(String line) {
                return line.startsWith("#");
            }

            @Override
            public Integer getResult() {
                return processed;
            }

            private final char FIELD_SEPARATOR = (char) 1;
            private Splitter splitter = Splitter.on(FIELD_SEPARATOR);
            
            @Override
            public boolean processLine(String line) throws IOException {
                if(isComment(line)) {
                    return true;
                }
                processed++;
                return processor.process(extractArtistRow(line));
            }

            private ArtistTableRow extractArtistRow(String line) {
                ImmutableList<String> lineParts = ImmutableList.copyOf(splitter.split(line.trim()));
                return new ArtistTableRow(
                        new DateTime(Long.valueOf(EXPORT_DATE.extractValue(lineParts)), DateTimeZones.UTC), 
                        ARTIST_ID.extractValue(lineParts), 
                        NAME.extractValue(lineParts), 
                        booleanFrom(IS_ACTUAL_ARTIST.extractValue(lineParts)), 
                        VIEW_URL.extractValue(lineParts), 
                        ArtistType.artistTypeForId(ARTIST_TYPE_ID.extractValue(lineParts)));
            }

            private Boolean booleanFrom(String value) {
                return "1".equals(value);
            }
        };

        CharStreams.readLines(new InputSupplier<Reader>() {
            @Override
            public Reader getInput() throws IOException {
                return artists;
            }
        }, lineProcessor);
        
        return lineProcessor.getResult();
    }
    
    @Override
    public String toString() {
        return String.format("Artist Table (%s)", artists);
    }
}
