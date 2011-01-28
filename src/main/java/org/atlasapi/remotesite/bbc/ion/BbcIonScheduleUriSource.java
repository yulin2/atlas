package org.atlasapi.remotesite.bbc.ion;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.metabroadcast.common.time.Clock;
import com.metabroadcast.common.time.SystemClock;

public class BbcIonScheduleUriSource implements Iterable<String> {

    public static final String SCHEDULE_PATTERN = "http://www.bbc.co.uk/iplayer/ion/schedule/service/%s/date/%s/timeslot/day/format/json";

    private final Clock clock;
    private final Iterable<String> serviceIds;
    private List<String> uris;
    private int lookAhead = 8;
    private int lookBack = 5;

    public BbcIonScheduleUriSource() {
        this(BbcIonServices.services.keySet());
    }

    public BbcIonScheduleUriSource(Iterable<String> serviceIds) {
        this(new SystemClock(), serviceIds);
    }

    public BbcIonScheduleUriSource(Clock clock, Iterable<String> serviceIds) {
        this.clock = clock;
        this.serviceIds = serviceIds;
    }

    public BbcIonScheduleUriSource withLookAhead(int lookAhead) {
        this.lookAhead = lookAhead;
        return this;
    }

    public BbcIonScheduleUriSource withLookBack(int lookBack) {
        this.lookBack = lookBack;
        return this;
    }
    
    @Override
    public Iterator<String> iterator() {
        if (uris == null) {
            uris = build();
        }
        return Collections.unmodifiableList(uris).iterator();
    }

    private List<String> build() {
        final DateTime now = clock.now();
        return ImmutableList.copyOf(Iterables.concat(Iterables.transform(serviceIds, new Function<String, Iterable<String>>() {
            @Override
            public Iterable<String> apply(String serviceId) {
                List<String> uris = Lists.newArrayListWithCapacity(lookBack + 1 + lookAhead);
                DateTime scheduleDay = now.minusDays(lookBack);
                for (int i = 0; i < (lookBack + 1 + lookAhead); i++) {
                    uris.add(String.format(SCHEDULE_PATTERN, serviceId, scheduleDay.plusDays(i).toString("yyyy-MM-dd")));
                }
                return uris;
            }
        })));
    }

}
