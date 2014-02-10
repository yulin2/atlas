package org.atlasapi.remotesite.rovi;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;



/**
 * An implementation that is based on the use of {@link Multimap} for storing the index
 *
 */
public class MapBasedKeyedFileIndex<T, S extends KeyedLine<T>> implements KeyedFileIndex<T, S> {

    private final RandomAccessFile randomAccessFile;
    private final Multimap<T, PointerAndSize> indexMap;
    private final Charset charset;
    private final RoviLineParser<S> parser;
    
    public MapBasedKeyedFileIndex(RandomAccessFile randomAccessFile, Multimap<T, PointerAndSize> indexMap, Charset charset, RoviLineParser<S> parser) {
        this.randomAccessFile = randomAccessFile;
        this.indexMap = indexMap;
        this.charset = charset;
        this.parser = parser;
    }
    
    @Override
    public Collection<S> getLinesForKey(T key) throws IOException {
        Collection<PointerAndSize> pointerAndSizeList = indexMap.get(key);
        ImmutableList.Builder<S> builder = ImmutableList.builder();
        
        for (PointerAndSize pointerAndSize: pointerAndSizeList) {
            String line = readData(key, pointerAndSize);
            builder.add(parser.parseLine(line));
        }

        return builder.build();
    }
    
    @Override
    public Set<T> getKeys() {
        return indexMap.keySet();
    }
    
    private String readData(T key, PointerAndSize pointerAndSize) throws IOException {
        randomAccessFile.seek(pointerAndSize.getPointer());
        byte[] bytesBuffer = new byte[pointerAndSize.getSize()];
        randomAccessFile.read(bytesBuffer);
        
        return new String(bytesBuffer, charset);
    }

}
