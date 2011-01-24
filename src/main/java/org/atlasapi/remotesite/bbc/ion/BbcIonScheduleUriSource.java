package org.atlasapi.remotesite.bbc.ion;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.metabroadcast.common.time.Clock;
import com.metabroadcast.common.time.SystemClock;

public class BbcIonScheduleUriSource implements Iterable<String> {

    private Clock clock;
    private List<String> uris;
    private Iterable<String> serviceIds;
    
    public BbcIonScheduleUriSource() {
        this(new SystemClock());
    }
    
    public BbcIonScheduleUriSource(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Iterator<String> iterator() {
        if (uris == null) { uris = build(); }
        return Collections.unmodifiableList(uris).iterator();
    }

    private List<String> build() {
        return null;
    }

}
