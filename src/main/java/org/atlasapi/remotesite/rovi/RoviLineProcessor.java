package org.atlasapi.remotesite.rovi;

import java.nio.charset.Charset;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;

import com.google.api.client.repackaged.com.google.common.base.Throwables;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.io.LineProcessor;


public abstract class RoviLineProcessor<T extends KeyedLine<?>> implements LineProcessor<RoviDataProcessingResult> {

    private final Function<String, T> parser;
    protected final Charset charset;
    
    private long scannedLines = 0;
    private long processedLines = 0;
    private long failedLines = 0;
    private DateTime startTime;
    
    public RoviLineProcessor(Function<String, T> parser, Charset charset) {
        this.parser = parser;
        this.charset = charset;
    }
    
    protected abstract Logger log();
    protected abstract boolean isToProcess(T parsedLine);
    protected abstract void process(String line, T parsedLine) throws IndexAccessException;
    
    /**
     * Execute actions that have to be done even in case of exceptions
     * @param line
     */
    protected abstract void doFinally(String line);
    protected abstract void handleBom(int bomLength);
    
    @Override
    public boolean processLine(String line) {
        // Removing BOM if charset is UTF-16LE see (http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4508058)
        if (scannedLines == 0) {
            startTime = now();
        }
        
        line = removeBom(line);

        try {
            T parsedLine = parse(line);
            
            if (isToProcess(parsedLine)) {
                log().trace("Processing line of type {} with id: {}", parsedLine.getClass().getName(), parsedLine.getKey().toString());
                process(line, parsedLine);
                processedLines++;
            }
        } catch (Exception e) {
            Throwables.propagateIfInstanceOf(e, IndexAccessException.class);
            log().error("Error occurred while processing the line [" + line + "]", e);
            failedLines++;
        } finally {
            scannedLines++;
            doFinally(line);
        }

        return true;
    }

    @Override
    public RoviDataProcessingResult getResult() {
        return new RoviDataProcessingResult(processedLines, failedLines, startTime, now());
    }
    
    protected T parse(String line) {
        return parser.apply(line);
    }
    
    private DateTime now() {
        return DateTime.now(DateTimeZone.UTC);
    }
    
    private String removeBom(String line) {
        if (scannedLines == 0 && charset.equals(Charsets.UTF_16LE)) {
            int bomLength = line.substring(0, 1).getBytes(charset).length;
            handleBom(bomLength);
            line = line.substring(1);
        }
        
        return line;
    }
}
