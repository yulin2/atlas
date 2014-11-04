package org.atlasapi.remotesite.rovi.processing;

import static org.atlasapi.remotesite.rovi.processing.restartable.IngestStep.BRANDS_NO_PARENT;
import static org.atlasapi.remotesite.rovi.processing.restartable.IngestStep.BRANDS_WITH_PARENT;
import static org.atlasapi.remotesite.rovi.processing.restartable.IngestStep.ITEMS_NO_PARENT;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.atlasapi.remotesite.rovi.processing.restartable.IngestProcessingChain;
import org.atlasapi.remotesite.rovi.processing.restartable.IngestProcessingStep;
import org.atlasapi.remotesite.rovi.processing.restartable.IngestStatus;
import org.atlasapi.remotesite.rovi.processing.restartable.IngestStep;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class IngestProcessingChainTest {

    @Mock
    private IngestProcessingStep step1;

    @Mock
    private IngestProcessingStep step2;

    @Mock
    private IngestProcessingStep step3;

    @Before
    public void init() {
        when(step1.getStep()).thenReturn(BRANDS_NO_PARENT);
        when(step2.getStep()).thenReturn(BRANDS_WITH_PARENT);
        when(step3.getStep()).thenReturn(ITEMS_NO_PARENT);
    }

    @Test
    public void testShouldExecuteAllTheStepsInTheChain() {
        IngestProcessingChain processingChain = IngestProcessingChain.withFirstStep(step1)
                .andThen(step2)
                .andFinally(step3);

        processingChain.execute();

        verify(step1, times(1)).execute();
        verify(step2, times(1)).execute();
        verify(step3, times(1)).execute();
    }

    @Test
    public void testShouldRestartFromRecoveredStatus() {
        IngestStatus recoveredStatus = new IngestStatus(BRANDS_WITH_PARENT, 3);
        ArgumentCaptor<IngestStatus> argument = ArgumentCaptor.forClass(IngestStatus.class);

        IngestProcessingChain processingChain = IngestProcessingChain.withFirstStep(step1)
                .andThen(step2)
                .andFinally(step3);

        processingChain.restartFrom(recoveredStatus);

        verify(step1, times(0)).execute();
        verify(step2, times(1)).execute(argument.capture());
        verify(step3, times(1)).execute();

        assertEquals(recoveredStatus, argument.getValue());
    }

    @Test
    public void testShouldExecuteAllTheStepsWhenRestartingFromCompleted() {
        IngestProcessingChain processingChain = IngestProcessingChain.withFirstStep(step1)
                .andThen(step2)
                .andFinally(step3);

        processingChain.restartFrom(IngestStatus.COMPLETED);

        verify(step1, times(1)).execute();
        verify(step2, times(1)).execute();
        verify(step3, times(1)).execute();
    }

    @Test
    public void testShouldNotExecuteIfRecoveredStepNotPartOfTheChain() {
        IngestProcessingChain processingChain = IngestProcessingChain.withFirstStep(step1)
                .andThen(step2)
                .andFinally(step3);

        processingChain.restartFrom(new IngestStatus(IngestStep.BROADCASTS, 0));

        verify(step1, times(0)).execute();
        verify(step2, times(0)).execute();
        verify(step3, times(0)).execute();
    }

}
