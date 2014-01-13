package org.atlasapi.remotesite.metabroadcast.similar;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Described;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentCategory;
import org.atlasapi.persistence.content.listing.ContentLister;
import org.atlasapi.persistence.content.listing.ContentListingCriteria;

import com.google.common.base.Function;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MinMaxPriorityQueue;
import com.google.common.collect.Sets;


public class DefaultSimilarContentProvider implements SimilarContentProvider {

    private final ContentLister contentLister;
    private final Publisher publisher;
    private final int similarItemLimit;
    private Map<Long, Set<Integer>> similarHashes;
    private final TraitHashCalculator traitHashCalculator;
    
    public DefaultSimilarContentProvider (ContentLister contentLister, Publisher publisher, 
            int similarItemLimit, TraitHashCalculator traitHashCalculator) {
        this.contentLister = checkNotNull(contentLister);
        this.publisher = checkNotNull(publisher);
        this.traitHashCalculator = checkNotNull(traitHashCalculator);
        this.similarItemLimit = similarItemLimit;
    }
    
    public void initialise() {
        ContentListingCriteria criteria = new ContentListingCriteria.Builder()
                                                    .forPublisher(publisher)
                                                    .forContent(ContentCategory.TOP_LEVEL_CONTENT)
                                                    .build();
        
        Iterator<Content> content = contentLister.listContent(criteria);
        ImmutableMap.Builder<Long, Set<Integer>> similarHashes = ImmutableMap.builder();
        
        while (content.hasNext()) {
            Content c = content.next();
            similarHashes.put(c.getId(), traitHashCalculator.traitHashesFor(c));
        }
        this.similarHashes = similarHashes.build();
    }

    @Override
    public List<Long> similarTo(Described described) {
        checkState(similarHashes != null, "Must call initialise() first");
        MinMaxPriorityQueue<ScoredContent> similarContent = MinMaxPriorityQueue
                .maximumSize(similarItemLimit)
                .<ScoredContent>create();
        
        Set<Integer> candidateHashes = traitHashCalculator.traitHashesFor(described);
        
        for (Entry<Long, Set<Integer>> entry : similarHashes.entrySet()) {
            if (entry.getKey() != described.getId()) {
                int score = Sets.intersection(candidateHashes, entry.getValue()).size();
                similarContent.add(new ScoredContent(entry.getKey(), score));
            }
        }
        
        return ImmutableList.copyOf(
                FluentIterable.from(similarContent)
                              .transform(TO_CONTENT)
       );
    }

    private static class ScoredContent implements Comparable<ScoredContent> {
        
        private final long id;
        private final int score;
        
        public ScoredContent(long id, int score) {
            this.id = id;
            this.score = score;
        }

        @Override
        public int compareTo(ScoredContent o) {
            return ComparisonChain.start()
                    .compare(o.score, this.score)
                    .compare(this.id, o.id)
                    .result();
        }
    }
    
    private static final Function<ScoredContent, Long> TO_CONTENT = new Function<ScoredContent, Long>() {

        @Override
        public Long apply(ScoredContent sc) {
            return sc.id;
        }
    };
    
}
