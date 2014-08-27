package org.atlasapi.remotesite.knowledgemotion;

import com.google.gdata.data.spreadsheet.CustomElementCollection;

public interface KnowledgeMotionDataProcessor<T> {

    boolean process(CustomElementCollection customElements);
    
    T getResult();
    
}
