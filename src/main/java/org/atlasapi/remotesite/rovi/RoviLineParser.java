package org.atlasapi.remotesite.rovi;


public interface RoviLineParser<T extends KeyedLine<?>> {
    
    T parseLine(String line);

}
