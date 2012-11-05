package org.atlasapi.equiv.generators;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.application.SourceStatus;
import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.ScoredCandidates;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.SearchResolver;
import org.atlasapi.search.model.SearchQuery;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.query.Selection;

public class TitleSearchGenerator<T extends Content> implements EquivalenceGenerator<T> {

    private final static float TITLE_WEIGHTING = 1.0f;
    
    public static final <T extends Content> TitleSearchGenerator<T> create(SearchResolver searchResolver, Class<? extends T> cls) {
        return new TitleSearchGenerator<T>(searchResolver, cls, ImmutableSet.<Publisher>of());
    }
    
    private final SearchResolver searchResolver;
    private final Class<? extends T> cls;
    private final Set<Publisher> searchPublishers;

    private final ContentTitleScorer<T> scorer;


    public TitleSearchGenerator(SearchResolver searchResolver, Class<? extends T> cls, Iterable<Publisher> publishers) {
        this.searchResolver = searchResolver;
        this.cls = cls;
        this.searchPublishers = ImmutableSet.copyOf(publishers);
        this.scorer = new ContentTitleScorer<T>();
    }
    
    public TitleSearchGenerator<T> copyWithPublishers(Iterable<Publisher> publishers) {
        return new TitleSearchGenerator<T>(searchResolver, cls, publishers);
    }

    @Override
    public ScoredCandidates<T> generate(T content, ResultDescription desc) {
        Iterable<? extends T> candidates = searchForCandidates(content);
        return scorer.scoreCandidates(content, candidates, desc);
    }

    private Iterable<? extends T> searchForCandidates(T content) {
        Set<Publisher> publishers = Sets.difference(searchPublishers, ImmutableSet.of(content.getPublisher()));
        ApplicationConfiguration appConfig = ApplicationConfiguration.defaultConfiguration().withSources(enabledPublishers(publishers));

        SearchQuery.Builder query = SearchQuery.builder(content.getTitle())
                .withSelection(new Selection(0, 20))
                .withPublishers(publishers)
                .withTitleWeighting(TITLE_WEIGHTING);
        if (content.getSpecialization() != null) {
            query.withSpecializations(ImmutableSet.of(content.getSpecialization()));
        }
        List<Identified> search = searchResolver.search(query.build(), appConfig);
        return Iterables.filter(search,cls);
    }

    private Map<Publisher, SourceStatus> enabledPublishers(Set<Publisher> enabledSources) {
        ImmutableMap.Builder<Publisher, SourceStatus> builder = ImmutableMap.builder();
        for (Publisher publisher : Publisher.values()) {
            if (enabledSources.contains(publisher)) {
                builder.put(publisher, SourceStatus.AVAILABLE_ENABLED);
            } else {
                builder.put(publisher, SourceStatus.AVAILABLE_DISABLED);
            }
        }
        return builder.build();
    }
    
    @Override
    public String toString() {
        return "Title-matching Generator";
    }

}
