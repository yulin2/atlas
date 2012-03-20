package org.atlasapi.remotesite.itunes.epf;

import org.atlasapi.media.content.Series;

public class SeriesVideoIdentifier {

    private final Series series;
    private final Integer trackNumber;

    public SeriesVideoIdentifier(Series series, Integer trackNumber) {
        this.series = series;
        this.trackNumber = trackNumber;
    }

    public Series series() {
        return this.series;
    }

    public Integer trackNumber() {
        return this.trackNumber;
    }

}
