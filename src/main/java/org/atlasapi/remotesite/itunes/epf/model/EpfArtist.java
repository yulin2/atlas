package org.atlasapi.remotesite.itunes.epf.model;

import static org.atlasapi.remotesite.itunes.epf.model.ArtistType.FROM_ID;
import static org.atlasapi.remotesite.itunes.epf.model.EpfTableColumn.BOOLEAN;
import static org.atlasapi.remotesite.itunes.epf.model.EpfTableColumn.INTEGER;
import static org.atlasapi.remotesite.itunes.epf.model.EpfTableColumn.STRING;
import static org.atlasapi.remotesite.itunes.epf.model.EpfTableColumn.TIMESTAMP;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.metabroadcast.common.time.Timestamp;

public class EpfArtist extends EpfTableRow {

    public static Function<List<String>, EpfArtist> FROM_ROW_PARTS = new Function<List<String>, EpfArtist>() {
        @Override
        public EpfArtist apply(List<String> input) {
            return new EpfArtist(input);
        }
    };
    
    private static int iota = 0;
    public static final EpfTableColumn<Timestamp> EXPORT_DATE = new EpfTableColumn<Timestamp>(iota++, TIMESTAMP){};
    public static final EpfTableColumn<Integer> ARTIST_ID = new EpfTableColumn<Integer>(iota++, INTEGER){};
    public static final EpfTableColumn<String> NAME = new EpfTableColumn<String>(iota++, STRING){};
    public static final EpfTableColumn<Boolean> IS_ACTUAL_ARTIST = new EpfTableColumn<Boolean>(iota++, BOOLEAN){};
    public static final EpfTableColumn<String> VIEW_URL = new EpfTableColumn<String>(iota++, STRING){};
    public static final EpfTableColumn<ArtistType> ARTIST_TYPE_ID = new EpfTableColumn<ArtistType>(iota++, FROM_ID) {};
        
    public EpfArtist(List<String> rowParts) {
        super(rowParts);
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that instanceof EpfArtist) {
            EpfArtist other = (EpfArtist) that;
            return Objects.equal(this.get(ARTIST_ID), other.get(ARTIST_ID));
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return this.get(ARTIST_ID);
    }
    
    @Override
    public String toString() {
        return String.format("Artist %s", this.get(ARTIST_ID));
    }
}
