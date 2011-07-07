package org.atlasapi.equiv.results;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.atlasapi.equiv.results.scores.ScoredEquivalent;
import org.atlasapi.media.entity.Content;
import org.atlasapi.persistence.lookup.LookupWriter;

import com.google.common.collect.Iterables;
import com.google.common.collect.MapMaker;

public class LookupWritingEquivalenceHandler<T extends Content> implements EquivalenceResultHandler<T> {
 
    private final LookupWriter writer;
    private ConcurrentMap<String, String> seenAsEquiv;

    public LookupWritingEquivalenceHandler(LookupWriter writer) {
        this.writer = writer;
        this.seenAsEquiv = new MapMaker().expireAfterWrite(5, TimeUnit.HOURS).makeMap();
    }
    
    @Override
    public void handle(EquivalenceResult<T> result) {
        
        Iterable<T> equivs = Iterables.transform(result.strongEquivalences().values(),ScoredEquivalent.<T>toEquivalent());
        
        //abort writing if seens as equiv and not equiv to anything
        if(seenAsEquiv.containsKey(result.target().getCanonicalUri()) && Iterables.isEmpty(equivs)) {
            return;
        }
        
        for (T equiv : equivs) {
            seenAsEquiv.put(equiv.getCanonicalUri(), "");
        }
        
        writer.writeLookup(result.target(), equivs);
        
        
    }

}
