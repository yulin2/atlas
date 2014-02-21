package org.atlasapi.remotesite.rovi.parsers;

import org.atlasapi.remotesite.rovi.indexing.KeyedLine;

import com.google.common.base.Function;


/**
 * It parses a text line into a specific model
 *
 * @param <T> - The specific model type of the line to be parsed
 */
public interface RoviLineParser<T extends KeyedLine<?>> extends Function<String, T> {
    
}
