package org.atlasapi.remotesite.rovi.processing.restartable;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.annotation.Nullable;

import org.atlasapi.remotesite.rovi.processing.RoviDataProcessingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class IngestProcessingChain {

    private final static Logger LOG = LoggerFactory.getLogger(IngestProcessingChain.class);
    private final ImmutableList<IngestProcessingStep> steps;
    private final static int NOT_FOUND = -1;

    public IngestProcessingChain(List<IngestProcessingStep> steps) {
        this.steps = ImmutableList.copyOf(steps);
    }

    public void execute() {
        executeAll(steps);
    }

    public void execute(@Nullable IngestStatus recoveredStatus) {
        if (recoveredStatus != null) {
            restartFrom(recoveredStatus);
        } else {
            execute();
        }
    }

    public void restartFrom(IngestStatus recoveredStatus) {
        checkNotNull(recoveredStatus, "A not null recovered status should be provided");
        if (recoveredStatus.isCompleted()) {
            execute();
            return;
        }

        IngestStep recoveredStep = recoveredStatus.getCurrentStep();
        int index = Iterables.indexOf(steps, isStep(recoveredStep));

        if (index == NOT_FOUND) {
            throw new UnrecoverableIngestStatusException("Step "
                    + recoveredStep.name()
                    + " not found in the chain, can't proceed with the ingest");
        }

        Iterable<IngestProcessingStep> remainingSteps = skipAlreadyProcessedSteps(index);
        executeAllRemainingSteps(remainingSteps, recoveredStatus);
    }

    private Iterable<IngestProcessingStep> skipAlreadyProcessedSteps(int numberOfStepsToSkip) {
        return Iterables.skip(steps, numberOfStepsToSkip);
    }

    private void executeAll(Iterable<IngestProcessingStep> steps) {
        for (IngestProcessingStep step: steps) {
            RoviDataProcessingResult result = step.execute();
            LOG.info("Step " + step.getStep().name() + " complete, result: {}", result);
        }
    }

    private void executeAllRemainingSteps(Iterable<IngestProcessingStep> remainingSteps,
            IngestStatus recoveredIngestStatus) {
        // Add a precondition
        IngestProcessingStep recoveredStep = Iterables.getFirst(remainingSteps, null);

        if (recoveredStep != null) {
            recoveredStep.execute(recoveredIngestStatus);
            executeAll(Iterables.skip(remainingSteps, 1));
        }
    }

    private Predicate<IngestProcessingStep> isStep(final IngestStep step) {
        return new Predicate<IngestProcessingStep>() {
            @Override
            public boolean apply(IngestProcessingStep input) {
                return input.getStep().equals(step);
            }
        };
    }

}
