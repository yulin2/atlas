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
import com.metabroadcast.common.query.Selection;

public class TitleMatchingEquivalenceScoringGenerator implements ContentEquivalenceGenerator<Container>, ContentEquivalenceScorer<Container> {

    private final static float TITLE_WEIGHTING = 1.0f;
    private final static float BROADCAST_WEIGHTING = 0.0f;
    private final static float CATCHUP_WEIGHTING = 0.0f;
    
    private final Set<Publisher> searchPublishers = ImmutableSet.of(
            Publisher.BBC, Publisher.C4, Publisher.HULU, Publisher.YOUTUBE, Publisher.TED, 
            Publisher.VIMEO, Publisher.ITV, Publisher.BLIP, Publisher.DAILYMOTION, 
            Publisher.FLICKR, Publisher.FIVE, Publisher.SEESAW, Publisher.TVBLOB, 
            Publisher.ICTOMORROW, Publisher.HBO, Publisher.ITUNES, Publisher.MSN_VIDEO, 
            Publisher.PA, Publisher.RADIO_TIMES, Publisher.ARCHIVE_ORG, Publisher.WORLD_SERVICE, Publisher.BBC_REDUX
    );
    
    private final SearchResolver searchResolver;
    private final ContentTitleScorer titleScorer;

    public TitleMatchingEquivalenceScoringGenerator(SearchResolver searchResolver) {
        this.searchResolver = searchResolver;
        this.titleScorer = new ContentTitleScorer();
    }

    @Override
    public ScoredEquivalents<Container> generate(Container content, ResultDescription desc) {
        return scoreSuggestions(content, Iterables.filter(searchForEquivalents(content), Container.class), desc);
    }

    @Override
    public ScoredEquivalents<Container> score(Container content, Iterable<Container> suggestions, ResultDescription desc) {
        return scoreSuggestions(content, suggestions, desc);
    }

    private ScoredEquivalents<Container> scoreSuggestions(Container content, Iterable<Container> suggestions, ResultDescription desc) {
        ScoredEquivalentsBuilder<Container> equivalents = DefaultScoredEquivalents.fromSource("Title");
        desc.appendText("Scoring %s suggestions", Iterables.size(suggestions));
        
        for (Container found : ImmutableSet.copyOf(suggestions)) {
            Score score = titleScorer.score(content, found);
            desc.appendText("%s (%s) scored %s", found.getTitle(), found.getCanonicalUri(), score);
            equivalents.addEquivalent(found, score);
        }

        return equivalents.build();
    }

    private List<Identified> searchForEquivalents(Container content) {
        Set<Publisher> publishers = Sets.difference(searchPublishers, ImmutableSet.of(content.getPublisher()));
        ApplicationConfiguration appConfig = ApplicationConfiguration.DEFAULT_CONFIGURATION.withSources(enabledPublishers(publishers));

        List<Identified> search = searchResolver.search(new SearchQuery(content.getTitle(), new Selection(0, 20), publishers, TITLE_WEIGHTING, BROADCAST_WEIGHTING, CATCHUP_WEIGHTING), appConfig);
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
