package org.atlasapi.equiv.generators;

import java.util.List;
import java.util.Set;

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

public class TitleMatchingContainerEquivalenceGenerator implements ContentEquivalenceGenerator<Container> {

    private final SearchResolver searchResolver;

    public TitleMatchingContainerEquivalenceGenerator(SearchResolver searchResolver) {
        this.searchResolver = searchResolver;
    }
    
    @Override
    public ScoredEquivalents<Container> generateEquivalences(Container content, Set<Container> suggestions) {
        ScoredEquivalentsBuilder<Container> equivalents = DefaultScoredEquivalents.fromSource("Title");
        
        List<Identified> search = searchForEquivalents(content);
        
        for (Container found : ImmutableSet.copyOf(Iterables.concat(Iterables.filter(search, Container.class), suggestions))) {
            if(content.getMediaType().equals(found.getMediaType())) {
                equivalents.addEquivalent(found, score(content.getTitle(), found.getTitle()));
            }
        }
        
        return equivalents.build();
    }
    
    private double score(String subjectTitle, String equivalentTitle) {
        subjectTitle = alphaNumeric(subjectTitle);
        equivalentTitle = alphaNumeric(equivalentTitle);
        double commonPrefix = commonPrefixLength(subjectTitle, equivalentTitle);
        double difference = Math.abs(equivalentTitle.length() - commonPrefix) / equivalentTitle.length();
        return commonPrefix / (subjectTitle.length()/1.0) - difference;
    }

    private String alphaNumeric(String title) {
        return title.replaceAll("[^\\d\\w]", "");
    }

    private double commonPrefixLength(String t1, String t2) {
        int i = 0;
        for(; i < Math.min(t1.length(), t2.length()) && t1.charAt(i)==t2.charAt(i); i++){}
        return i;
    }

    private List<Identified> searchForEquivalents(Container content) {
        SetView<Publisher> publishers = Sets.difference(ImmutableSet.copyOf(Publisher.values()), ImmutableSet.of(content.getPublisher()));
        ApplicationConfiguration appConfig = ApplicationConfiguration.DEFAULT_CONFIGURATION.copyWithIncludedPublishers(publishers);
        
        List<Identified> search = searchResolver.search(new Search(content.getTitle()), publishers, appConfig , new Selection(0, 10));
        return search;
    }
    
}
