package org.atlasapi.equiv.update;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.scores.ScoredEquivalent;
import org.atlasapi.media.entity.Content;
import org.atlasapi.persistence.lookup.LookupWriter;

import com.google.common.collect.Iterables;
import com.google.common.collect.MapMaker;

public class LookupWritingEquivalenceUpdater<T extends Content> implements ContentEquivalenceUpdater<T> {

    private final ContentEquivalenceUpdater<T> delegate;
    private final LookupWriter writer;
    private ConcurrentMap<String, String> seenAsEquiv;

    public LookupWritingEquivalenceUpdater(ContentEquivalenceUpdater<T> delegate, LookupWriter writer) {
        this.delegate = delegate;
        this.writer = writer;
        this.seenAsEquiv = new MapMaker().expireAfterWrite(5, TimeUnit.HOURS).makeMap();
    }
    
    @Override
    public EquivalenceResult<T> updateEquivalences(T content) {
        EquivalenceResult<T> equivalenceResult = delegate.updateEquivalences(content);
        
        Iterable<T> equivs = Iterables.transform(equivalenceResult.strongEquivalences().values(),ScoredEquivalent.<T>toEquivalent());
        
        //abort writing if seens as equiv and not equiv to anything
        if(seenAsEquiv.containsKey(content.getCanonicalUri()) && Iterables.isEmpty(equivs)) {
            return equivalenceResult;
        }
        
        for (T equiv : equivs) {
            seenAsEquiv.put(equiv.getCanonicalUri(), "");
        }
        
        writer.writeLookup(content, equivs);
        
        return equivalenceResult;
    }

}
