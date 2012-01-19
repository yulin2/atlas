package org.atlasapi.remotesite.itunes.epf.model;

import static org.atlasapi.remotesite.itunes.epf.model.EpfTableColumn.BOOLEAN;
import static org.atlasapi.remotesite.itunes.epf.model.EpfTableColumn.INTEGER;
import static org.atlasapi.remotesite.itunes.epf.model.EpfTableColumn.TIMESTAMP;
import static org.atlasapi.remotesite.itunes.epf.model.EpfTableColumn.column;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.metabroadcast.common.time.Timestamp;

public class EpfArtistCollection extends EpfTableRow {

    public static Function<List<String>, EpfArtistCollection> FROM_ROW_PARTS = new Function<List<String>, EpfArtistCollection>() {
        @Override
        public EpfArtistCollection apply(List<String> input) {
            return new EpfArtistCollection(input);
        }
    };
    
    public EpfArtistCollection(List<String> rowParts) {
        super(rowParts);
    }
    
    private static int iota = 0;
    public static final EpfTableColumn<Timestamp> EXPORT_DATE = column(iota++, TIMESTAMP);
    public static final EpfTableColumn<Integer> ARTIST_ID = column(iota++, INTEGER);
    public static final EpfTableColumn<Integer> COLLECTION_ID = column(iota++, INTEGER);
    public static final EpfTableColumn<Boolean> IS_PRIMARY_ARTIST = column(iota++, BOOLEAN);
    public static final EpfTableColumn<Integer> ROLE_ID = column(iota++, INTEGER);
    
    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that instanceof EpfArtistCollection) {
            EpfArtistCollection other = (EpfArtistCollection) that;
            return Objects.equal(this.get(ARTIST_ID), other.get(ARTIST_ID)) && Objects.equal(this.get(COLLECTION_ID), other.get(COLLECTION_ID));
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(this.get(ARTIST_ID), this.get(COLLECTION_ID));
    }
    
    @Override
    public String toString() {
        return String.format("Artist %s -> Collection %s", this.get(ARTIST_ID), this.get(COLLECTION_ID));
    }
}
