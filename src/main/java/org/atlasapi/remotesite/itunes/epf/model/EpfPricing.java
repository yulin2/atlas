package org.atlasapi.remotesite.itunes.epf.model;

import static org.atlasapi.remotesite.itunes.epf.model.EpfTableColumn.BIG_DECIMAL;
import static org.atlasapi.remotesite.itunes.epf.model.EpfTableColumn.DATE_TIME;
import static org.atlasapi.remotesite.itunes.epf.model.EpfTableColumn.STRING;
import static org.atlasapi.remotesite.itunes.epf.model.EpfTableColumn.column;

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.DateTime;

import com.google.common.base.Function;
import com.google.common.base.Objects;

public class EpfPricing extends EpfTableRow {

    public static Function<List<String>, EpfPricing> FROM_ROW_PARTS = new Function<List<String>, EpfPricing>() {
        @Override
        public EpfPricing apply(List<String> input) {
            return new EpfPricing(input);
        }
    };
    
    public EpfPricing(List<String> rowParts) {
        super(rowParts);
    }

    private static int iota = 0;
    public static final EpfTableColumn<String> TITLE = column(iota++, STRING);
    public static final EpfTableColumn<String> NETWORK = new EpfTableColumn<String>(iota++, STRING){};
    public static final EpfTableColumn<String> PRODUCTIN_NUMBER = new EpfTableColumn<String>(iota++, STRING){};
    public static final EpfTableColumn<String> SEASON = new EpfTableColumn<String>(iota++, STRING){};
    public static final EpfTableColumn<DateTime> ORIGINAL_RELEASE_DATE = column(iota++, DATE_TIME);
    public static final EpfTableColumn<String> COPYRIGHT = new EpfTableColumn<String>(iota++, STRING){};
    public static final EpfTableColumn<String> EPISODE_URL = new EpfTableColumn<String>(iota++, STRING){};
    public static final EpfTableColumn<String> EPISODE_ARTWORK_URL = new EpfTableColumn<String>(iota++, STRING){};
    public static final EpfTableColumn<String> UPC = new EpfTableColumn<String>(iota++, STRING){};
    public static final EpfTableColumn<String> ISAN = new EpfTableColumn<String>(iota++, STRING){};
    public static final EpfTableColumn<String> FORMATS = new EpfTableColumn<String>(iota++, STRING){};
    public static final EpfTableColumn<BigDecimal> SD_PRICE = new EpfTableColumn<BigDecimal>(iota++, BIG_DECIMAL){};
    public static final EpfTableColumn<BigDecimal> HQ_PRICE = new EpfTableColumn<BigDecimal>(iota++, BIG_DECIMAL){};
    public static final EpfTableColumn<BigDecimal> LC_RENTAL_PRICE = new EpfTableColumn<BigDecimal>(iota++, BIG_DECIMAL){};
    public static final EpfTableColumn<BigDecimal> SD_RENTAL_PRICE = new EpfTableColumn<BigDecimal>(iota++, BIG_DECIMAL){};
    public static final EpfTableColumn<BigDecimal> HD_RENTAL_PRICE = new EpfTableColumn<BigDecimal>(iota++, BIG_DECIMAL){};

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that instanceof EpfPricing) {
            EpfPricing other = (EpfPricing) that;
            return Objects.equal(this.get(EPISODE_URL), other.get(EPISODE_URL));
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return this.get(EPISODE_URL).hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("Pricing for %s", this.get(EPISODE_URL));
    }
}
