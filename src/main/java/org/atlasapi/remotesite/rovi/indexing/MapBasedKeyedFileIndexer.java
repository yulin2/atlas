package org.atlasapi.remotesite.rovi.indexing;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;

import org.atlasapi.remotesite.rovi.parsers.RoviLineParser;
import org.atlasapi.remotesite.rovi.processing.RoviDataProcessingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.Files;


/**
 * An implementation that creates an index of type {@link MapBasedKeyedFileIndex}
 *
 */
public class MapBasedKeyedFileIndexer<T, S extends KeyedLine<T>> implements KeyedFileIndexer<T, S>{
    private final static Logger LOG = LoggerFactory.getLogger(MapBasedKeyedFileIndexer.class);
    

    private final RoviLineParser<S> parser;
    private final Charset charset;

    public MapBasedKeyedFileIndexer(Charset charset, RoviLineParser<S> parser) {
        this.parser = checkNotNull(parser);
        this.charset = checkNotNull(charset);
    }
    
    @Override
    public KeyedFileIndex<T, S> index(File file)
            throws IOException {
        return indexWithOptionalPredicate(file, Optional.<Predicate<? super S>>absent());
    }
    
    @Override
    public KeyedFileIndex<T, S> indexWithPredicate(File file, Predicate<? super S> isToIndex) throws IOException {
        return indexWithOptionalPredicate(file, Optional.<Predicate<? super S>>of(isToIndex));
    }

    private KeyedFileIndex<T, S> indexWithOptionalPredicate(File file, Optional<Predicate<? super S>> isToIndex)
            throws IOException, FileNotFoundException {
        Multimap<T, PointerAndSize> indexMap = buildIndex(file, isToIndex);
        MapBasedKeyedFileIndex<T, S> index = new MapBasedKeyedFileIndex<T, S>(file, indexMap, charset, parser);
        
        return index;
    }
    
    private Multimap<T, PointerAndSize> buildIndex(File file, Optional<Predicate<? super S>> isToIndex) throws IOException {
        final HashMultimap<T, PointerAndSize> indexMap = HashMultimap.create();
        
        LOG.info("Start indexing file {}", file.getAbsolutePath());
        
        RoviDataProcessingResult result = Files.readLines(file, charset, new RoviLineIndexer<>(
                parser,
                charset,
                indexMap,
                isToIndex));
        
        LOG.info("File {} indexed. Result: {}", file.getAbsolutePath(), result);

        return indexMap;
    }

}
