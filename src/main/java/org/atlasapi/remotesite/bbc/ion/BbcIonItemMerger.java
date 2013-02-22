package org.atlasapi.remotesite.bbc.ion;

import java.util.Map;

import org.atlasapi.media.content.Container;
import org.atlasapi.media.content.Content;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Film;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Series;
import org.atlasapi.media.entity.Version;
import org.joda.time.Duration;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class BbcIonItemMerger {

    public Item merge(Item fetchedItem, Item existingItem) {
        if (fetchedItem instanceof Film) {
            return mergeFetchedFilm((Film) fetchedItem, existingItem);
        } else if (fetchedItem instanceof Episode) {
            return mergeFetchedEpisode((Episode) fetchedItem, existingItem);
        } else { //Fetched item is stand-alone.
            return mergeFetchedItem(fetchedItem, existingItem);
        }
    }
    
    private Film mergeFetchedFilm(Film fetchedFilm, Item existingItem) {
        Film existingFilm;
        if (existingItem instanceof Film) {
            existingFilm = (Film) existingItem;
        } else {
            existingFilm = new Film();
            Item.copyTo(existingItem, existingFilm);
        }
        return mergeFilms(fetchedFilm, existingFilm);
    }

    private Film mergeFilms(Film fetchedFilm, Film existingFilm) {
        Film mergedFilms = mergeItems(fetchedFilm, existingFilm);
        
        mergedFilms.setYear(ifNotNull(fetchedFilm.getYear(), existingFilm.getYear()));
        mergedFilms.setWebsiteUrl(ifNotNull(fetchedFilm.getWebsiteUrl(), existingFilm.getWebsiteUrl()));
        
        return mergedFilms;
    }
    
    private Episode mergeFetchedEpisode(Episode fetchedEpisode, Item existingItem) {
        Episode existingEpisode;
        if (existingItem instanceof Episode) {
            existingEpisode = (Episode) existingItem;
        } else {
            existingEpisode = new Episode();
            Item.copyTo(existingItem, existingEpisode);
        }
        return mergeEpisodes(fetchedEpisode, existingEpisode);
    }

    private Episode mergeEpisodes(Episode fetchedEpisode, Episode existingEpisode) {
        Episode mergedEpisode = mergeItems(fetchedEpisode, existingEpisode);
        
        mergedEpisode.setEpisodeNumber(ifNotNull(fetchedEpisode.getEpisodeNumber(), existingEpisode.getEpisodeNumber()));
        mergedEpisode.setSeriesNumber(ifNotNull(fetchedEpisode.getSeriesNumber(), existingEpisode.getSeriesNumber()));
        mergedEpisode.setPartNumber(ifNotNull(fetchedEpisode.getPartNumber(), existingEpisode.getPartNumber()));
        mergedEpisode.setSeriesRef(fetchedEpisode.getSeriesRef());
        
        return mergedEpisode;
    }

    private Item mergeFetchedItem(Item fetchedItem, Item existingItem) {
        if (existingItem instanceof Episode || existingItem instanceof Film) {
            //existing is not just an item, so copy its details to a new one.
            Item newItem = new Item();
            Item.copyTo(existingItem, newItem);
            existingItem = newItem;
        }
        return mergeItems(fetchedItem, existingItem);
    }

    private <T extends Item> T mergeItems(T fetchedItem, T existingItem) {
        mergeContents(fetchedItem, existingItem);
        
        //Item attrs.
        existingItem.setParentRef(fetchedItem.getContainer());
        existingItem.setPeople(ImmutableList.copyOf(Sets.newLinkedHashSet(
            Iterables.concat(fetchedItem.getPeople(), existingItem.getPeople())
        )));

        //Versions...
        Map<String,Version> existingVersionMap = Maps.uniqueIndex(existingItem.getVersions(), Identified.TO_URI);
        for (Version fetchedVersion : fetchedItem.getVersions()) {
            Version existingVersion = existingVersionMap.get(fetchedVersion.getCanonicalUri());
            if(existingVersion != null) {
                mergeVersions(fetchedVersion, existingVersion);
            } else {
                existingItem.addVersion(fetchedVersion);
            }
        }
        
        return existingItem;
    }
    
    public Container mergeContainers(Container fetchedContainer, Container existingContainer) {
        if (fetchedContainer instanceof Series) {
            return mergeSeries((Series)fetchedContainer, existingContainer);
        }
        if (fetchedContainer instanceof Brand) {
            return mergeBrands((Brand)fetchedContainer, existingContainer);
        }
        throw new RuntimeException("Can't merge container of type " + fetchedContainer.getClass().getSimpleName());
    }

    private Brand mergeBrands(Brand fetchedBrand, Container existingContainer) {
        Brand existingBrand;
        if (existingContainer instanceof Brand) {
            existingBrand = (Brand) existingContainer;
        } else {
            existingBrand = new Brand();
            Brand.copyTo(existingContainer, existingBrand);
        }
        mergeContents(fetchedBrand, existingBrand);
        return existingBrand;
    }

    private Series mergeSeries(Series fetchedSeries, Container existingContainer) {
        Series existingSeries;
        if (existingContainer instanceof Series) {
            existingSeries = (Series) existingContainer;
        } else {
            existingSeries = new Series();
            Series.copyTo(existingContainer, existingSeries);
        }
        mergeContents(fetchedSeries, existingSeries);
        existingSeries.setParentRef(ifNotNull(fetchedSeries.getParent(), existingSeries.getParent()));
        existingSeries.withSeriesNumber(ifNotNull(fetchedSeries.getSeriesNumber(), existingSeries.getSeriesNumber()));
        return existingSeries;
    }

    private <T extends Content> void mergeContents(T fetchedItem, T existingItem) {
        //Identified attrs. Assume uri, curie are corrrect, equivs ignored.
        existingItem.addAliasUrls(fetchedItem.getAliasUrls());
        existingItem.setLastUpdated(ifNotNull(fetchedItem.getLastUpdated(), existingItem.getLastUpdated()));
        
        //Described attrs.
        existingItem.setTitle(ifNotNull(fetchedItem.getTitle(), existingItem.getTitle()));
        existingItem.setDescription(ifNotNull(fetchedItem.getDescription(), existingItem.getDescription()));
        existingItem.setMediaType(ifNotNull(fetchedItem.getMediaType(), existingItem.getMediaType()));
        existingItem.setSpecialization(ifNotNull(fetchedItem.getSpecialization(), existingItem.getSpecialization()));
        existingItem.setGenres(Iterables.concat(fetchedItem.getGenres(), existingItem.getGenres()));
        existingItem.setTags(ImmutableSet.copyOf(Iterables.concat(fetchedItem.getTags(), existingItem.getTags())));
        existingItem.setImage(ifNotNull(fetchedItem.getImage(), existingItem.getImage()));
        existingItem.setThumbnail(ifNotNull(fetchedItem.getThumbnail(), existingItem.getThumbnail()));
        existingItem.setPresentationChannel(ifNotNull(fetchedItem.getPresentationChannel(), existingItem.getPresentationChannel()));
        
        //Content attrs. Assume that clips themselves don't need merging.
        existingItem.setClips(ImmutableSet.copyOf(Iterables.concat(fetchedItem.getClips(), existingItem.getClips())));
    }
    
    private void mergeVersions(Version fetchedVersion, Version existingVersion) {
        Integer intDuration = ifNotNull(fetchedVersion.getDuration(), existingVersion.getDuration());
        if(intDuration != null) {
            existingVersion.setDuration(Duration.standardSeconds(intDuration));
        }
        existingVersion.setBroadcasts(Sets.newHashSet(Iterables.concat(fetchedVersion.getBroadcasts(), existingVersion.getBroadcasts())));
        existingVersion.setPublishedDuration(ifNotNull(fetchedVersion.getPublishedDuration(), existingVersion.getPublishedDuration()));
        existingVersion.setRestriction(ifNotNull(fetchedVersion.getRestriction(), existingVersion.getRestriction()));
        existingVersion.setManifestedAs(Sets.newHashSet(Iterables.concat(fetchedVersion.getManifestedAs(), existingVersion.getManifestedAs())));
    }

    private <T> T ifNotNull(T preferredVal, T defautVal) {
        return preferredVal != null ? preferredVal : defautVal;
    }

}
