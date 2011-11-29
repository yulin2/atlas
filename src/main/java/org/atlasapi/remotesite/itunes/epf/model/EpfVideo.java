package org.atlasapi.remotesite.itunes.epf.model;

import static org.atlasapi.remotesite.itunes.epf.model.EpfTableColumn.DATE_TIME;
import static org.atlasapi.remotesite.itunes.epf.model.EpfTableColumn.INTEGER;
import static org.atlasapi.remotesite.itunes.epf.model.EpfTableColumn.STRING;
import static org.atlasapi.remotesite.itunes.epf.model.EpfTableColumn.TIMESTAMP;
import static org.atlasapi.remotesite.itunes.epf.model.EpfTableColumn.column;

import java.util.List;

import org.joda.time.DateTime;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.metabroadcast.common.time.Timestamp;

public class EpfVideo extends EpfTableRow {
    
    public static Function<List<String>, EpfVideo> FROM_ROW_PARTS = new Function<List<String>, EpfVideo>() {
        @Override
        public EpfVideo apply(List<String> input) {
            return new EpfVideo(input);
        }
    };
    
    private static int iota = 0;
    public static final EpfTableColumn<Timestamp> EXPORT_DATE = column(iota++, TIMESTAMP);
    public static final EpfTableColumn<Integer> VIDEO_ID = column(iota++, INTEGER);
    public static final EpfTableColumn<String> NAME = column(iota++, STRING);
    public static final EpfTableColumn<String> TITLE_VERSION = column(iota++, STRING);
    public static final EpfTableColumn<String> SEARCH_TERMS = column(iota++, STRING);
    public static final EpfTableColumn<Integer> PARENTAL_ADVISORY_ID = column(iota++, INTEGER);
    public static final EpfTableColumn<String>  ARTIST_DISPLAY_NAME = column(iota++, STRING);
    public static final EpfTableColumn<String>  COLLECTION_DISPLAY_NAME = column(iota++, STRING);
    public static final EpfTableColumn<Integer> MEDIA_TYPE_ID = column(iota++, INTEGER);
    public static final EpfTableColumn<String> VIEW_URL = column(iota++, STRING);
    public static final EpfTableColumn<String> ARTWORK_URL = column(iota++, STRING);
    public static final EpfTableColumn<DateTime> ORIGINAL_RELEASE_DATE = column(iota++, DATE_TIME);
    public static final EpfTableColumn<DateTime> ITUNES_RELEASE_DATE = column(iota++, DATE_TIME);
    public static final EpfTableColumn<String> STUDIO_NAME = column(iota++, STRING);
    public static final EpfTableColumn<String> NETWORK_NAME = column(iota++, STRING);
    public static final EpfTableColumn<String> CONTENT_PROVIDER_NAME = column(iota++, STRING);
    public static final EpfTableColumn<Integer> TRACK_LENGTH = column(iota++, INTEGER);
    public static final EpfTableColumn<String> COPYRIGHT = column(iota++, STRING);
    public static final EpfTableColumn<String> P_LINE = column(iota++, STRING);
    public static final EpfTableColumn<String> SHORT_DESCRIPTION = column(iota++, STRING);
    public static final EpfTableColumn<String> LONG_DESCRIPTION = column(iota++, STRING);
    public static final EpfTableColumn<String> EPISODE_PRODUCTION_NUMBER = column(iota++, STRING);
    
    public EpfVideo(List<String> rowParts) {
        super(rowParts);
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that instanceof EpfVideo) {
            EpfVideo other = (EpfVideo) that;
            return Objects.equal(this.get(VIDEO_ID), other.get(VIDEO_ID));
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return this.get(VIDEO_ID);
    }
    
    @Override
    public String toString() {
        return String.format("Video %s", this.get(VIDEO_ID));
    }
}
