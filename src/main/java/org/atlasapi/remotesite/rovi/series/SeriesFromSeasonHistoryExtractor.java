package org.atlasapi.remotesite.rovi.series;

import static org.atlasapi.remotesite.rovi.RoviConstants.DEFAULT_PUBLISHER;
import static org.atlasapi.remotesite.rovi.RoviUtils.canonicalUriForProgram;
import static org.atlasapi.remotesite.rovi.RoviUtils.canonicalUriForSeason;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.ParentRef;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Series;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.remotesite.ContentExtractor;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.base.Maybe;


public class SeriesFromSeasonHistoryExtractor implements ContentExtractor<RoviSeasonHistoryLine, Series> {

    private final ContentResolver contentResolver;
    private final LoadingCache<String, Optional<Publisher>> parentPublisherCache = CacheBuilder.newBuilder()
            .maximumSize(100000)
            .build(new CacheLoader<String, Optional<Publisher>>() {

                public Optional<Publisher> load(String parentId) {
                    return getParentPublisher(parentId);
                }
            });
    
    public SeriesFromSeasonHistoryExtractor(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }
    
    @Override
    public Series extract(RoviSeasonHistoryLine season) {
        Series series = new Series();
        
        Optional<Publisher> parentPublisher = Optional.absent();
        
        String parentCanonicalUri = canonicalUriForProgram(season.getSeriesId());
        parentPublisher = parentPublisherCache.getUnchecked(parentCanonicalUri);

        if (parentPublisher.isPresent()) {
            series.setPublisher(parentPublisher.get());
        } else {
            series.setPublisher(DEFAULT_PUBLISHER);
        }
        
        series.setCanonicalUri(canonicalUriForSeason(season.getSeasonProgramId()));
        series.setParentRef(new ParentRef(parentCanonicalUri));
        
        if (season.getSeasonName().isPresent()) {
            series.setTitle(season.getSeasonName().get());
        }
        
        if (season.getSeasonNumber().isPresent()) {
            series.withSeriesNumber(season.getSeasonNumber().get());
        }
        
        return series;
    }
    
    private Optional<Publisher> getParentPublisher(String parentCanonicalUri) {
        Maybe<Identified> maybeParent = contentResolver.findByCanonicalUris(ImmutableList.of(parentCanonicalUri)).getFirstValue();
        
        if (maybeParent.hasValue()) {
            Content parent = (Content) maybeParent.requireValue();
            return Optional.of(parent.getPublisher());
        }
        
        return Optional.absent();
    }
    
    public void clearCache() {
        parentPublisherCache.invalidateAll();
    }

}
