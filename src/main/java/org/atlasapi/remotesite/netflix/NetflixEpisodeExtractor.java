package org.atlasapi.remotesite.netflix;

import java.util.Set;

import nu.xom.Element;

import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.ParentRef;

import com.google.common.collect.ImmutableSet;

public class NetflixEpisodeExtractor extends NetflixContentExtractor<Episode> {
    
    private static final String EPISODE_URL_PREFIX = "http://gb.netflix.com/episodes/";
    private static final String LOCATIONS_URL_PREFIX = "http://movies.netflix.com/WiPlayer?movieid=";
    private static final String EPISODE_PARAM = "&EpisodeMovieId=";

    @Override
    Set<Episode> extract(Element source, int id) {
        Episode episode = new Episode();

        episode.setCanonicalUri(EPISODE_URL_PREFIX + id);

        episode.setTitle(getTitle(source));
        episode.setDescription(getDescription(source));
        episode.addVersion(getVersion(source, getEncoding(LOCATIONS_URL_PREFIX + getShowId(source) + EPISODE_PARAM + id)));
        episode.setYear(getYear(source));
        episode.setGenres(getGenres(source));
        episode.setCertificates(getCertificates(source));
        episode.addAlias(getAlias(source));
        episode.setParentRef(getParentRef(source));
        episode.setSeriesRef(getSeriesRef(source));
        episode.setEpisodeNumber(getEpisodeNumber(source));
        episode.setSeriesNumber(getSeriesNumber(source));
        episode.setPublisher(getPublisher());

        return ImmutableSet.<Episode>builder().add(episode).build();
    }

    private ParentRef getParentRef(Element episodeElement) {
        return new ParentRef(NetflixBrandExtractor.BRAND_URL_PREFIX + getShowId(episodeElement));
    }

    private ParentRef getSeriesRef(Element episodeElement) {
        return new ParentRef(NetflixSeriesExtractor.SERIES_URL_PREFIX + getShowId(episodeElement) + "-" + getSeriesNumber(episodeElement));
    }

    @Override
    String getTitle(Element episodeElement) {
        Element titleElement = episodeElement.getFirstChildElement(NetflixContentExtractor.TITLE_KEY);
        if (titleElement != null) {
            String[] parts = titleElement.getValue().split(":");
            // return last part of title, trimmed of whitespace
            // leading and trailing quotes removed if present
            return parts[parts.length - 1].trim().replaceAll("^\"|\"$", "");
        }
        throw new ElementNotFoundException(episodeElement, NetflixContentExtractor.TITLE_KEY);
    }

}
