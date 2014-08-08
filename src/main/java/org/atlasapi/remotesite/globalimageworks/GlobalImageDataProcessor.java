package org.atlasapi.remotesite.globalimageworks;

import com.google.gdata.data.spreadsheet.CustomElementCollection;

public interface GlobalImageDataProcessor<T> {

    boolean process(CustomElementCollection customElements);
    
    T getResult();
    
}
