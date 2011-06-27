package org.atlasapi.equiv.generators;

import java.util.List;
import java.util.Set;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.equiv.results.DefaultScoredEquivalents;
import org.atlasapi.equiv.results.Score;
import org.atlasapi.equiv.results.DefaultScoredEquivalents.ScoredEquivalentsBuilder;
import org.atlasapi.equiv.results.ScoredEquivalents;
import org.atlasapi.media.entity.Film;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.SearchResolver;
import org.atlasapi.search.model.Search;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.query.Selection;

public class FilmEquivalenceGenerator implements ContentEquivalenceGenerator<Film> {
    
    private static final ApplicationConfiguration config = new ApplicationConfiguration(ImmutableSet.of(Publisher.PREVIEW_NETWORKS), null);
    
    private final SearchResolver searchResolver;

    public FilmEquivalenceGenerator(SearchResolver searchResolver) {
        this.searchResolver = searchResolver;
    }
    
    @Override
    public ScoredEquivalents<Film> generateEquivalences(Film film, Set<Film> suggestions) {

        ScoredEquivalentsBuilder<Film> scores = DefaultScoredEquivalents.<Film>fromSource("Film");
        
        if (film.getYear() == null || Strings.isNullOrEmpty(film.getTitle())) {
            return scores.build();
        }
        
        List<Identified> possibleEquivalentFilms = searchResolver.search(new Search(film.getTitle()), ImmutableList.of(Publisher.PREVIEW_NETWORKS), config, Selection.ALL);
        
        Iterable<Film> equivalentFilms = Iterables.filter(Iterables.filter(possibleEquivalentFilms, Film.class), new EquivalentFilmPredicate(film));
        
        for (Film equivFilm : equivalentFilms) {
            scores.addEquivalent(equivFilm, Score.valueOf(1.0));
        }
        
        return scores.build();
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
