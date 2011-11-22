package org.atlasapi.remotesite.itunes.epf;

import static com.google.common.base.Preconditions.checkNotNull;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.Series;
import org.atlasapi.remotesite.itunes.epf.model.EpfVideo;

public class ItunesEpfVideoSource {

    private final Brand brand;
    private final Series series;
    private final Integer episodeNumber;
    private final EpfVideo video;
    private final Iterable<Location> locations;

    public ItunesEpfVideoSource(EpfVideo video, Brand brand, Series series, Integer episodeNumber, Iterable<Location> locations) {
        this.video = checkNotNull(video);
        this.brand = brand;
        this.series = series;
        this.episodeNumber = episodeNumber;
        this.locations = locations;
    }

    public Brand parentBrand() {
        return this.brand;
    }

    public Series parentSeries() {
        return this.series;
    }

    public Integer episodeNumber() {
        return this.episodeNumber;
    }

    public EpfVideo video() {
        return this.video;
    }

    public Iterable<Location> locations() {
        return locations;
    }

}
