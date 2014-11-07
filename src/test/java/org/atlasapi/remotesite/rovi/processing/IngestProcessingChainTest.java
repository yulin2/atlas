package org.atlasapi.remotesite.rovi.processing;

import static org.atlasapi.remotesite.rovi.processing.restartable.IngestStep.BRANDS_NO_PARENT;
import static org.atlasapi.remotesite.rovi.processing.restartable.IngestStep.BRANDS_WITH_PARENT;
import static org.atlasapi.remotesite.rovi.processing.restartable.IngestStep.ITEMS_NO_PARENT;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.atlasapi.remotesite.rovi.processing.restartable.IngestProcessingChain;
import org.atlasapi.remotesite.rovi.processing.restartable.IngestProcessingStep;
import org.atlasapi.remotesite.rovi.processing.restartable.IngestStatus;
import org.atlasapi.remotesite.rovi.processing.restartable.IngestStep;
import org.atlasapi.remotesite.rovi.processing.restartable.UnrecoverableIngestStatusException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class IngestProcessingChainTest {

    @Mock
    private IngestProcessingStep step1;

    @Mock
    private IngestProcessingStep step2;

    @Mock
    private IngestProcessingStep step3;

    private List<IngestProcessingStep> steps;

    @Before
    public void init() {
        when(step1.getStep()).thenReturn(BRANDS_NO_PARENT);
        when(step2.getStep()).thenReturn(BRANDS_WITH_PARENT);
        when(step3.getStep()).thenReturn(ITEMS_NO_PARENT);

        steps = Lists.newArrayList(step1, step2, step3);
    }

    @Test
    public void testShouldExecuteAllTheStepsInTheChain() {
        IngestProcessingChain processingChain = new IngestProcessingChain(steps);

        processingChain.execute();

        verify(step1, times(1)).execute();
        verify(step2, times(1)).execute();
        verify(step3, times(1)).execute();
    }

    @Test
    public void testShouldRestartFromRecoveredStatus() {
        IngestStatus recoveredStatus = new IngestStatus(BRANDS_WITH_PARENT, 3);
        ArgumentCaptor<IngestStatus> argument = ArgumentCaptor.forClass(IngestStatus.class);

        IngestProcessingChain processingChain = new IngestProcessingChain(steps);

        processingChain.restartFrom(recoveredStatus);

        verify(step1, times(0)).execute();
        verify(step2, times(1)).execute(argument.capture());
        verify(step3, times(1)).execute();

        assertEquals(recoveredStatus, argument.getValue());
    }

    @Test
    public void testShouldExecuteAllTheStepsWhenRestartingFromCompleted() {
        IngestProcessingChain processingChain = new IngestProcessingChain(steps);

        processingChain.restartFrom(IngestStatus.COMPLETED);

        verify(step1, times(1)).execute();
        verify(step2, times(1)).execute();
        verify(step3, times(1)).execute();
    }

    @Test(expected = UnrecoverableIngestStatusException.class)
    public void testShouldNotExecuteIfRecoveredStepNotPartOfTheChain() {
        IngestProcessingChain processingChain = new IngestProcessingChain(steps);
        processingChain.restartFrom(new IngestStatus(IngestStep.BROADCASTS, 0));
    }

}
