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
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.SearchResolver;
import org.atlasapi.search.model.SearchQuery;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.query.Selection;

public class TitleMatchingEquivalenceScoringGenerator implements ContentEquivalenceGenerator<Container>, ContentEquivalenceScorer<Container> {

    private final static float TITLE_WEIGHTING = 1.0f;
    private final static float BROADCAST_WEIGHTING = 0.0f;
    private final static float CATCHUP_WEIGHTING = 0.0f;

    private final SearchResolver searchResolver;

    public TitleMatchingEquivalenceScoringGenerator(SearchResolver searchResolver) {
        this.searchResolver = searchResolver;
    }

    @Override
    public ScoredEquivalents<Container> generate(Container content, ResultDescription desc) {
        desc.startStage("Title-matching generator");
        ScoredEquivalents<Container> scores = scoreSuggestions(content, Iterables.filter(searchForEquivalents(content), Container.class), desc);
        desc.finishStage();
        return scores;
    }

    @Override
    public ScoredEquivalents<Container> score(Container content, Iterable<Container> suggestions, ResultDescription desc) {
        desc.startStage("Title-matching scorer");
        ScoredEquivalents<Container> scores = scoreSuggestions(content, suggestions, desc);
        desc.finishStage();
        return scores;
    }

    private ScoredEquivalents<Container> scoreSuggestions(Container content, Iterable<Container> suggestions, ResultDescription desc) {
        ScoredEquivalentsBuilder<Container> equivalents = DefaultScoredEquivalents.fromSource("Title");
        desc.appendText("Scoring %s suggestions", Iterables.size(suggestions));
        
        for (Container found : ImmutableSet.copyOf(suggestions)) {
            Score score = score(content.getTitle(), found.getTitle());
            desc.appendText("%s (%s) scored %s", found.getTitle(), found.getCanonicalUri(), score);
            equivalents.addEquivalent(found, score);
        }

        return equivalents.build();
    }

    private Score score(String subjectTitle, String equivalentTitle) {
        subjectTitle = removeCommonPrefixes(alphaNumeric(subjectTitle));
        equivalentTitle = removeCommonPrefixes(alphaNumeric(equivalentTitle));
        System.out.println(String.format("%s : %s", subjectTitle, equivalentTitle));
        double commonPrefix = commonPrefixLength(subjectTitle, equivalentTitle);
        double difference = Math.abs(equivalentTitle.length() - commonPrefix) / equivalentTitle.length();
        return Score.valueOf(commonPrefix / (subjectTitle.length() / 1.0) - difference);
    }

    private String removeCommonPrefixes(String alphaNumeric) {
        return (alphaNumeric.startsWith("the ") ? alphaNumeric.substring(4) : alphaNumeric).replace(" ", "");
    }

    private String alphaNumeric(String title) {
        return title.replaceAll(" & ", " and ").replaceAll("[^\\d\\w\\s]", "").toLowerCase();
    }

    private double commonPrefixLength(String t1, String t2) {
        int i = 0;
        for (; i < Math.min(t1.length(), t2.length()) && t1.charAt(i) == t2.charAt(i); i++) {
        }
        return i;
    }

    private List<Identified> searchForEquivalents(Container content) {
        Set<Publisher> publishers = Sets.difference(ImmutableSet.copyOf(Publisher.values()), ImmutableSet.of(content.getPublisher()));
        ApplicationConfiguration appConfig = ApplicationConfiguration.DEFAULT_CONFIGURATION.withSources(enabledPublishers(publishers));

        List<Identified> search = searchResolver.search(new SearchQuery(content.getTitle(), new Selection(0, 10), publishers, TITLE_WEIGHTING, BROADCAST_WEIGHTING, CATCHUP_WEIGHTING, Maybe.<Float>nothing(), Maybe.<Float>nothing()), appConfig);
        return search;
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
