package org.atlasapi.remotesite.rovi.processing;

import java.io.IOException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.common.io.LineProcessor;

public class CountingLineProcessor implements LineProcessor<RoviDataProcessingResult> {

    private long processedLines = 0;
    private DateTime startedTime = DateTime.now(DateTimeZone.UTC);

    @Override
    public boolean processLine(String line) throws IOException {
        processedLines++;
        return true;
    }

    @Override
    public RoviDataProcessingResult getResult() {
        return new RoviDataProcessingResult(processedLines, 0, startedTime, DateTime.now(DateTimeZone.UTC));
    }
}