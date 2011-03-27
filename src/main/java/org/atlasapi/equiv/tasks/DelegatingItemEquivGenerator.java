package org.atlasapi.equiv.tasks;

import java.util.List;
import java.util.Set;

import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.stats.Count;

public class DelegatingItemEquivGenerator implements ItemEquivGenerator {
    
    private final List<ItemEquivGenerator> delegates;
    private final Set<Publisher> supportedPublishers;
    
    public DelegatingItemEquivGenerator(Iterable<ItemEquivGenerator> delegates) {
        this.delegates = ImmutableList.copyOf(delegates);
        
        ImmutableSet.Builder<Publisher> publishers = ImmutableSet.builder();
        for (ItemEquivGenerator delegate: delegates) {
            publishers.addAll(delegate.supportedPublishers());
        }
        supportedPublishers = publishers.build();
    }

    @Override
    public SuggestedEquivalents<Item> equivalentsFor(Item item) {
        SuggestedEquivalents<Item> suggestions = new SuggestedEquivalents<Item>(ImmutableMap.<Publisher, List<Count<Item>>>of());
        
        for (ItemEquivGenerator delegate: delegates) {
            SuggestedEquivalents<Item> equivalents = delegate.equivalentsFor(item);
            suggestions = SuggestedEquivalents.merge(suggestions, equivalents);
        }
        
        return suggestions;
    }

    @Override
    public Set<Publisher> supportedPublishers() {
        return supportedPublishers;
    }
}
