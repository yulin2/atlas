package org.atlasapi.output;


public interface EntityListWriter<T> extends EntityWriter<T> {

    String listName();
    
}
