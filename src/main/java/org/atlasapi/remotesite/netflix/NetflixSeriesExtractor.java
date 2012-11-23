package org.atlasapi.remotesite.netflix;

import java.util.Set;

import nu.xom.Element;

import org.atlasapi.media.entity.Series;

import com.google.common.collect.ImmutableSet;

public class NetflixSeriesExtractor extends NetflixContentExtractor<Series> {
    
    public static final String SERIES_URL_PREFIX = "http://gb.netflix.com/seasons/";

    @Override
    Set<Series> extract(Element source, int id) {
        Series series = new Series();

        series.setCanonicalUri(SERIES_URL_PREFIX + getShowId(source) + "-" + getSeriesNumber(source));

        series.setTitle(getTitle(source));
        series.withSeriesNumber(getSeriesNumber(source));

        return ImmutableSet.<Series>builder().add(series).build();
    }

    String getTitle(Element filmElement) throws ElementNotFoundException {
        Element titleElement = filmElement.getFirstChildElement(NetflixContentExtractor.TITLE_KEY);
        if (titleElement != null) {
            String[] parts = titleElement.getValue().split(":");
            // return second part of title, trimmed of whitespace
            return parts[1].trim();
        }
        throw new ElementNotFoundException(filmElement, NetflixContentExtractor.TITLE_KEY);
    }

}
