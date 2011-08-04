package org.atlasapi.remotesite.bbc;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;

import org.atlasapi.remotesite.bbc.ion.BbcIonServices;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.time.Clock;
import com.metabroadcast.common.time.DateTimeZones;
import com.metabroadcast.common.time.DayRangeGenerator;
import com.metabroadcast.common.time.SystemClock;

public class BbcIonDayRangeUrlSupplier implements Supplier<Iterable<String>> {

    private final String urlFormat;
    private final DayRangeGenerator dayGenerator;
    private final Clock clock;
    
    private Iterable<String> services = BbcIonServices.services.keySet();
    private DateTimeFormatter dateFormat = ISODateTimeFormat.basicDate().withZone(DateTimeZones.UTC);

    public BbcIonDayRangeUrlSupplier(String urlFormat, DayRangeGenerator dayGenerator, Clock clock) {
        this.urlFormat = urlFormat;
        this.dayGenerator = dayGenerator;
        this.clock = clock;
    }
    
    public BbcIonDayRangeUrlSupplier(String urlFormat, DayRangeGenerator dayGenerator) {
        this(urlFormat, dayGenerator, new SystemClock());
    }
    
    public BbcIonDayRangeUrlSupplier forServices(Iterable<String> services) {
        this.services = services;
        return this;
    }
    
    public BbcIonDayRangeUrlSupplier withDateFormat(DateTimeFormatter dateFormat) {
        this.dateFormat = dateFormat;
        return this;
    }

    @Override
    public Iterable<String> get() {
        return ImmutableList.copyOf(concat(transform(dayGenerator.generate(clock.now().toLocalDate()), new Function<LocalDate, Iterable<String>>() {
            @Override
            public Iterable<String> apply(final LocalDate day) {
                return Iterables.transform(services, new Function<String, String>() {
                    @Override
                    public String apply(String service) {
                        return String.format(urlFormat, service, dateFormat.print(day));
                    }
                });
            }
        })));
    }
    
}
