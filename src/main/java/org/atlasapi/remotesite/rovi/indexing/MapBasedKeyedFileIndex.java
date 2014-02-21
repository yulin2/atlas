package org.atlasapi.remotesite.rovi.indexing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;



/**
 * An implementation that is based on the use of {@link Multimap} for storing the index
 *
 */
public class MapBasedKeyedFileIndex<T, S extends KeyedLine<T>> implements KeyedFileIndex<T, S> {

    private final static String READ_MODE = "r";
    private final static Logger LOG = LoggerFactory.getLogger(MapBasedKeyedFileIndex.class);
    
    private final File file;
    private final RandomAccessFile randomAccessFile;
    private final Multimap<T, PointerAndSize> indexMap;
    private final Charset charset;
    private final Function<String, S> toParsedLine;
    
    public MapBasedKeyedFileIndex(File file, Multimap<T, PointerAndSize> indexMap, Charset charset, Function<String, S> toParsedLine) throws FileNotFoundException {
        this.file = file;
        this.randomAccessFile = new RandomAccessFile(file, READ_MODE);
        this.indexMap = indexMap;
        this.charset = charset;
        this.toParsedLine = toParsedLine;
    }
    
    @Override
    public Collection<S> getLinesForKey(T key) throws IndexAccessException  {
        return getLinesForKey(key, Predicates.<S>alwaysTrue());
    }
    
    @Override
    public Collection<S> getLinesForKey(T key, Predicate<? super S> predicate) throws IndexAccessException {
        Collection<PointerAndSize> pointerAndSizeList = indexMap.get(key);
        ImmutableList.Builder<S> builder = ImmutableList.builder();
        
        for (PointerAndSize pointerAndSize: pointerAndSizeList) {
            S parsed = getParsed(key, pointerAndSize);
            if (predicate.apply(parsed)) {
                builder.add(parsed);
            }
        }

        return builder.build();
    }
    
    @Override
    public Set<T> getKeys() {
        return indexMap.keySet();
    }
    
    private S getParsed(T key, PointerAndSize pointerAndSize) throws IndexAccessException {
        String line = readData(key, pointerAndSize);
        return toParsedLine.apply(line);
    }
    
    private String readData(T key, PointerAndSize pointerAndSize) throws IndexAccessException {
        try {
            randomAccessFile.seek(pointerAndSize.getPointer());
            byte[] bytesBuffer = new byte[pointerAndSize.getSize()];
            randomAccessFile.read(bytesBuffer);

            return new String(bytesBuffer, charset);
        } catch (IOException e) {
            throw new IndexAccessException("Error while trying to access the index - key: " + key.toString(), e);
        }
    }

    @Override
    public void releaseResources() {
        try {
            randomAccessFile.close();
        } catch (IOException e) {
            LOG.error("Error while closing RandomAccessFile for file " + file.getAbsolutePath(), e);
        }
    }

    @Override
    public Optional<S> getFirstForKey(T key) throws IndexAccessException {
        if (!indexMap.containsKey(key)) {
            return Optional.absent();
        }
        
        Collection<PointerAndSize> pointerAndSizeList = indexMap.get(key);
        PointerAndSize pointerAndSize = Iterables.getFirst(pointerAndSizeList, null);

        if (pointerAndSize != null) {
            return Optional.of(getParsed(key, pointerAndSize));
        }

        return Optional.absent();
    }

}
