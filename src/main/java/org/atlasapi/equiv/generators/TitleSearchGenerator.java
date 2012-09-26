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
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.SearchResolver;
import org.atlasapi.search.model.SearchQuery;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.metabroadcast.common.query.Selection;

public class TitleSearchGenerator implements EquivalenceGenerator<Container> {

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
    private final ContentTitleScorer<Container> scorer;

    public TitleSearchGenerator(SearchResolver searchResolver) {
        this.searchResolver = searchResolver;
        this.scorer = new ContentTitleScorer<Container>();
    }

    @Override
    public ScoredCandidates<Container> generate(Container content, ResultDescription desc) {
        List<Identified> candidates = searchForCandidates(content);
        Iterable<Container> candidateContainers = Iterables.filter(candidates, Container.class);
        return scorer.scoreCandidates(content, candidateContainers, desc);
    }

    private List<Identified> searchForCandidates(Container content) {
        Set<Publisher> publishers = Sets.difference(searchPublishers, ImmutableSet.of(content.getPublisher()));
        ApplicationConfiguration appConfig = ApplicationConfiguration.DEFAULT_CONFIGURATION.withSources(enabledPublishers(publishers));

        List<Identified> search = searchResolver.search(new SearchQuery(content.getTitle(), new Selection(0, 20), publishers, TITLE_WEIGHTING, BROADCAST_WEIGHTING, CATCHUP_WEIGHTING), appConfig);
        return search;
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
    
    public static void main(String[] args) throws IOException {
        URL url = new URL(null);
        System.out.println(url);
    }
}
