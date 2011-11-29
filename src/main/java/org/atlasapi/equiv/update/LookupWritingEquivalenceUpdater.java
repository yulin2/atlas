package org.atlasapi.equiv.update;

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

public class LookupWritingEquivalenceUpdater<T extends Content> implements ContentEquivalenceUpdater<T> {

    private final ContentEquivalenceUpdater<T> delegate;
    private final LookupWriter writer;
    private Cache<String, String> seenAsEquiv;

    public LookupWritingEquivalenceUpdater(ContentEquivalenceUpdater<T> delegate, LookupWriter writer) {
        this(delegate, writer, Duration.standardHours(5));
    }
    
    public LookupWritingEquivalenceUpdater(ContentEquivalenceUpdater<T> delegate, LookupWriter writer, Duration cacheDuration) {
        this.delegate = delegate;
        this.writer = writer;
        this.seenAsEquiv = CacheBuilder.newBuilder().expireAfterWrite(cacheDuration.getMillis(), MILLISECONDS).build(new CacheLoader<String, String>() {
            @Override
            public String load(String key) throws Exception {
                return "";
            }
        });
    }
    
    @Override
    public EquivalenceResult<T> updateEquivalences(T content) {
        EquivalenceResult<T> equivalenceResult = delegate.updateEquivalences(content);
        
        Iterable<T> equivs = Iterables.transform(equivalenceResult.strongEquivalences().values(),ScoredEquivalent.<T>toEquivalent());
        
        //abort writing if seens as equiv and not equiv to anything
        if(seenAsEquiv.asMap().containsKey(content.getCanonicalUri()) && Iterables.isEmpty(equivs)) {
            return equivalenceResult;
        }
        
        for (T equiv : equivs) {
            seenAsEquiv.getUnchecked(equiv.getCanonicalUri());
        }
        
        writer.writeLookup(content, equivs);
        
        return equivalenceResult;
    }

}
