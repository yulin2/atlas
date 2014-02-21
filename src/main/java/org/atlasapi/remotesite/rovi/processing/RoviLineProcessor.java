package org.atlasapi.remotesite.rovi.processing;

import java.nio.charset.Charset;

import org.atlasapi.remotesite.rovi.RoviUtils;
import org.atlasapi.remotesite.rovi.indexing.IndexAccessException;
import org.atlasapi.remotesite.rovi.indexing.KeyedLine;
import org.atlasapi.remotesite.rovi.parsers.RoviLineParser;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;

import com.google.common.base.Function;
import com.google.common.io.LineProcessor;


public abstract class RoviLineProcessor<T extends KeyedLine<?>> implements LineProcessor<RoviDataProcessingResult> {

    private final Function<String, T> parser;
    protected final Charset charset;
    
    private long scannedLines = 0;
    private long processedLines = 0;
    private long failedLines = 0;
    private DateTime startTime;
    
    public RoviLineProcessor(RoviLineParser<T> parser, Charset charset) {
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
    protected abstract void handleBom();
    protected abstract void handleProcessingException(Exception e, String line);
    
    @Override
    public boolean processLine(String line) {
        // Removing BOM if charset is UTF-16LE see (http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4508058)
        if (scannedLines == 0) {
            startTime = now();
            line = stripBomIfNeeded(line);
        }
        
        try {
            T parsedLine = parse(line);
            
            if (isToProcess(parsedLine)) {
                log().trace("Processing line of type {} with id: {}", parsedLine.getClass().getName(), parsedLine.getKey().toString());
                process(line, parsedLine);
                processedLines++;
            }
        } catch (IndexAccessException iae) {
            // Throwing a RuntimeException in order to stop the processing. 
            // It's not possible to go ahead if the index is not accessible
            throw new RuntimeException(errorMessage(line), iae);
        } catch (Exception e) {
            failedLines++;
            handleProcessingException(e, line);
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
    
    protected String errorMessage(String line) {
        return "Error occurred while processing the line [" + line + "]";
    }
    
    
    protected T parse(String line) {
        return parser.apply(line);
    }

    private String stripBomIfNeeded(String line) {
        if (RoviUtils.startsWithUTF16LEBom(line)) {
            line = RoviUtils.stripBom(line);
            handleBom();
        }
        return line;
    }
    
    private DateTime now() {
        return DateTime.now(DateTimeZone.UTC);
    }
}