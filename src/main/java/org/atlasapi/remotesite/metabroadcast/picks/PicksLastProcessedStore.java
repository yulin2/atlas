package org.atlasapi.remotesite.metabroadcast.picks;

import org.joda.time.LocalDate;

import com.google.common.base.Optional;


public interface PicksLastProcessedStore {

    Optional<LocalDate> getLastScheduleDayProcessed();
    void setLastScheduleDayProcessed(LocalDate date);
    
}
