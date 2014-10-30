package org.atlasapi.remotesite.rovi.processing.restartable;

import static com.google.common.base.Preconditions.checkNotNull;

import org.atlasapi.remotesite.rovi.processing.RoviDataProcessingResult;

public abstract class AbstractIngestProcessingStep implements IngestProcessingStep {

    private final static int START_FROM_BEGINNING = 0;
    private final IngestStep step;

    protected AbstractIngestProcessingStep(IngestStep step) {
        this.step = checkNotNull(step);
    }

    @Override public IngestStep getStep() {
        return this.step;
    }

    @Override
    public RoviDataProcessingResult execute() {
        return execute(new IngestStatus(step, START_FROM_BEGINNING));
    }

    @Override
    public RoviDataProcessingResult execute(IngestStatus recoveredIngestStatus) {
        ensureRecoveredStepIsValid(recoveredIngestStatus);

        return executeWithStatus(recoveredIngestStatus);
    }

    public abstract RoviDataProcessingResult executeWithStatus(IngestStatus ingestStatus);

    private void ensureRecoveredStepIsValid(IngestStatus recoveredIngestStatus) {
        if (!recoveredIngestStatus.getCurrentStep().equals(step)) {
            String errorMsg = "Step %s cannot be recovered from step %s";
            throw new UnrecoverableIngestStatusException(String.format(errorMsg,
                    recoveredIngestStatus.getCurrentStep().name(),
                    step.name()));
        }
    }

}
