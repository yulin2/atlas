package org.atlasapi.equiv.update;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.ScoredEquivalent;
import org.atlasapi.media.entity.Content;
import org.atlasapi.persistence.lookup.LookupWriter;

import com.google.common.collect.Iterables;

public class LookupWritingEquivalenceUpdater<T extends Content> implements ContentEquivalenceUpdater<T> {

    private final ContentEquivalenceUpdater<T> delegate;
    private final LookupWriter writer;

    public LookupWritingEquivalenceUpdater(ContentEquivalenceUpdater<T> delegate, LookupWriter writer) {
        this.delegate = delegate;
        this.writer = writer;
    }
    
    @Override
    public EquivalenceResult<T> updateEquivalences(T content) {
        EquivalenceResult<T> equivalenceResult = delegate.updateEquivalences(content);
        
        writer.writeLookup(content, Iterables.transform(equivalenceResult.strongEquivalences().values(),ScoredEquivalent.<T>toEquivalent()));
        
        return equivalenceResult;
    }

}
