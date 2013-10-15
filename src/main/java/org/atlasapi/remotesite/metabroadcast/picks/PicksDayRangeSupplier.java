package org.atlasapi.remotesite.metabroadcast.picks;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;

import com.google.common.base.Supplier;
import com.google.common.collect.Range;
import com.metabroadcast.common.time.Clock;
import com.metabroadcast.common.time.SystemClock;


public class PicksDayRangeSupplier implements Supplier<Range<LocalDate>> {

    private static final LocalDate START_DATE = new LocalDate(2013, DateTimeConstants.OCTOBER, 1);
    private static final int DAYS_INTO_FUTURE_TO_RUN = 1;

    private final Clock clock = new SystemClock();
    private final PicksLastProcessedStore picksLastProcessedStore;
    
    public PicksDayRangeSupplier(PicksLastProcessedStore picksLastProcessedStore) {
        this.picksLastProcessedStore = picksLastProcessedStore;
    }
    
    @Override
    public Range<LocalDate> get() {
        LocalDate lastRun = picksLastProcessedStore.getLastScheduleDayProcessed()
                .or(START_DATE.minusDays(1));
        LocalDate computePicksUntil = computePicksUntil();
        
        if (!computePicksUntil.isAfter(lastRun)) {
            return Range.openClosed(START_DATE, START_DATE);
        }
        
        return Range.openClosed(lastRun, computePicksUntil);
    }
    
    private LocalDate computePicksUntil() {
        return clock.now().plusDays(DAYS_INTO_FUTURE_TO_RUN).toLocalDate();
    }

}
