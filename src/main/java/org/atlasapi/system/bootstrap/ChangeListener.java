package org.atlasapi.system.bootstrap;

public interface ChangeListener {

    void beforeChange();
    
    void onChange(Iterable changed);
        
    void afterChange();
}
