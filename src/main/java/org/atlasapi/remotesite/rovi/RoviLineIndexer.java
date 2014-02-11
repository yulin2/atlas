package org.atlasapi.remotesite.rovi;

import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;


public class RoviLineIndexer<T, S extends KeyedLine<T>> extends RoviLineProcessor<S> {

    private static final Logger LOG = LoggerFactory.getLogger(RoviLineIndexer.class);
    private final static String END_OF_LINE = "\r\n";
    
    private final Multimap<T, PointerAndSize> indexMap;
    private final AtomicLong currentPointer = new AtomicLong(0);
    
    public RoviLineIndexer(RoviLineParser<S> parser, Charset charset, Multimap<T, PointerAndSize> indexMap) {
        super(parser, charset);
        this.indexMap = indexMap;
    }
    
    @Override
    protected Logger log() {
        return LOG;
    }

    @Override
    protected boolean isToProcess(S parsedLine) {
        return true;
    }

    @Override
    protected void process(String line, S parsedLine) throws IndexAccessException {
        indexMap.put(parsedLine.getKey(), new PointerAndSize(currentPointer.get(), getSizeInBytes(line)));
    }

    private int getSizeInBytes(String line) {
        return line.getBytes(charset).length;
    }

    @Override
    protected void doFinally(String line) {
        currentPointer.addAndGet(getSizeInBytes(line) + endOfLineSize());
    }

    @Override
    protected void handleBom(int bomLength) {
        currentPointer.addAndGet(bomLength);
    }
    
    private int endOfLineSize() {
        return END_OF_LINE.getBytes(charset).length;
    }

}
