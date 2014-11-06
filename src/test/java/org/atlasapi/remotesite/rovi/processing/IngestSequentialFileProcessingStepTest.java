package org.atlasapi.remotesite.rovi.processing;

import static org.atlasapi.remotesite.rovi.RoviConstants.FILE_CHARSET;
import static org.atlasapi.remotesite.rovi.RoviTestUtils.countTotalLines;
import static org.atlasapi.remotesite.rovi.RoviTestUtils.fileFromResource;
import static org.atlasapi.remotesite.rovi.processing.restartable.IngestStep.BRANDS_NO_PARENT;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.atlasapi.remotesite.rovi.processing.restartable.IngestSequentialFileProcessingStep;
import org.atlasapi.remotesite.rovi.processing.restartable.IngestStatus;
import org.atlasapi.remotesite.rovi.processing.restartable.IngestStatusStore;
import org.atlasapi.remotesite.rovi.processing.restartable.IngestStep;
import org.atlasapi.remotesite.rovi.processing.restartable.UnrecoverableIngestStatusException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IngestSequentialFileProcessingStepTest {

    private static final String FILE_PATH = "org/atlasapi/remotesite/rovi/program.txt";

    @Mock
    private IngestStatusStore statusStore;

    @Test
    public void testAllLinesAreProcessedWithNoRecoveredStatus() throws IOException {
        File file = fileFromResource(FILE_PATH);

        IngestSequentialFileProcessingStep ingestStep = IngestSequentialFileProcessingStep.builder(FILE_CHARSET, statusStore)
                .withStep(BRANDS_NO_PARENT)
                .withFile(file)
                .withProcessor(new CountingLineProcessor())
                .build();

        long totalLines = countTotalLines(file, FILE_CHARSET);
        RoviDataProcessingResult result = ingestStep.execute();

        assertEquals(totalLines, result.getProcessedLines());
    }

    @Test
    public void testRemainingLinesAreProcessedWithRecoveredStatus() throws IOException {
        File file = fileFromResource(FILE_PATH);
        IngestStatus recoveredStatus = new IngestStatus(BRANDS_NO_PARENT, 3);

        IngestSequentialFileProcessingStep ingestStep = IngestSequentialFileProcessingStep.builder(FILE_CHARSET, statusStore)
                .withStep(BRANDS_NO_PARENT)
                .withFile(file)
                .withProcessor(new CountingLineProcessor())
                .build();

        long totalLines = countTotalLines(file, FILE_CHARSET);
        RoviDataProcessingResult result = ingestStep.execute(recoveredStatus);

        assertEquals(totalLines - recoveredStatus.getLatestProcessedLine(), result.getProcessedLines());
    }

    @Test(expected = UnrecoverableIngestStatusException.class)
    public void testIngestFailsIfRecoveringFromADifferentStep() {
        File file = fileFromResource(FILE_PATH);
        IngestStatus recoveredStatus = new IngestStatus(IngestStep.ITEMS_NO_PARENT, 3);

        IngestSequentialFileProcessingStep ingestStep = IngestSequentialFileProcessingStep.builder(FILE_CHARSET, statusStore)
                .withStep(BRANDS_NO_PARENT)
                .withFile(file)
                .withProcessor(new CountingLineProcessor())
                .build();

        ingestStep.execute(recoveredStatus);
    }

}
