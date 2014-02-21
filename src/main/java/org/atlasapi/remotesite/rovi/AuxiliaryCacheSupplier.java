package org.atlasapi.remotesite.rovi;

import static org.atlasapi.remotesite.rovi.RoviUtils.canonicalUriForSeason;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Series;
import org.atlasapi.persistence.content.ContentResolver;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.base.Maybe;


public class AuxiliaryCacheSupplier {
    
    private final ContentResolver contentResolver;
    
    public AuxiliaryCacheSupplier(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    public LoadingCache<String, Optional<Publisher>> parentPublisherCache(int maximumSize) {
            return CacheBuilder.newBuilder()
            .maximumSize(maximumSize)
            .build(new CacheLoader<String, Optional<Publisher>>() {

                public Optional<Publisher> load(String parentId) {
                    return getParentPublisher(parentId);
                }
            });
    }
    
    private Optional<Publisher> getParentPublisher(String parentCanonicalUri) {
        Maybe<Identified> maybeParent = contentResolver.findByCanonicalUris(ImmutableList.of(parentCanonicalUri)).getFirstValue();
        
        if (maybeParent.hasValue()) {
            Content parent = (Content) maybeParent.requireValue();
            return Optional.of(parent.getPublisher());
        }
        
        return Optional.absent();
    }
    
    public LoadingCache<String, Optional<Integer>> seasonNumberCache(int maximumSize) {
        return CacheBuilder.newBuilder()
                .maximumSize(maximumSize)
                .build(new CacheLoader<String, Optional<Integer>>() {

                    public Optional<Integer> load(String seasonId) {
                        return getSeasonNumberResolvingSeason(seasonId);
                    }
                });
    }
    
    private Optional<Integer> getSeasonNumberResolvingSeason(String seasonId) {
        String seasonCanonicalUri = canonicalUriForSeason(seasonId);
        Maybe<Identified> maybeSeason = contentResolver.findByCanonicalUris(ImmutableList.of(seasonCanonicalUri)).getFirstValue();
        
        if (maybeSeason.hasValue() && maybeSeason.requireValue() instanceof Series) {
            Series season = (Series) maybeSeason.requireValue();
            return Optional.fromNullable(season.getSeriesNumber());
        }
        
        return Optional.absent();
    }
}
