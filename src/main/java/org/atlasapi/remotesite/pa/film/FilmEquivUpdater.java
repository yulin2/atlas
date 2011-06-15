package org.atlasapi.remotesite.pa.film;

import java.util.List;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.media.entity.Film;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.SearchResolver;
import org.atlasapi.persistence.lookup.LookupWriter;
import org.atlasapi.search.model.Search;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.query.Selection;

public class FilmEquivUpdater {
    
    private static final ApplicationConfiguration config = new ApplicationConfiguration(ImmutableSet.of(Publisher.PREVIEW_NETWORKS), null);
    private final SearchResolver searchResolver;
    private final LookupWriter lookupWriter;

    public FilmEquivUpdater(SearchResolver searchResolver, LookupWriter lookupWriter) {
        this.searchResolver = searchResolver;
        this.lookupWriter = lookupWriter;
    }
    
    public void updateEquivalence(Film film) {
        
        if (film.getYear() == null || Strings.isNullOrEmpty(film.getTitle())) {
            return;
        }
        
        List<Identified> possibleEquivalentFilms = searchResolver.search(new Search(film.getTitle()), ImmutableList.of(Publisher.PREVIEW_NETWORKS), config, Selection.ALL);
        
        Iterable<Film> equivalentFilms = Iterables.filter(Iterables.filter(possibleEquivalentFilms, Film.class), new EquivalentFilmPredicate(film));
        
        if (!Iterables.isEmpty(equivalentFilms)) {
            lookupWriter.writeLookup(film, equivalentFilms);
        }
    }
    
    private class EquivalentFilmPredicate implements Predicate<Film> {
        
        private final Film film;

        public EquivalentFilmPredicate(Film film) {
            this.film = Preconditions.checkNotNull(film);
            Preconditions.checkArgument(!Strings.isNullOrEmpty(film.getTitle()));
            Preconditions.checkArgument(film.getYear() != null);
        }

        @Override
        public boolean apply(Film input) {
            return trim(film.getTitle()).equals(trim(input.getTitle())) && film.getYear().equals(input.getYear());
        }
        
        private String trim(String title) {
            return title.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        }
    }
}