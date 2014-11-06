package org.atlasapi.remotesite.rovi.processing;

import static org.atlasapi.remotesite.rovi.RoviConstants.FILE_CHARSET;
import static org.atlasapi.remotesite.rovi.RoviTestUtils.fileFromResource;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.atlasapi.remotesite.rovi.processing.restartable.IngestStatus;
import org.atlasapi.remotesite.rovi.processing.restartable.IngestStatusStore;
import org.atlasapi.remotesite.rovi.processing.restartable.IngestStep;
import org.atlasapi.remotesite.rovi.processing.restartable.RestartableLineProcessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.io.Files;

@RunWith(MockitoJUnitRunner.class)
public class RestartableLineProcessorTest {

    private final static int LINES_TO_SKIP = 3;
    private final static int START_FROM_BEGINNING = 0;
    private static final String FILE_PATH = "org/atlasapi/remotesite/rovi/program.txt";

    @Mock private IngestStatusStore statusStore;

    @Test
    public void testLinesAreSkippedWhenRecoveringFromLatestProcessedLine() throws IOException {
        IngestStatus ingestStatus = new IngestStatus(IngestStep.BRANDS_NO_PARENT, LINES_TO_SKIP);

        RestartableLineProcessor skippingProcessor = new RestartableLineProcessor(new CountingLineProcessor(),
                ingestStatus,
                statusStore);

        long totalLinesInFile = countTotalLines(fileFromResource(FILE_PATH), FILE_CHARSET);

        RoviDataProcessingResult skippingResult = Files.readLines(fileFromResource(FILE_PATH),
                FILE_CHARSET,
                skippingProcessor);

        assertEquals(totalLinesInFile - LINES_TO_SKIP, skippingResult.getProcessedLines());
    }

    @Test
    public void testAllLinesAreProcessedWhenStartingFromBeginning() throws IOException {
        IngestStatus ingestStatus = new IngestStatus(IngestStep.BRANDS_NO_PARENT, START_FROM_BEGINNING);

        RestartableLineProcessor skippingProcessor = new RestartableLineProcessor(new CountingLineProcessor(),
                ingestStatus,
                statusStore);

        long totalLinesInFile = countTotalLines(fileFromResource(FILE_PATH), FILE_CHARSET);

        RoviDataProcessingResult result = Files.readLines(fileFromResource(FILE_PATH),
                FILE_CHARSET,
                skippingProcessor);

        assertEquals(totalLinesInFile, result.getProcessedLines());
    }

    private long countTotalLines(File file, Charset fileCharset) throws IOException {
        RoviDataProcessingResult result = Files.readLines(file,
                fileCharset,
                new CountingLineProcessor());

        return result.getProcessedLines();
    }

}
