package org.atlasapi.remotesite.rovi.processing.restartable;

import static com.google.api.client.repackaged.com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

import org.atlasapi.remotesite.rovi.processing.RoviDataProcessingResult;

import com.google.common.io.LineProcessor;

public class RestartableLineProcessor implements LineProcessor<RoviDataProcessingResult> {

    private static final boolean CONTINUE_SKIPPING = true;
    private static final int UPDATE_STATUS_EVERY_N_LINES = 50;

    private final LineProcessor<RoviDataProcessingResult> delegate;
    private final IngestStatusStore statusPersistor;
    private final IngestStatus startingStatus;

    private long scannedLines = 0;

    public RestartableLineProcessor(LineProcessor<RoviDataProcessingResult> delegate,
            IngestStatus startingStatus, IngestStatusStore statusPersistor) {
        this.delegate = checkNotNull(delegate);
        this.statusPersistor = checkNotNull(statusPersistor);
        this.startingStatus = checkNotNull(startingStatus);
    }

    @Override
    public boolean processLine(String line) throws IOException {
        scannedLines++;

        if (scannedLines <= startingStatus.getLatestProcessedLine()) {
            return CONTINUE_SKIPPING;
        }

        boolean continueProcessing = delegate.processLine(line);
        persistCurrentStatusIfNeeded();

        return continueProcessing;
    }

    private void persistCurrentStatusIfNeeded() {
        if (shouldPersist()) {
            IngestStatus newStatus = new IngestStatus(startingStatus.getCurrentStep(), scannedLines);
            statusPersistor.persistIngestStatus(newStatus);
        }
    }

    private boolean shouldPersist() {
        return scannedLines % UPDATE_STATUS_EVERY_N_LINES == 0;
    }

    @Override
    public RoviDataProcessingResult getResult() {
        return delegate.getResult();
    }
}
