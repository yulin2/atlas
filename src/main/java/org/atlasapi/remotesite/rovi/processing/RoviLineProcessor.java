package org.atlasapi.remotesite.rovi.processing;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.atlasapi.remotesite.rovi.RoviConstants.FILE_CHARSET;
import static org.atlasapi.remotesite.rovi.RoviConstants.UTF_16LE_BOM;

import java.nio.charset.Charset;
import java.util.Arrays;

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
        this.parser = checkNotNull(parser);
        this.charset = checkNotNull(charset);
    }
    
    protected abstract Logger log();
    protected abstract boolean shouldProcess(T parsedLine);
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
            
            if (shouldProcess(parsedLine)) {
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
        if (startsWithUTF16LEBom(line)) {
            line = stripBom(line);
            handleBom();
        }
        return line;
    }
    
    private DateTime now() {
        return DateTime.now(DateTimeZone.UTC);
    }
    
    /**
     * Strips the UTF-16LE BOM (Byte Order Mark) from a line
     * UTF-16LE BOM is composed by a sequence of two bytes: 0xFF followed by 0xFE
     * 
     * @param line - The line to strip the BOM from
     * @return the line without the UTF-16LE BOM
     */
    public static String stripBom(String line) {
        if (startsWithUTF16LEBom(line)) {
            byte[] bytes = line.getBytes(FILE_CHARSET);
            return new String(stripBomFromBytes(bytes), FILE_CHARSET);
        }
        
        return line;
    }

    /**
     * Detects if a String line starts with an UTF-16LE BOM (Byte Order Mark).
     * UTF-16LE BOM is composed by a sequence of two bytes: 0xFF followed by 0xFE
     * 
     * @param line - The line to analyze
     * @return true if the line starts with an UTF-16LE BOM, false otherwise
     */
    public static boolean startsWithUTF16LEBom(String line) {
        byte[] bytes = line.getBytes(FILE_CHARSET);
        
        if (bytes.length >= UTF_16LE_BOM.length) {
            int[] firstTwoBytes = {toUnsignedInt(bytes[0]), toUnsignedInt(bytes[1])};    
            
            return Arrays.equals(firstTwoBytes, UTF_16LE_BOM);
        }
        
        return false;
    }

    private static byte[] stripBomFromBytes(byte[] bytes) {
        return Arrays.copyOfRange(bytes, UTF_16LE_BOM.length, bytes.length);
    }
    
    private static int toUnsignedInt(byte b) {
        return (int) b & 0xFF;
    }
}