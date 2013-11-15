package org.atlasapi.remotesite.wikipedia.television;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Series;

import com.google.common.collect.ImmutableCollection;

public class TvBrandHierarchy {
    private final Brand brand;
    private final ImmutableCollection<Series> seasons;
    private final ImmutableCollection<Episode> episodes;

    public Brand getBrand() {
        return brand;
    }

    public ImmutableCollection<Series> getSeasons() {
        return seasons;
    }

    public ImmutableCollection<Episode> getEpisodes() {
        return episodes;
    }

    public TvBrandHierarchy(Brand brand, ImmutableCollection<Series> seasons, ImmutableCollection<Episode> episodes) {
        this.brand = brand;
        this.seasons = seasons;
        this.episodes = episodes;
    }
}
