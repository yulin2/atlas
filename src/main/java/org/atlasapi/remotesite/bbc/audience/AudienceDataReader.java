package org.atlasapi.remotesite.bbc.audience;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;

import com.google.api.client.repackaged.com.google.common.base.Throwables;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;


public class AudienceDataReader {

    private static final Splitter SPLITTER = Splitter.on("\t").trimResults();
    private final NumberFormat fmt;
    private final String filename;
    
    public AudienceDataReader(String filename) {
        fmt = NumberFormat.getInstance();
        if (fmt instanceof DecimalFormat) {
            ((DecimalFormat) fmt).setParseBigDecimal(true);
        }
        this.filename = checkNotNull(filename);
    }
    
    public Iterable<AudienceDataRow> readData() throws IOException {
        return Files.readLines(new File(filename), 
                               Charsets.UTF_8, 
                               new AudienceDataLineReader());
    }
    
    private class AudienceDataLineReader implements LineProcessor<List<AudienceDataRow>> {

        private ImmutableList.Builder<AudienceDataRow> rows = ImmutableList.builder();
        
        @Override
        public boolean processLine(String line) throws IOException {
            Iterator<String> values = SPLITTER.split(line).iterator();
            
            String channel = values.next();
            String title = values.next();
            LocalDate date = toLocalDate(values.next());
            LocalTime startTime = toLocalTime(values.next());
            LocalTime endTime = toLocalTime(values.next());
            String genre = values.next();
            BigDecimal audience = null;
            BigDecimal share = null;
            try {
                audience = toBigDecimal(values.next());
                share = toBigDecimal(values.next());
            } catch (ParseException e) {
                Throwables.propagate(e);
            }
            
            Integer ai = toInteger(values.next());
            Integer malePercentage = toInteger(values.next());
            Integer femalePercentage = toInteger(values.next());
            String repeat = values.next();
            Integer age4to9 = toInteger(values.next());
            Integer age10to15 = toInteger(values.next());
            Integer age16to24 = toInteger(values.next());
            Integer age25to34 = toInteger(values.next());
            Integer age35to44 = toInteger(values.next());
            Integer age45to54 = toInteger(values.next());
            Integer age55to64 = toInteger(values.next());
            Integer age65plus = toInteger(values.next());
            Integer ab = toInteger(values.next());
            Integer c1 = toInteger(values.next());
            Integer c2 = toInteger(values.next());
            Integer de = toInteger(values.next());
            
            rows.add(new AudienceDataRow(channel, title, date, 
                    startTime, endTime, genre, audience, share, ai, 
                    malePercentage, femalePercentage, repeat, age4to9, 
                    age10to15, age16to24, age25to34, age35to44, age45to54, 
                    age55to64, age65plus, ab, c1, c2, de));
            
            return true;
        }

        private Integer toInteger(String next) {
            if ("*".equals(next)) {
                return null;
            }
            return Integer.valueOf(next);
        }

        private BigDecimal toBigDecimal(String next) throws ParseException {
            return (BigDecimal) fmt.parse(next);
        }

        private LocalTime toLocalTime(String next) {
            return LocalTime.parse(next);
        }

        private LocalDate toLocalDate(String next) {
            return LocalDate.parse(next, DateTimeFormat.forPattern("dd/MM/yyyy"));
        }

        @Override
        public List<AudienceDataRow> getResult() {
            return rows.build();
        }
        
    }
}
