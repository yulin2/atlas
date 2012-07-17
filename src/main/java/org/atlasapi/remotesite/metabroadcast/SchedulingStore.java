package org.atlasapi.remotesite.metabroadcast;

import java.util.Map;

import com.google.common.base.Optional;

public interface SchedulingStore {

    void storeState(String key, Map<String, Object> value);

    Optional<Map<String, Object>> retrieveState(String key);

}
