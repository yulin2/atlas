package org.atlasapi.remotesite.rovi;


/**
 * It parses a text line into a specific model
 *
 * @param <T> - The specific model type of the line to be parsed
 */
public interface RoviLineParser<T extends KeyedLine<?>> {
    
    T parseLine(String line);

}
