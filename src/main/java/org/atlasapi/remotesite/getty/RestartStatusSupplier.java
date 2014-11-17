package org.atlasapi.remotesite.getty;

import java.util.Map;

import org.atlasapi.remotesite.metabroadcast.SchedulingStore;

import com.google.common.base.Optional;

public interface RestartStatusSupplier {
    public Optional<Integer> startFromOffset();

    public static final class StoreProvidedStatus implements RestartStatusSupplier {
        private static final String KEY_START_FROM_OFFSET = "startFromOffset";

        private final SchedulingStore store;
        public StoreProvidedStatus(SchedulingStore store) {
            this.store = store;
        }

        @Override
        public Optional<Integer> startFromOffset() {
            Optional<Map<String, Object>> retrievedState = store.retrieveState(GettyUpdateTask.JOB_KEY);
            if (retrievedState.isPresent()) {
                return Optional.of((Integer) retrievedState.get().get(KEY_START_FROM_OFFSET));
            }
            return Optional.absent();
        }
    }
}
