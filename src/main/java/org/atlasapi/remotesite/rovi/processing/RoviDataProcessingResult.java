package org.atlasapi.remotesite.rovi.processing;

import static com.google.common.base.Preconditions.checkNotNull;

import org.joda.time.DateTime;

import com.google.common.base.Objects;


/**
 * Represents the result of a file processing (i.e. indexing or ingesting)
 *
 */
public class RoviDataProcessingResult {

    private final long processedLines;
    private final long failedLines;
    private final DateTime startTime;
    private final DateTime endTime;
    
    public RoviDataProcessingResult(long processedLines, long failedLines, DateTime startTime, DateTime endTime) {
        this.processedLines = processedLines;
        this.failedLines = failedLines;
        this.startTime = checkNotNull(startTime);
        this.endTime = checkNotNull(endTime);
    }
    
    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("processedLines",
                        processedLines)
                .add("failedLines", failedLines)
                .add("startTime", startTime)
                .add("endTime", endTime)
                .toString();
    }
    
}
