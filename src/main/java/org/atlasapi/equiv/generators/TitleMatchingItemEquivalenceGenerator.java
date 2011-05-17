package org.atlasapi.equiv.generators;

import java.util.List;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.equiv.results.ScoredEquivalents;
import org.atlasapi.equiv.results.ScoredEquivalents.ScoredEquivalentsBuilder;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.SearchResolver;
import org.atlasapi.search.model.Search;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.metabroadcast.common.query.Selection;

public class TitleMatchingItemEquivalenceGenerator implements ContentEquivalenceGenerator<Item> {

    private final SearchResolver searchResolver;

    public TitleMatchingItemEquivalenceGenerator(SearchResolver searchResolver) {
        this.searchResolver = searchResolver;
    }
    
    @Override
    public ScoredEquivalents<Item> generateEquivalences(Item content) {
        ScoredEquivalentsBuilder<Item> equivalents = ScoredEquivalents.fromSource("Title");
        
        List<Identified> search = searchForEquivalents(content);
        
        for (Item found : Iterables.filter(search, Item.class)) {
            equivalents.addEquivalent(found, 1.0);
        }
        
        return equivalents.build();
    }
    
    private List<Identified> searchForEquivalents(Item content) {
        SetView<Publisher> publishers = Sets.difference(ImmutableSet.copyOf(Publisher.values()), ImmutableSet.of(content.getPublisher()));
        ApplicationConfiguration appConfig = ApplicationConfiguration.DEFAULT_CONFIGURATION.copyWithIncludedPublishers(publishers);
        
        List<Identified> search = searchResolver.search(new Search(content.getTitle()), publishers, appConfig , new Selection(0, 10));
        return search;
    }

}
