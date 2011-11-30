package org.atlasapi.equiv.handlers;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.scores.ScoredEquivalent;
import org.atlasapi.media.entity.Content;
import org.atlasapi.persistence.lookup.LookupWriter;
import org.joda.time.Duration;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.Iterables;

public class LookupWritingEquivalenceHandler<T extends Content> implements EquivalenceResultHandler<T> {
 
    private final LookupWriter writer;
    private final Cache<String, String> seenAsEquiv;
    
    public LookupWritingEquivalenceHandler(LookupWriter writer) {
        this(writer, Duration.standardHours(5));
    }

    public LookupWritingEquivalenceHandler(LookupWriter writer, Duration cacheDuration) {
        this.writer = writer;
        this.seenAsEquiv = CacheBuilder.newBuilder().expireAfterWrite(cacheDuration.getMillis(), MILLISECONDS).build(new CacheLoader<String, String>() {
            @Override
            public String load(String key) throws Exception {
                return "";
            }
        });
    }
    
    @Override
    public void handle(EquivalenceResult<T> result) {
        
        Iterable<T> equivs = Iterables.transform(result.strongEquivalences().values(),ScoredEquivalent.<T>toEquivalent());
        
        //abort writing if seens as equiv and not equiv to anything
        if(seenAsEquiv.asMap().containsKey(result.target().getCanonicalUri()) && Iterables.isEmpty(equivs)) {
            return;
        }
        
        for (T equiv : equivs) {
            seenAsEquiv.getUnchecked(equiv.getCanonicalUri());
        }
        
        writer.writeLookup(result.target(), equivs);
        
    }

}
