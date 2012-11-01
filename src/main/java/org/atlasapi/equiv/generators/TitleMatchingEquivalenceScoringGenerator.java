package org.atlasapi.equiv.generators;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.application.SourceStatus;
import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.DefaultScoredEquivalents;
import org.atlasapi.equiv.results.scores.DefaultScoredEquivalents.ScoredEquivalentsBuilder;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredEquivalents;
import org.atlasapi.equiv.scorers.ContentEquivalenceScorer;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.SearchResolver;
import org.atlasapi.search.model.SearchQuery;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.metabroadcast.common.query.Selection;

public class TitleMatchingEquivalenceScoringGenerator<T extends Content> implements ContentEquivalenceGenerator<T>, ContentEquivalenceScorer<T> {

    private final static float TITLE_WEIGHTING = 1.0f;
    
    public static final <T extends Content> TitleMatchingEquivalenceScoringGenerator<T> create(SearchResolver searchResolver, Class<? extends T> cls) {
        return new TitleMatchingEquivalenceScoringGenerator<T>(searchResolver, cls, ImmutableSet.<Publisher>of());
    }
    
    private final SearchResolver searchResolver;
    private final ContentTitleScorer titleScorer;
    private final Class<? extends T> cls;

    private final Set<Publisher> searchPublishers;

    public TitleMatchingEquivalenceScoringGenerator(SearchResolver searchResolver, Class<? extends T> cls, Iterable<Publisher> publishers) {
        this.searchResolver = searchResolver;
        this.cls = cls;
        this.searchPublishers = ImmutableSet.copyOf(publishers);
        this.titleScorer = new ContentTitleScorer();
    }
    
    public TitleMatchingEquivalenceScoringGenerator<T> copyWithPublishers(Iterable<Publisher> publishers) {
        return new TitleMatchingEquivalenceScoringGenerator<T>(searchResolver, cls, publishers);
    }

    @Override
    public ScoredEquivalents<T> generate(T content, ResultDescription desc) {
        return scoreSuggestions(content, searchForEquivalents(content), desc);
    }

    @Override
    public ScoredEquivalents<T> score(T content, Iterable<T> suggestions, ResultDescription desc) {
        return scoreSuggestions(content, suggestions, desc);
    }

    private ScoredEquivalents<T> scoreSuggestions(T content, Iterable<? extends T> suggestions, ResultDescription desc) {
        ScoredEquivalentsBuilder<T> equivalents = DefaultScoredEquivalents.fromSource("Title");
        desc.appendText("Scoring %s suggestions", Iterables.size(suggestions));
        
        for (T found : ImmutableSet.copyOf(suggestions)) {
            Score score = titleScorer.score(content, found);
            desc.appendText("%s (%s) scored %s", found.getTitle(), found.getCanonicalUri(), score);
            equivalents.addEquivalent(found, score);
        }

        return equivalents.build();
    }

    private Iterable<? extends T> searchForEquivalents(T content) {
        Set<Publisher> publishers = Sets.difference(searchPublishers, ImmutableSet.of(content.getPublisher()));
        ApplicationConfiguration appConfig = ApplicationConfiguration.DEFAULT_CONFIGURATION.withSources(enabledPublishers(publishers));

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
        Builder<Publisher, SourceStatus> builder = ImmutableMap.builder();
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
        return "Title-matching Scorer/Generator";
    }
}
