package org.atlasapi.equiv.generators;

import java.util.List;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.equiv.results.DefaultScoredEquivalents;
import org.atlasapi.equiv.results.DefaultScoredEquivalents.ScoredEquivalentsBuilder;
import org.atlasapi.equiv.results.ScoredEquivalents;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.SearchResolver;
import org.atlasapi.search.model.Search;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.metabroadcast.common.query.Selection;

public class TitleMatchingContainerEquivalenceGenerator implements ContentEquivalenceGenerator<Container<?>> {

    private final SearchResolver searchResolver;

    public TitleMatchingContainerEquivalenceGenerator(SearchResolver searchResolver) {
        this.searchResolver = searchResolver;
    }
    
    @Override
    public ScoredEquivalents<Container<?>> generateEquivalences(Container<?> content) {
        ScoredEquivalentsBuilder<Container<?>> equivalents = DefaultScoredEquivalents.fromSource("Title");
        
        List<Identified> search = searchForEquivalents(content);
        
        for (Container<?> found : Iterables.filter(search, Container.class)) {
            equivalents.addEquivalent(found, 1.0);
        }
        
        return equivalents.build();
    }
    
    private List<Identified> searchForEquivalents(Container<?> content) {
        SetView<Publisher> publishers = Sets.difference(ImmutableSet.copyOf(Publisher.values()), ImmutableSet.of(content.getPublisher()));
        ApplicationConfiguration appConfig = ApplicationConfiguration.DEFAULT_CONFIGURATION.copyWithIncludedPublishers(publishers);
        
        List<Identified> search = searchResolver.search(new Search(content.getTitle()), publishers, appConfig , new Selection(0, 10));
        return search;
    }

}
