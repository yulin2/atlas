package org.atlasapi.equiv.update;

import static org.atlasapi.persistence.logging.AdapterLogEntry.warnEntry;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.atlasapi.equiv.generators.ContentEquivalenceGenerator;
import org.atlasapi.equiv.results.DefaultScoredEquivalents;
import org.atlasapi.equiv.results.DefaultScoredEquivalents.ScoredEquivalentsBuilder;
import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.EquivalenceResultBuilder;
import org.atlasapi.equiv.results.Score;
import org.atlasapi.equiv.results.ScoredEquivalent;
import org.atlasapi.equiv.results.ScoredEquivalents;
import org.atlasapi.equiv.scorers.ContentEquivalenceScorer;
import org.atlasapi.media.entity.ChildRef;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.ParentRef;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.logging.AdapterLog;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ContainerEquivalenceUpdater implements ContentEquivalenceUpdater<Container> {

    public static final String NAME = "Item";

    private final ContentResolver contentResolver;
    private final AdapterLog log;

    private final ItemEquivalenceUpdater itemUpdater;
    
    private Iterable<ContentEquivalenceGenerator<Container>> generators;
    private Iterable<ContentEquivalenceScorer<Container>> scorers;
    
    private final EquivalenceResultBuilder<Container> containerResultBuilder;
    private final EquivalenceResultBuilder<Item> itemResultBuilder;
    
    public ContainerEquivalenceUpdater(ContentResolver contentResolver, ItemEquivalenceUpdater itemUpdater, 
            EquivalenceResultBuilder<Container> containerResultBuilder, EquivalenceResultBuilder<Item> itemResultBuilder, AdapterLog log) {
                this.contentResolver = contentResolver;
                this.itemUpdater = itemUpdater;
                this.containerResultBuilder = containerResultBuilder;
                this.itemResultBuilder = itemResultBuilder;
                this.log = log;
    }

    public ContainerEquivalenceUpdater withEquivalenceGenerators(Iterable<ContentEquivalenceGenerator<Container>> generators) {
        this.generators = generators;
        return this;
    }

    public ContentEquivalenceUpdater<Container> withEquivalenceScorers(Iterable<ContentEquivalenceScorer<Container>> scorers) {
        this.scorers = scorers;
        return this;
    }

    @Override
    public EquivalenceResult<Container> updateEquivalences(Container content) {
        
        Map<String, Container> containerCache = Maps.newHashMap(); //local cache.
        
        List<String> childrenUris = Lists.transform(content.getChildRefs(), ChildRef.TO_URI);
        
        Map<String, Identified> resolvedChildren = contentResolver.findByCanonicalUris(childrenUris).asResolvedMap();
        
        Set<EquivalenceResult<Item>> childResults = Sets.newHashSet();
        
        for (String childUri : childrenUris) {
            Identified child = resolvedChildren.get(childUri);
            if(child instanceof Item) {
                EquivalenceResult<Item> itemEquivalences = itemUpdater.updateEquivalences((Item)child);
                childResults.add(itemEquivalences);
            } else {
                log.record(warnEntry().withSource(getClass()).withDescription("Resolved %s child %s to null/not Item", content.getCanonicalUri(), childUri));
            }
        }
        
        extractContainersFrom(childResults, childrenUris.size(), containerCache);
        
        return null;
    }

    private ScoredEquivalents<Container> extractContainersFrom(Set<EquivalenceResult<Item>> childResults, int children, Map<String, Container> containerCache) {

        ScoredEquivalentsBuilder<Container> containerEquivalents = DefaultScoredEquivalents.fromSource(NAME);
        
        for (EquivalenceResult<Item> equivalenceResult : childResults) {
            for (ScoredEquivalent<Item> strongEquivalent : equivalenceResult.strongEquivalences().values()) {
                ParentRef parentEquivalent = strongEquivalent.equivalent().getContainer();
                Container container = resolve(parentEquivalent, containerCache);
                if (container != null) {
                    containerEquivalents.addEquivalent(container, normalize(strongEquivalent.score(), container.getChildRefs().size()));
                }
            }
        }
        
        
        return containerEquivalents.build();
    }
    
    private Container resolve(ParentRef parentEquivalent, Map<String, Container> containerCache) {
        String uri = parentEquivalent.getUri();
        if(containerCache.containsKey(uri)) {
            return containerCache.get(uri);
        }

        Container requireValue = (Container) contentResolver.findByCanonicalUris(ImmutableList.of(uri)).getFirstValue().requireValue();
        
        return requireValue;
    }

    private Score normalize(Score score, int itemCount) {
        if(score.isRealScore()) {
            return Score.valueOf(score.asDouble() / itemCount);
        }
        return Score.NULL_SCORE;
    }
}
