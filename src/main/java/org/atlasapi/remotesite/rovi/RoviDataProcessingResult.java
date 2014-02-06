package org.atlasapi.remotesite.rovi;

import org.joda.time.DateTime;

import com.google.common.base.Objects;


public class RoviDataProcessingResult {

    private final long processedLines;
    private final long failedLines;
    private final DateTime startTime;
    private final DateTime endTime;
    
    public RoviDataProcessingResult(long processedLines, long failedLines, DateTime startTime, DateTime endTime) {
        this.processedLines = processedLines;
        this.failedLines = failedLines;
        this.startTime = startTime;
        this.endTime = endTime;
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