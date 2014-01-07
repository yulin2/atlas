package org.atlasapi.remotesite.metabroadcast.similar;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Iterator;
import java.util.List;

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
import com.google.common.collect.MinMaxPriorityQueue;


public class DefaultSimilarContentProvider implements SimilarContentProvider {

    private final ContentLister contentLister;
    private final Publisher publisher;
    private final SimilarityScorer scorer;
    private final int similarItemLimit;
    
    public DefaultSimilarContentProvider (ContentLister contentLister, Publisher publisher,
            SimilarityScorer scorer, int similarItemLimit) {
        this.contentLister = checkNotNull(contentLister);
        this.publisher = checkNotNull(publisher);
        this.scorer = checkNotNull(scorer);
        this.similarItemLimit = similarItemLimit;
    }
    
    @Override
    public List<Described> similarTo(Described described) {
        
        ContentListingCriteria criteria = new ContentListingCriteria.Builder()
                                                    .forPublisher(publisher)
                                                    .forContent(ContentCategory.TOP_LEVEL_CONTENT)
                                                    .build();
        
        MinMaxPriorityQueue<ScoredContent> similarContent = MinMaxPriorityQueue
                                                    .maximumSize(similarItemLimit)
                                                    .<ScoredContent>create();
        
        Iterator<Content> content = contentLister.listContent(criteria);
        
        while (content.hasNext()) {
            Content c = content.next();
            if (!c.getCanonicalUri().equals(described.getCanonicalUri())) {
                similarContent.add(new ScoredContent(c, scorer.score(described, c)));
            }
        }
        
        return ImmutableList.copyOf(
                        FluentIterable.from(similarContent)
                                      .transform(TO_CONTENT)
                                      .filter(Described.class)
               );
    }

    private static class ScoredContent implements Comparable<ScoredContent> {
        
        private final Content content;
        private final int score;
        
        public ScoredContent(Content content, int score) {
            this.content = content;
            this.score = score;
        }

        @Override
        public int compareTo(ScoredContent o) {
            return ComparisonChain.start()
                    .compare(o.score, this.score)
                    .compare(this.content.getCanonicalUri(), o.content.getCanonicalUri())
                    .result();
        }
    }
    
    private static final Function<ScoredContent, Content> TO_CONTENT = new Function<ScoredContent, Content>() {

        @Override
        public Content apply(ScoredContent sc) {
            return sc.content;
        }
    };
}
