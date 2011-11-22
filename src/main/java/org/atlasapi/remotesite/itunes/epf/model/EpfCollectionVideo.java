package org.atlasapi.remotesite.itunes.epf.model;

import static org.atlasapi.remotesite.itunes.epf.model.EpfTableColumn.BOOLEAN;
import static org.atlasapi.remotesite.itunes.epf.model.EpfTableColumn.INTEGER;
import static org.atlasapi.remotesite.itunes.epf.model.EpfTableColumn.TIMESTAMP;
import static org.atlasapi.remotesite.itunes.epf.model.EpfTableColumn.column;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.metabroadcast.common.time.Timestamp;

public class EpfCollectionVideo extends EpfTableRow {

    public static Function<List<String>, EpfCollectionVideo> FROM_ROW_PARTS = new Function<List<String>, EpfCollectionVideo>() {
        @Override
        public EpfCollectionVideo apply(List<String> input) {
            return new EpfCollectionVideo(input);
        }
    };
    
    private static int iota = 0;
    public static final EpfTableColumn<Timestamp> EXPORT_DATE = column(iota++, TIMESTAMP);
    public static final EpfTableColumn<Integer> COLLECTION_ID = column(iota++, INTEGER);
    public static final EpfTableColumn<Integer> VIDEO_ID = column(iota++, INTEGER);
    public static final EpfTableColumn<Integer> TRACK_NUMBER = column(iota++, INTEGER);
    public static final EpfTableColumn<Integer> VOLUME_NUMBER = column(iota++, INTEGER);
    public static final EpfTableColumn<Boolean> PREORDER_ONLY = column(iota++, BOOLEAN);
    
    public EpfCollectionVideo(List<String> rowParts) {
        super(rowParts);
    }
    
    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that instanceof EpfArtistCollection) {
            EpfArtistCollection other = (EpfArtistCollection) that;
            return Objects.equal(this.get(COLLECTION_ID), other.get(COLLECTION_ID)) && Objects.equal(this.get(VIDEO_ID), other.get(VIDEO_ID));
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(this.get(COLLECTION_ID), this.get(VIDEO_ID));
    }
    
    @Override
    public String toString() {
        return String.format("Artist %s -> Collection %s", this.get(COLLECTION_ID), this.get(VIDEO_ID));
    }
}
