package org.atlasapi.remotesite.rovi.indexing;

import static org.atlasapi.remotesite.rovi.RoviConstants.END_OF_LINE;
import static org.atlasapi.remotesite.rovi.RoviConstants.UTF_16LE_BOM;

import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicLong;

import org.atlasapi.remotesite.rovi.parsers.RoviLineParser;
import org.atlasapi.remotesite.rovi.processing.RoviLineProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Multimap;


public class RoviLineIndexer<T, S extends KeyedLine<T>> extends RoviLineProcessor<S> {

    private static final Logger LOG = LoggerFactory.getLogger(RoviLineIndexer.class);
    
    private final Multimap<T, PointerAndSize> indexMap;
    private final AtomicLong currentPointer = new AtomicLong(0);
    private final Optional<Predicate<? super S>> isToIndex;
    
    public RoviLineIndexer(RoviLineParser<S> parser, Charset charset, Multimap<T, PointerAndSize> indexMap, Optional<Predicate<? super S>> isToIndex) {
        super(parser, charset);
        this.indexMap = indexMap;
        this.isToIndex = isToIndex;
    }
    
    @Override
    protected Logger log() {
        return LOG;
    }

    @Override
    protected boolean shouldProcess(S parsedLine) {
        if (isToIndex.isPresent()) {
            return isToIndex.get().apply(parsedLine);
        }
        
        return true;
    }

    @Override
    protected void process(String line, S parsedLine) throws IndexAccessException {
        // Can happen that a field used as a key is null, for example for Rovi Delete records
        if (parsedLine.getKey() != null) {
            indexMap.put(parsedLine.getKey(), new PointerAndSize(currentPointer.get(), getSizeInBytes(line)));
        }
    }

    private int getSizeInBytes(String line) {
        return line.getBytes(charset).length;
    }

    @Override
    protected void doFinally(String line) {
        currentPointer.addAndGet(getSizeInBytes(line) + endOfLineSize());
    }

    @Override
    protected void handleBom() {
        currentPointer.addAndGet(UTF_16LE_BOM.length);
    }
    
    private int endOfLineSize() {
        return END_OF_LINE.getBytes(charset).length;
    }

    @Override
    protected void handleProcessingException(Exception e, String line) {
        // Stops the process throwing an exception
        throw new RuntimeException(errorMessage(line), e);
    }

}
