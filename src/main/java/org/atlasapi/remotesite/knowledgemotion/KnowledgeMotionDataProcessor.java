package org.atlasapi.remotesite.knowledgemotion;

import com.google.common.collect.ImmutableSet;
import com.google.gdata.data.spreadsheet.CustomElementCollection;

public interface KnowledgeMotionDataProcessor<T> {

    boolean process(CustomElementCollection customElements);

    ImmutableSet<String> seenUris();

    T getResult();

}
