package org.atlasapi.equiv.tasks;

import java.util.List;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.media.entity.Film;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.SearchResolver;
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
    private final ContentWriter contentWriter;

    public FilmEquivUpdater(SearchResolver searchResolver, ContentWriter contentWriter) {
        this.searchResolver = searchResolver;
        this.contentWriter = contentWriter;
    }
    
    public void updateEquivalence(Film film) {
        
        if (film.getYear() == null || Strings.isNullOrEmpty(film.getTitle())) {
            return;
        }
        
        List<Identified> possibleEquivalentFilms = searchResolver.search(new Search(film.getTitle()), ImmutableList.of(Publisher.PREVIEW_NETWORKS), config, Selection.ALL);
        
        Iterable<Film> equivalentFilms = Iterables.filter(Iterables.filter(possibleEquivalentFilms, Film.class), new EquivalentFilmPredicate(film));
        
        if (!Iterables.isEmpty(equivalentFilms)) {
            for (Film equivalentFilm : equivalentFilms) {
                film.addEquivalentTo(equivalentFilm);
                equivalentFilm.addEquivalentTo(film);
                contentWriter.createOrUpdate(equivalentFilm);
            }
            contentWriter.createOrUpdate(film);
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
