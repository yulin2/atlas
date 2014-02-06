package org.atlasapi.remotesite.rovi;

import static org.atlasapi.remotesite.rovi.RoviConstants.FILE_CHARSET;

import java.io.IOException;

import org.atlasapi.media.entity.Content;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;

import com.google.common.base.Charsets;
import com.google.common.io.LineProcessor;


public abstract class RoviLineProcessor<T extends KeyedLine<?>> implements LineProcessor<RoviDataProcessingResult> {

    private final RoviContentWriter contentWriter;
    
    private long scannedLines = 0;
    private long processedLines = 0;
    private long failedLines = 0;
    private DateTime startTime;
    
    public RoviLineProcessor(RoviContentWriter contentWriter) {
        this.contentWriter = contentWriter;
    }
    
    protected abstract Logger log();
    protected abstract Content extractContent(T parsedLine);
    protected abstract boolean isToProcess(T parsedLine);
    protected abstract T parse(String line);
    
    @Override
    public boolean processLine(String line) throws IOException {
        // Removing BOM if charset is UTF-16LE see (http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4508058)
        if (scannedLines == 0) {
            startTime = now();
        }
        
        try {
            line = removeBom(line);
            T parsedLine = parse(line);
            
            if (isToProcess(parsedLine)) {
                log().trace("Processing line of type {} with id: {}", parsedLine.getClass().getName(), parsedLine.getKey().toString());
                Content extractedContent = extractContent(parsedLine);
                contentWriter.writeContent(extractedContent);
                processedLines++;
            }
        } catch (Exception e) {
            log().error("Error occurred while processing the line [" + line + "]", e);
            failedLines++;
        } finally {
            scannedLines++;
        }

        return true;
    }

    @Override
    public RoviDataProcessingResult getResult() {
        return new RoviDataProcessingResult(processedLines, failedLines, startTime, now());
    }
    
    private DateTime now() {
        return DateTime.now(DateTimeZone.UTC);
    }
    
    private String removeBom(String line) {
        if (scannedLines == 0 && FILE_CHARSET.equals(Charsets.UTF_16LE)) {
            line = line.substring(1);
        }
        return line;
    }
    
}
