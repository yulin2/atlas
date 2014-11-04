package org.atlasapi.remotesite.rovi.processing.restartable;

import java.util.List;

import org.atlasapi.remotesite.rovi.processing.RoviDataProcessingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class IngestProcessingChain {

    private final static Logger LOG = LoggerFactory.getLogger(IngestProcessingChain.class);
    private final ImmutableList<IngestProcessingStep> steps;
    private final static int NOT_FOUND = -1;

    private IngestProcessingChain(List<IngestProcessingStep> steps) {
        this.steps = ImmutableList.copyOf(steps);
    }

    public void execute() {
        executeAll(steps);
    }

    public void execute(Optional<IngestStatus> maybeRecoveredStatus) {
        if (maybeRecoveredStatus.isPresent()) {
            restartFrom(maybeRecoveredStatus.get());
        } else {
            execute();
        }
    }

    public void restartFrom(IngestStatus recoveredStatus) {
        if (recoveredStatus.isCompleted()) {
            execute();
            return;
        }

        IngestStep recoveredStep = recoveredStatus.getCurrentStep();
        int index = Iterables.indexOf(steps, isStep(recoveredStep));

        if (index == NOT_FOUND) {
            LOG.warn("Step "
                    + recoveredStep.name()
                    + " not found in the chain, maybe a not restartable step? Skipping all the steps");
            return;
        }

        Iterable<IngestProcessingStep> stepsToExecute = skipAlreadyProcessedSteps(index);
        executeAll(stepsToExecute, recoveredStatus);
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

    private void executeAll(Iterable<IngestProcessingStep> stepsToExecute,
            IngestStatus recoveredIngestStatus) {
        IngestProcessingStep recoveredStep = Iterables.getFirst(stepsToExecute, null);

        if (recoveredStep != null) {
            recoveredStep.execute(recoveredIngestStatus);
            executeAll(Iterables.skip(stepsToExecute, 1));
        }
    }

    public static ChainBuilder withFirstStep(IngestProcessingStep firstStep) {
        return new ChainBuilder(firstStep);
    }

    private Predicate<IngestProcessingStep> isStep(final IngestStep step) {
        return new Predicate<IngestProcessingStep>() {
            @Override
            public boolean apply(IngestProcessingStep input) {
                return input.getStep().equals(step);
            }
        };
    }

    public static class ChainBuilder {
        private final ImmutableList.Builder<IngestProcessingStep> steps = ImmutableList.builder();

        private ChainBuilder(IngestProcessingStep firstStep) {
            steps.add(firstStep);
        }

        public ChainBuilder andThen(IngestProcessingStep step) {
            steps.add(step);
            return this;
        }

        public IngestProcessingChain andFinally(IngestProcessingStep step) {
            steps.add(step);
            return new IngestProcessingChain(steps.build());
        }
    }

}
