package org.atlasapi.remotesite.netflix;

import java.util.Set;

import nu.xom.Element;

import org.atlasapi.media.entity.Episode;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;

public class NetflixEpisodeExtractor extends NetflixContentExtractor<Episode> {
    
    private static final String EPISODE_URL_PREFIX = "http://gb.netflix.com/episodes/";

    @Override
    Set<Episode> extract(Element source, int id) {
        try {
            Episode episode = new Episode();

            episode.setCanonicalUri(EPISODE_URL_PREFIX + id + "-" + getSeriesNumber(source));

            episode.setTitle(getTitle(source));
            episode.setDescription(getDescription(source));
            episode.addVersion(getVersion(source));
            episode.setYear(getYear(source));
            episode.setGenres(getGenres(source));
            episode.setCertificates(getCertificates(source));
            episode.addAlias(getAlias(source));
            // TODO series_summary
            // TODO container
            episode.setEpisodeNumber(getEpisodeNumber(source));
            episode.setSeriesNumber(getSeriesNumber(source));

            return ImmutableSet.<Episode>builder().add(episode).build();
        } catch (Exception e) {
            Throwables.propagate(e);
            // never get here
            return null;
        }
    }

    String getTitle(Element filmElement) throws ElementNotFoundException {
        Element titleElement = filmElement.getFirstChildElement(NetflixContentExtractor.TITLE_KEY);
        if (titleElement != null) {
            String[] parts = titleElement.getValue().split(":");
            // return last part of title, trimmed of whitespace
            return parts[parts.length - 1].trim();
        }
        throw new ElementNotFoundException(filmElement, NetflixContentExtractor.TITLE_KEY);
    }

}
