package org.atlasapi.remotesite.rovi.processing.restartable;


import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Objects;


public class IngestStatus {

    public static final IngestStatus COMPLETED = new IngestStatus(IngestStep.COMPLETED, 0);

    private final IngestStep currentStep;
    private final long latestProcessedLine;

    public IngestStatus(IngestStep currentStep, long processedLine) {
        this.currentStep = checkNotNull(currentStep);
        this.latestProcessedLine = processedLine;
    }

    public IngestStep getCurrentStep() {
        return currentStep;
    }

    public long getLatestProcessedLine() {
        return latestProcessedLine;
    }

    public boolean isCompleted() {
        return currentStep.isCompleted();
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }

        if (!(that instanceof IngestStatus)) {
            return false;
        }

        IngestStatus thatStatus = (IngestStatus) that;

        return Objects.equals(this.currentStep, thatStatus.currentStep)
                && Objects.equals(this.latestProcessedLine, thatStatus.latestProcessedLine);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentStep, latestProcessedLine);
    }

}
