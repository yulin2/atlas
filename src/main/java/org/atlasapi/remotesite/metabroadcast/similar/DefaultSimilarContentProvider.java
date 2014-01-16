package org.atlasapi.remotesite.metabroadcast.similar;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

<<<<<<< HEAD
import org.atlasapi.media.entity.ChildRef;
=======
import org.atlasapi.application.v3.ApplicationConfiguration;
import org.atlasapi.application.v3.SourceStatus;
import org.atlasapi.media.entity.ChildRef;
import org.atlasapi.media.entity.Container;
>>>>>>> similar-available-content
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Described;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentCategory;
import org.atlasapi.persistence.content.listing.ContentLister;
import org.atlasapi.persistence.content.listing.ContentListingCriteria;
import org.atlasapi.persistence.output.AvailableItemsResolver;
import org.atlasapi.persistence.output.UpcomingItemsResolver;

import com.google.common.base.Function;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.MinMaxPriorityQueue;
import com.google.common.collect.Sets;


public class DefaultSimilarContentProvider implements SimilarContentProvider {

    private static final int AVAILABLE_UPCOMING_BOOST_FACTOR = 10;
    private final ContentLister contentLister;
    private final Publisher publisher;
    private final int similarItemLimit;
    private Map<ChildRef, ContentSummary> similarHashes;
    private final TraitHashCalculator traitHashCalculator;
    private final AvailableItemsResolver availableItemsResolver;
    private final UpcomingItemsResolver upcomingItemsResolver;
    private final ApplicationConfiguration appConfig = 
            ApplicationConfiguration.defaultConfiguration()
                                    .withSources(Maps.asMap(Publisher.all(), new Function<Publisher, SourceStatus>() {

                                            @Override
                                            public SourceStatus apply(Publisher publisher) {
                                                return SourceStatus.AVAILABLE_ENABLED;
                                            }
        
                                     }));
    
    public DefaultSimilarContentProvider (ContentLister contentLister, Publisher publisher, 
            int similarItemLimit, TraitHashCalculator traitHashCalculator, 
            AvailableItemsResolver availableItemsResolver, UpcomingItemsResolver upcomingItemsResolver) {
        this.contentLister = checkNotNull(contentLister);
        this.publisher = checkNotNull(publisher);
        this.traitHashCalculator = checkNotNull(traitHashCalculator);
        this.availableItemsResolver = checkNotNull(availableItemsResolver);
        this.upcomingItemsResolver = checkNotNull(upcomingItemsResolver);
        this.similarItemLimit = similarItemLimit;
    }
    
    public void initialise() {
        ContentListingCriteria criteria = new ContentListingCriteria.Builder()
                                                    .forPublisher(publisher)
                                                    .forContent(ContentCategory.TOP_LEVEL_CONTENT)
                                                    .build();
        
        Iterator<Content> content = contentLister.listContent(criteria);
        ImmutableMap.Builder<ChildRef, ContentSummary> similarHashes = ImmutableMap.builder();
        
        while (content.hasNext()) {
            Content c = content.next();
            similarHashes.put(c.childRef(), new ContentSummary(traitHashCalculator.traitHashesFor(c), 
                    isUpcomingOrAvailable(c)));
        }
        this.similarHashes = similarHashes.build();
    }

    private boolean isUpcomingOrAvailable(Content content) {
        if (content instanceof Container) {
            Container container = (Container) content;
            return availableItemsResolver.availableItemsFor(container, appConfig).iterator().hasNext()
                    || upcomingItemsResolver.upcomingItemsFor(container).iterator().hasNext();
        } else if (content instanceof Item) {
            Item item = (Item) content;
            return availableItemsResolver.isAvailable(item, appConfig)
                    || upcomingItemsResolver.hasUpcomingBroadcasts(item, appConfig);
        } else {
            throw new IllegalArgumentException("Can't deal with Content of type " + 
                            content.getClass().getSimpleName());
        }
        
        
    }

    @Override
    public List<ChildRef> similarTo(Described described) {
        checkState(similarHashes != null, "Must call initialise() first");
        MinMaxPriorityQueue<ScoredContent> similarContent = MinMaxPriorityQueue
                .maximumSize(similarItemLimit)
                .<ScoredContent>create();
        
        Set<Integer> candidateHashes = traitHashCalculator.traitHashesFor(described);
        
        for (Entry<ChildRef, ContentSummary> entry : similarHashes.entrySet()) {
            if (!entry.getKey().getId().equals(described.getId())) {
                int score = Sets.intersection(candidateHashes, entry.getValue().traits).size();
                if (entry.getValue().isUpcomingOrAvailable) {
                    score *= AVAILABLE_UPCOMING_BOOST_FACTOR;
                }
                similarContent.add(new ScoredContent(entry.getKey(), score));
            }
        }
        
        return FluentIterable.from(similarContent)
                             .transform(TO_CHILDREF)
                             .toList();
    }

    private static class ScoredContent implements Comparable<ScoredContent> {
        
        private final ChildRef ref;
        private final int score;
        
        public ScoredContent(ChildRef ref, int score) {
            this.ref = ref;
            this.score = score;
        }

        @Override
        public int compareTo(ScoredContent o) {
            return ComparisonChain.start()
                    .compare(o.score, this.score)
                    .compare(this.ref, o.ref)
                    .result();
        }
    }
    
    private static class ContentSummary {
        
        private final Set<Integer> traits;
        private final boolean isUpcomingOrAvailable;
        
        public ContentSummary(Iterable<Integer> traits, boolean isUpcomingOrAvailable) {
            this.traits = ImmutableSet.copyOf(traits);
            this.isUpcomingOrAvailable = isUpcomingOrAvailable;
        }
    }
    
    private static final Function<ScoredContent, ChildRef> TO_CHILDREF = new Function<ScoredContent, ChildRef>() {

        @Override
        public ChildRef apply(ScoredContent sc) {
            return sc.ref;
        }
    };
    
}
