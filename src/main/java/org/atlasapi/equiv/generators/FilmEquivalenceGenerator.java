package org.atlasapi.equiv.generators;

import static com.google.common.collect.Iterables.filter;

import java.util.List;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.equiv.results.scores.DefaultScoredEquivalents;
import org.atlasapi.equiv.results.scores.DefaultScoredEquivalents.ScoredEquivalentsBuilder;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredEquivalents;
import org.atlasapi.media.entity.Film;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.SearchResolver;
import org.atlasapi.search.model.SearchQuery;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.query.Selection;

public class FilmEquivalenceGenerator implements ContentEquivalenceGenerator<Film> {

    private static final ApplicationConfiguration config = new ApplicationConfiguration(ImmutableSet.of(Publisher.PREVIEW_NETWORKS), null);
    private static final float TITLE_WEIGHTING = 1.0f;
    private static final float BROADCAST_WEIGHTING = 0.0f;
    private static final float CATCHUP_WEIGHTING = 0.0f;

    private final SearchResolver searchResolver;

    public FilmEquivalenceGenerator(SearchResolver searchResolver) {
        this.searchResolver = searchResolver;
    }

    @Override
    public ScoredEquivalents<Film> generate(Film film) {
        ScoredEquivalentsBuilder<Film> scores = DefaultScoredEquivalents.<Film> fromSource("Film");

        if (film.getYear() == null || Strings.isNullOrEmpty(film.getTitle())) {
            return scores.build();
        }

        List<Identified> possibleEquivalentFilms = searchResolver.search(new SearchQuery(film.getTitle(), Selection.ALL, ImmutableList.of(Publisher.PREVIEW_NETWORKS), TITLE_WEIGHTING,
                BROADCAST_WEIGHTING, CATCHUP_WEIGHTING), config);

        Iterable<Film> foundFilms = filter(possibleEquivalentFilms, Film.class);

        for (Film equivFilm : foundFilms) {
            if(sameYear(film, equivFilm)) {
                scores.addEquivalent(equivFilm, Score.valueOf(titleMatch(film, equivFilm)));
            } else {
                scores.addEquivalent(equivFilm, Score.valueOf(0.0));
            }
        }

        return scores.build();
    }

    private double titleMatch(Film film, Film equivFilm) {
        return match(removeThe(alphaNumeric(film.getTitle())), removeThe(alphaNumeric(equivFilm.getTitle())));
    }

    public double match(String subjectTitle, String equivalentTitle) {
        double commonPrefix = commonPrefixLength(subjectTitle, equivalentTitle);
        double difference = Math.abs(equivalentTitle.length() - commonPrefix) / equivalentTitle.length();
        return commonPrefix / (subjectTitle.length() / 1.0) - difference;
    }
    
    private String removeThe(String title) {
        if(title.startsWith("the")) {
            return title.substring(3);
        }
        return title;
    }

    private String alphaNumeric(String title) {
        return title.replaceAll("[^\\d\\w]", "").toLowerCase();
    }

    private double commonPrefixLength(String t1, String t2) {
        int i = 0;
        for (; i < Math.min(t1.length(), t2.length()) && t1.charAt(i) == t2.charAt(i); i++) {
        }
        return i;
    }

    private boolean sameYear(Film film, Film equivFilm) {
        return film.getYear().equals(equivFilm.getYear());
    }
}
