package org.atlasapi.remotesite.wikipedia.television;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Series;

import com.google.common.collect.ImmutableSet;

public class TvBrandHierarchy {
    private final Brand brand;
    private final ImmutableSet<Series> seasons;
    private final ImmutableSet<Episode> episodes;

    public Brand getBrand() {
        return brand;
    }

    public ImmutableSet<Series> getSeasons() {
        return seasons;
    }

    public ImmutableSet<Episode> getEpisodes() {
        return episodes;
    }

    public TvBrandHierarchy(Brand brand, ImmutableSet<Series> seasons, ImmutableSet<Episode> episodes) {
        this.brand = brand;
        this.seasons = seasons;
        this.episodes = episodes;
    }
}
