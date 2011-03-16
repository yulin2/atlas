package org.atlasapi.equiv.tasks;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.ScheduleResolver;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

public class ItemBasedBrandEquivUpdater {

    private static final Set<Publisher> TARGET_PUBLISHERS = ImmutableSet.of(Publisher.BBC, Publisher.ITV, Publisher.C4, Publisher.FIVE);
    
    private final BroadcastBasedItemEquivUpdater itemEquivUpdater;
    private final ContentWriter contentWriter;

    private double certaintyThreshold = 0.9;
    private boolean writesResults = true;

    public ItemBasedBrandEquivUpdater(ScheduleResolver resolver, ContentWriter contentWriter) {
        this(new BroadcastBasedItemEquivUpdater(resolver), contentWriter);
    }
    
    public ItemBasedBrandEquivUpdater(BroadcastBasedItemEquivUpdater itemUpdater, ContentWriter contentWriter) {
        this.itemEquivUpdater = itemUpdater;
        this.contentWriter = contentWriter;
    }
    
    public EquivResult<Brand> updateEquivalence(Brand brand) {
        
        Multimap<Publisher, Brand> suggestedBrands = ArrayListMultimap.create();
        
        List<EquivResult<Item>> episodeEquivResults = Lists.newArrayListWithExpectedSize(brand.getContents().size());
        for (Episode episode : brand.getContents()) {
            
            EquivResult<Item> result = itemEquivUpdater.updateEquivalence(episode);
            episodeEquivResults.add(result);
            
            suggestedBrands.putAll(Multimaps.forMap(getBrands(result.strongSuggestions())));
        }   

        SuggestedEquivalents<Brand> suggestedEquivalentBrands = SuggestedEquivalents.from(suggestedBrands);
        
        for (Publisher publisher : TARGET_PUBLISHERS) {
            Brand mostSuggested = suggestedEquivalentBrands.strongSuggestions(certaintyThreshold).get(publisher);
            if (mostSuggested != null) {
                brand.addEquivalentTo(mostSuggested);
                mostSuggested.addEquivalentTo(brand);
            }
        }
        
        if(writesResults) {
            contentWriter.createOrUpdate(brand, false);
            for (Brand equivBrand : suggestedBrands.values()) {
                contentWriter.createOrUpdate(equivBrand, false);
            }
        }
        
        return ContainerEquivResult.of(brand, suggestedEquivalentBrands, certaintyThreshold).withItemResults(episodeEquivResults);
    }

    private Map<Publisher, Brand> getBrands(Map<Publisher, ? extends Item> mostSuggestedPerPublisher) {
        return Maps.filterValues(Maps.transformValues(mostSuggestedPerPublisher, new Function<Item, Brand>() {
            @Override
            public Brand apply(Item input) {
                if(input.getFullContainer() instanceof Brand) {
                    return (Brand) input.getFullContainer();
                }
                return null;
            }
        }), Predicates.notNull());
    }
    
    
    
    public ItemBasedBrandEquivUpdater withCertaintyThreshold(double threshold) {
        this.itemEquivUpdater.withCertaintyThreshold(threshold);
        this.certaintyThreshold = threshold;
        return this;
    }
    
    public ItemBasedBrandEquivUpdater writesResults(boolean shouldWriteResults) {
        this.writesResults = shouldWriteResults;
        return this;
    }
}
