package org.atlasapi.equiv.generators;

import java.util.List;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.DefaultScoredEquivalents;
import org.atlasapi.equiv.results.scores.DefaultScoredEquivalents.ScoredEquivalentsBuilder;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredCandidate;
import org.atlasapi.equiv.results.scores.ScoredCandidates;
import org.atlasapi.equiv.update.EquivalenceUpdater;
import org.atlasapi.media.entity.ChildRef;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.ParentRef;
import org.atlasapi.persistence.content.ContentResolver;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.base.Maybe;

@Deprecated
public class ItemBasedContainerEquivalenceGenerator implements EquivalenceGenerator<Container> {

    public static final String NAME = "Item";
    private final EquivalenceUpdater<Item> itemUpdater;
    private final ContentResolver resolver;

    public ItemBasedContainerEquivalenceGenerator(EquivalenceUpdater<Item> itemUpdater, ContentResolver resolver) {
        this.itemUpdater = itemUpdater;
        this.resolver = resolver;
    }
    
    @Override
    public ScoredCandidates<Container> generate(Container container, ResultDescription desc) {
        
        ScoredEquivalentsBuilder<Container> containerEquivalents = DefaultScoredEquivalents.fromSource(NAME);
        
         for (ChildRef ref : container.getChildRefs()) {
            
            String itemUri = ref.getUri();
            Maybe<Identified> maybeItem = resolver.findByCanonicalUris(ImmutableList.of(itemUri)).get(itemUri);
            
            if(maybeItem.hasValue()) {
                
                Item item = (Item) maybeItem.requireValue();
                
                EquivalenceResult<Item> itemEquivalences = itemUpdater.updateEquivalences(item, Optional.<List<Item>>absent());
                
                for (ScoredCandidate<Item> strongEquivalent : itemEquivalences.strongEquivalences().values()) {
                    ParentRef parentEquivalent = strongEquivalent.candidate().getContainer();
                    if (resolve(parentEquivalent) != null) {
                        containerEquivalents.addEquivalent(resolve(parentEquivalent), normalize(strongEquivalent.score(), container.getChildRefs().size()));
                    }
                }
            }
            
        }
        
        return containerEquivalents.build();
    }

    private Container resolve(ParentRef parentEquivalent) {
        return (Container) resolver.findByCanonicalUris(ImmutableList.of(parentEquivalent.getUri())).getFirstValue().requireValue();
    }

    private Score normalize(Score score, int itemCount) {
        if(score.isRealScore()) {
            return Score.valueOf(score.asDouble() / itemCount);
        }
        return Score.NULL_SCORE;
    }

}
