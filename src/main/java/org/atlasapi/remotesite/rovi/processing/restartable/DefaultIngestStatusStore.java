package org.atlasapi.remotesite.rovi.processing.restartable;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;

import org.atlasapi.remotesite.metabroadcast.SchedulingStore;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

public class DefaultIngestStatusStore implements IngestStatusStore {

    private static final String ROVI_FULL_INGEST_JOB_KEY = "rovi-full-ingest";
    private static final String STEP_KEY = "step";
    private static final String LAST_LINE_KEY = "lastLine";

    private final SchedulingStore store;

    public DefaultIngestStatusStore(SchedulingStore store) {
        this.store = checkNotNull(store);
    }

    @Override
    public Optional<IngestStatus> getIngestStatus() {
        Optional<Map<String, Object>> status = store.retrieveState(ROVI_FULL_INGEST_JOB_KEY);

        if (!status.isPresent()) {
            return Optional.absent();
        }

        IngestStep step = IngestStep.valueOf((String) status.get().get(STEP_KEY));
        long lastLine = (Long) status.get().get(LAST_LINE_KEY);

        return Optional.of(new IngestStatus(step, lastLine));
    }

    @Override
    public void persistIngestStatus(IngestStatus newStatus) {
        Map<String, Object> values = ImmutableMap.<String, Object>builder()
                .put(STEP_KEY, newStatus.getCurrentStep().name())
                .put(LAST_LINE_KEY, newStatus.getProcessedLine())
                .build();

        store.storeState(ROVI_FULL_INGEST_JOB_KEY,
                values);
    }

}
