package org.atlasapi.equiv.generators;

import org.atlasapi.equiv.results.scores.DefaultScoredEquivalents;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredEquivalent;
import org.atlasapi.equiv.results.scores.ScoredEquivalents;
import org.atlasapi.equiv.results.scores.DefaultScoredEquivalents.ScoredEquivalentsBuilder;
import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.update.ContentEquivalenceUpdater;
import org.atlasapi.media.entity.ChildRef;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.ParentRef;
import org.atlasapi.persistence.content.ContentResolver;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.base.Maybe;

@Deprecated
public class ItemBasedContainerEquivalenceGenerator implements ContentEquivalenceGenerator<Container> {

    public static final String NAME = "Item";
    private final ContentEquivalenceUpdater<Item> itemUpdater;
    private final ContentResolver resolver;

    public ItemBasedContainerEquivalenceGenerator(ContentEquivalenceUpdater<Item> itemUpdater, ContentResolver resolver) {
        this.itemUpdater = itemUpdater;
        this.resolver = resolver;
    }
    
    @Override
    public ScoredEquivalents<Container> generate(Container container) {
        
        ScoredEquivalentsBuilder<Container> containerEquivalents = DefaultScoredEquivalents.fromSource(NAME);
        
         for (ChildRef ref : container.getChildRefs()) {
            
            String itemUri = ref.getUri();
            Maybe<Identified> maybeItem = resolver.findByCanonicalUris(ImmutableList.of(itemUri)).get(itemUri);
            
            if(maybeItem.hasValue()) {
                
                Item item = (Item) maybeItem.requireValue();
                
                EquivalenceResult<Item> itemEquivalences = itemUpdater.updateEquivalences(item);
                
                for (ScoredEquivalent<Item> strongEquivalent : itemEquivalences.strongEquivalences().values()) {
                    ParentRef parentEquivalent = strongEquivalent.equivalent().getContainer();
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
