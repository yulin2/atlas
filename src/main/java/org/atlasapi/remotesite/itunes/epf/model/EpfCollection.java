package org.atlasapi.remotesite.itunes.epf.model;

import static org.atlasapi.remotesite.itunes.epf.model.EpfTableColumn.BOOLEAN;
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

public class EpfCollection extends EpfTableRow {

    public static Function<List<String>, EpfCollection> FROM_ROW_PARTS = new Function<List<String>, EpfCollection>() {
        @Override
        public EpfCollection apply(List<String> input) {
            return new EpfCollection(input);
        }
    };
    
    private static int iota = 0;
    public static final EpfTableColumn<Timestamp> EXPORT_DATE = column(iota++, TIMESTAMP);
    public static final EpfTableColumn<Integer> COLLECTION_ID = column(iota++, INTEGER);
    public static final EpfTableColumn<String> NAME = column(iota++, STRING);
    public static final EpfTableColumn<String> TITLE_VERSION = column(iota++, STRING);
    public static final EpfTableColumn<String> SEARCH_TERMS = column(iota++, STRING);
    public static final EpfTableColumn<Integer> PARENTAL_ADVISORY_ID = column(iota++, INTEGER);
    public static final EpfTableColumn<String>  ARTIST_DISPLAY_NAME = column(iota++, STRING);
    public static final EpfTableColumn<String> VIEW_URL = column(iota++, STRING);
    public static final EpfTableColumn<String> ARTWORK_URL = column(iota++, STRING);
    public static final EpfTableColumn<DateTime> ORIGINAL_RELEASE_DATE = column(iota++, DATE_TIME);
    public static final EpfTableColumn<DateTime> ITUNES_RELEASE_DATE = column(iota++, DATE_TIME);
    public static final EpfTableColumn<String> LABEL_STUDIO = column(iota++, STRING);
    public static final EpfTableColumn<String> CONTENT_PROVIDER_NAME = column(iota++, STRING);
    public static final EpfTableColumn<String> COPYRIGHT = column(iota++, STRING);
    public static final EpfTableColumn<String> P_LINE = column(iota++, STRING);
    public static final EpfTableColumn<Integer> MEDIA_TYPE_ID = column(iota++, INTEGER);
    public static final EpfTableColumn<Boolean> IS_COMPILATION = column(iota++, BOOLEAN);
    public static final EpfTableColumn<CollectionType> COLLECTION_TYPE_ID = column(iota++, CollectionType.FROM_ID);

    public EpfCollection(List<String> rowParts) {
        super(rowParts);
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that instanceof EpfCollection) {
            EpfCollection other = (EpfCollection) that;
            return Objects.equal(this.get(COLLECTION_ID), other.get(COLLECTION_ID));
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return this.get(COLLECTION_ID);
    }
    
    @Override
    public String toString() {
        return String.format("Collection %s", this.get(COLLECTION_ID));
    }
    
}
