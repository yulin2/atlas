package org.atlasapi.remotesite.channel4;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Series;

import com.google.common.collect.SetMultimap;

public class BrandSeriesAndEpisodes {

    private final Brand brand;
    private final SetMultimap<Series, Episode> seriesAndEpisodes;

    public BrandSeriesAndEpisodes(Brand brand, SetMultimap<Series, Episode> seriesAndEpisodes) {
        this.brand = brand;
        this.seriesAndEpisodes = seriesAndEpisodes;
    }

    public Brand getBrand() {
        return this.brand;
    }

    public SetMultimap<Series, Episode> getSeriesAndEpisodes() {
        return this.seriesAndEpisodes;
    }

}
