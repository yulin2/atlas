package org.atlasapi.remotesite.bloomberg;

import com.google.gdata.data.spreadsheet.CustomElementCollection;

public interface BloombergDataProcessor<T> {

    boolean process(CustomElementCollection customElements);
    
    T getResult();
    
}
