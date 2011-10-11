package org.atlasapi.remotesite.redux;

public interface Reducible<T extends Reducible<T>> {
    
    T reduce(T other);
    
}
