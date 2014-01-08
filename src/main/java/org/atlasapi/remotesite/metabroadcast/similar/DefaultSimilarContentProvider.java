package org.atlasapi.remotesite.metabroadcast.similar;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.CrewMember;
import org.atlasapi.media.entity.Described;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentCategory;
import org.atlasapi.persistence.content.listing.ContentLister;
import org.atlasapi.persistence.content.listing.ContentListingCriteria;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.MinMaxPriorityQueue;
import com.google.common.collect.Sets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;


public class DefaultSimilarContentProvider implements SimilarContentProvider {

    private static final HashFunction hash = Hashing.goodFastHash(32);

    private final ContentLister contentLister;
    private final Publisher publisher;
    private final SimilarityScorer scorer;
    private final int similarItemLimit;
    private Map<Long, Set<Integer>> similarHashes;
    
    public DefaultSimilarContentProvider (ContentLister contentLister, Publisher publisher,
            SimilarityScorer scorer, int similarItemLimit) {
        this.contentLister = checkNotNull(contentLister);
        this.publisher = checkNotNull(publisher);
        this.scorer = checkNotNull(scorer);
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
            similarHashes.put(c.getId(), computeHashes(c));
        }
        this.similarHashes = similarHashes.build();
    }

    private ImmutableSet<Integer> computeHashes(Described c) {
        ImmutableSet.Builder<Integer> hashes = ImmutableSet.builder();
        hashes.addAll(Iterables.transform(c.getGenres(), GENRE_HASH));
        
        if (c instanceof Item) {
            hashes.addAll(Iterables.transform(((Item) c).getPeople(), CREW_HASH));
        }
        return hashes.build();
    }
    
    @Override
    public List<Long> similarTo(Described described) {
        MinMaxPriorityQueue<ScoredContent> similarContent = MinMaxPriorityQueue
                .maximumSize(similarItemLimit)
                .<ScoredContent>create();
        
        ImmutableSet<Integer> candidateHashes = computeHashes(described);
        
        for (Entry<Long, Set<Integer>> entry : similarHashes.entrySet()) {
            int score = Sets.intersection(candidateHashes, entry.getValue()).size();
            similarContent.add(new ScoredContent(entry.getKey(), score));
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
    
    private static final Function<String, Integer> GENRE_HASH = new Function<String, Integer>() {

        @Override
        public Integer apply(String s) {
            return hash.hashString(s, Charsets.UTF_8).asInt();
        }
        
    };
    
    private static final Function<CrewMember, Integer> CREW_HASH = new Function<CrewMember, Integer>() {

        @Override
        public Integer apply(CrewMember c) {
            return hash.hashString(c.getCanonicalUri(), Charsets.UTF_8).asInt();
        }
        
    };
}
