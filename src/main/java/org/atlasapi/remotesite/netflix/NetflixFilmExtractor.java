package org.atlasapi.remotesite.netflix;

import java.util.Set;

import nu.xom.Element;

import org.atlasapi.media.entity.Film;

import com.google.common.base.Throwables;
import com.google.inject.internal.ImmutableSet;

public class NetflixFilmExtractor extends NetflixContentExtractor<Film>  {
    
    private static final String MOVIES_URL_PREFIX = "http://gb.netflix.com/movies/";
    
    @Override
    public Set<Film> extract(Element source, int id) {
        try {
            Film film = new Film();

            film.setCanonicalUri(MOVIES_URL_PREFIX + id);

            film.setTitle(getTitle(source));
            film.setYear(getYear(source));
            film.setDescription(getDescription(source));
            film.addVersion(getVersion(source));
            film.setGenres(getGenres(source));
            film.setPeople(getPeople(source));
            film.setCertificates(getCertificates(source));
            film.addAlias(getAlias(source));

            return ImmutableSet.<Film>builder().add(film).build();
        } catch (Exception e) {
            Throwables.propagate(e);
            // never get here
            return null;
        }
    }
}
