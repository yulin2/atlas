package org.atlasapi.remotesite.rovi;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicLong;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;


/**
 * @author An implementation that creates an index of type {@link MapBasedKeyedFileIndex}
 *
 */
public class MapBasedKeyedFileIndexer<T, S extends KeyedLine<T>> implements KeyedFileIndexer<T, S>{
    private final static Logger LOG = LoggerFactory.getLogger(MapBasedKeyedFileIndexer.class);
    private final static String READ_MODE = "r";
    private final static String END_OF_LINE = "\r\n";

    private final File file;
    private final Function<String, S> toParsedLine;
    private final Charset charset;

    public MapBasedKeyedFileIndexer(File file, Charset charset, Function<String, S> toParsedLine) {
        this.file = checkNotNull(file);
        this.toParsedLine = checkNotNull(toParsedLine);
        this.charset = checkNotNull(charset);
    }
    
    @Override
    public KeyedFileIndex<T, S> index()
            throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, READ_MODE);
        Multimap<T, PointerAndSize> indexMap = buildIndex();
        
        MapBasedKeyedFileIndex<T, S> index = new MapBasedKeyedFileIndex<T, S>(randomAccessFile, indexMap, charset, toParsedLine);
        
        return index;
    }
    
    private Multimap<T, PointerAndSize> buildIndex() throws IOException {
        final HashMultimap<T, PointerAndSize> indexMap = HashMultimap.create();
        final AtomicLong currentPointer = new AtomicLong(0);
        
        LOG.info("Start indexing file {}", file.getAbsolutePath());
        
        RoviDataProcessingResult result = Files.readLines(file, charset, new LineProcessor<RoviDataProcessingResult>() {
            long scannedLines = 0;
            long processedLines = 0;
            long failedLines = 0;
            DateTime startTime = now();
            
            @Override
            public boolean processLine(String line) throws IOException {
                // Removing BOM if charset is UTF-16LE see (http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4508058)
                if (scannedLines == 0 && charset.equals(Charsets.UTF_16LE)) {
                    int bomLength = line.substring(0, 1).getBytes(charset).length;
                    line = line.substring(1);
                    currentPointer.addAndGet(bomLength);
                }
                
                int lineSize = line.getBytes(charset).length;
                
                try {
                    S roviLine = toParsedLine.apply(line);
                    indexMap.put(roviLine.getKey(), new PointerAndSize(currentPointer.get(), lineSize));
                    processedLines++;
                } catch (Exception e) {
                    LOG.error("Error occurred while indexing line ["+ line +"]", e);
                    failedLines++;
                } finally {
                    currentPointer.addAndGet(lineSize + endOfLineSize());
                    scannedLines++;
                }
                
                return true;
            }

            @Override
            public RoviDataProcessingResult getResult() {
                return new RoviDataProcessingResult(
                        processedLines,
                        failedLines,
                        startTime,
                        now());
            }
        });
        
        LOG.info("File {} indexed. Result: {}", file.getAbsolutePath(), result);

        return indexMap;
    }
    
    private int endOfLineSize() {
        return END_OF_LINE.getBytes(charset).length;
    }
    
    private DateTime now() {
        return DateTime.now(DateTimeZone.UTC);
    }

}
