package org.atlasapi.remotesite.metabroadcast;

import java.util.Map;

import com.google.common.base.Optional;

public class NullSchedulingStore implements SchedulingStore {

    @Override
    public void storeState(String key, Map<String, Object> value) {
        //nop
    }

    @Override
    public Optional<Map<String, Object>> retrieveState(String key) {
        return Optional.absent();
    }

}
