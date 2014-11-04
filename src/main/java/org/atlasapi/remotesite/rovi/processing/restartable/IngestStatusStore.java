package org.atlasapi.remotesite.rovi.processing.restartable;

import com.google.common.base.Optional;

public interface IngestStatusStore {

    Optional<IngestStatus> getIngestStatus();
    void persistIngestStatus(IngestStatus newStatus);
    void markAsCompleted();

}
