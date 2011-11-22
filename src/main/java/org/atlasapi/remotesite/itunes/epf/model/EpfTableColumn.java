package org.atlasapi.remotesite.itunes.epf.model;

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.metabroadcast.common.time.DateTimeZones;
import com.metabroadcast.common.time.Timestamp;

public abstract class EpfTableColumn<T> {

    private final int index;
    private final Function<String, T> transformer;
    
    public static final <T> EpfTableColumn<T> column(int index, Function<String, T> transformer) {
        return new EpfTableColumn<T>(index, transformer){};
    }
    
    public EpfTableColumn(int index, Function<String, T> transformer) {
        this.index = index;
        this.transformer = transformer;
    }

    public T getValue(List<String> rowParts) {
        return transformer.apply(extractValue(rowParts));
    }

    protected String extractValue(List<String> rowParts) {
        return rowParts.get(index);
    }
    
    public static final Function<String, Timestamp> TIMESTAMP = new Function<String, Timestamp>() {
        @Override
        public Timestamp apply(String input) {
            return Timestamp.of(Long.valueOf(input));
        }
    };
    
    public static final Function<String, Integer> INTEGER = new Function<String, Integer>() {
        @Override
        public Integer apply(String input) {
            return Integer.valueOf(input);
        }
    };
    
    public static final Function<String, DateTime> DATE_TIME = new Function<String, DateTime>() {
        private final DateTimeFormatter format = DateTimeFormat.forPattern("yyyy MM dd").withZone(DateTimeZones.UTC);
        @Override
        public DateTime apply(String input) {
            return format.parseDateTime(input);
        }
    };
    
    public static final Function<String, Boolean> BOOLEAN = new Function<String, Boolean>() {

        @Override
        public Boolean apply(String input) {
            return "1".equals(input);
        }
        
    };
    
    public static final Function<String, String> STRING = Functions.identity();
    
    public static final Function<String, BigDecimal> BIG_DECIMAL = new Function<String, BigDecimal> () {

        @Override
        public BigDecimal apply(String input) {
            return new BigDecimal(input);
        } 
        
    };
    
}
