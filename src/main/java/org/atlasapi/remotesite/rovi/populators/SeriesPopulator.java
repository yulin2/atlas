package org.atlasapi.remotesite.rovi.populators;

import static com.google.common.base.Preconditions.checkArgument;
import static org.atlasapi.remotesite.rovi.RoviConstants.DEFAULT_PUBLISHER;
import static org.atlasapi.remotesite.rovi.RoviUtils.canonicalUriForProgram;
import static org.atlasapi.remotesite.rovi.RoviUtils.canonicalUriForSeason;
import static org.atlasapi.remotesite.rovi.RoviUtils.canonicalUriForSeasonHistory;
import static org.atlasapi.remotesite.rovi.model.ActionType.DELETE;

import org.atlasapi.media.entity.ParentRef;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Series;
import org.atlasapi.remotesite.rovi.model.RoviSeasonHistoryLine;

import com.google.common.base.Optional;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;


public class SeriesPopulator implements ContentPopulator<Series> {
    
    private final RoviSeasonHistoryLine season;
    private final LoadingCache<String, Optional<Publisher>> parentPublisherCache;
    
    public SeriesPopulator(RoviSeasonHistoryLine season,
            LoadingCache<String, Optional<Publisher>> parentPublisherCache) {
        checkArgument(!season.getActionType().equals(DELETE), "It's not possible to populate a Series from a deletion");
        
        this.season = season;
        this.parentPublisherCache = parentPublisherCache;
    }

    @Override
    public void populateContent(Series series) {
        Optional<Publisher> parentPublisher = Optional.absent();

        String parentCanonicalUri = canonicalUriForProgram(season.getSeriesId().get());
        parentPublisher = parentPublisherCache.getUnchecked(parentCanonicalUri);

        if (parentPublisher.isPresent()) {
            series.setPublisher(parentPublisher.get());
        } else if (series.getPublisher() == null) {
            series.setPublisher(DEFAULT_PUBLISHER);
        }

        series.setCanonicalUri(canonicalUriForSeason(season.getSeasonProgramId().get()));
        series.setParentRef(new ParentRef(parentCanonicalUri));

        if (season.getSeasonName().isPresent()) {
            series.setTitle(season.getSeasonName().get());
        }

        if (season.getSeasonNumber().isPresent()) {
            series.withSeriesNumber(season.getSeasonNumber().get());
        }
        
        series.setAliasUrls(ImmutableList.of(canonicalUriForSeasonHistory(season.getSeasonHistoryId())));
    }

}
