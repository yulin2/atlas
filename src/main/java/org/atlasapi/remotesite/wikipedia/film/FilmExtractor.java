package org.atlasapi.remotesite.wikipedia.film;

import java.io.IOException;
import java.util.List;

import org.atlasapi.media.entity.Alias;
import org.atlasapi.media.entity.CrewMember;
import org.atlasapi.media.entity.CrewMember.Role;
import org.atlasapi.media.entity.Film;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.wikipedia.Article;
import org.atlasapi.remotesite.wikipedia.SwebleHelper.ListItemResult;
import org.atlasapi.remotesite.wikipedia.film.FilmInfoboxScraper.Result;

import xtc.parser.ParseException;

import com.google.common.collect.ImmutableList;

/**
 * This attempts to extract a {@link Film} from its Wikipedia article.
 */
public class FilmExtractor implements ContentExtractor<Article, Film> {

    @Override
    public Film extract(Article article) {
        String source = article.getMediaWikiSource();
        try {
            Result info = FilmInfoboxScraper.getInfoboxAttrs(source);
            
            String url = article.getUrl();
            Film flim = new Film(url, url, Publisher.WIKIPEDIA);
            
            flim.setLastUpdated(article.getLastModified());
            
            ImmutableList<ListItemResult> title = info.name;
            if (title.size() == 1) {
                flim.setTitle(title.get(0).name);
            } else {
                throw new RuntimeException("Film in Wikipedia article \"" + article.getTitle() + "\" has " + (title.isEmpty() ? "no title." : "multiple titles."));
            }
            
            List<CrewMember> people = flim.getPeople();
//            crewify(info.cinematographers, Role., people);  // TODO do we have / do we want a role for cinematographers?
            crewify(info.composers, Role.COMPOSER, people);
            crewify(info.directors, Role.DIRECTOR, people);
            crewify(info.editors, Role.EDITOR, people);
            crewify(info.narrators, Role.NARRATOR, people);
            crewify(info.producers, Role.PRODUCER, people);
            crewify(info.writers, Role.WRITER, people);
            crewify(info.storyWriters, Role.SOURCE_WRITER, people);  // TODO is this the right role?
            crewify(info.screenplayWriters, Role.ADAPTED_BY, people);  // TODO is this the right role?
            crewify(info.starring, Role.ACTOR, people);
            
            if (info.imdbID != null) {
                flim.addAlias(new Alias("imdb:title", info.imdbID));
                flim.addAlias(new Alias("imdb:url", "http://imdb.com/title/tt" + info.imdbID));
            }
            
            return flim;
        } catch (IOException | ParseException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private void crewify(ImmutableList<ListItemResult> from, Role role, List<CrewMember> into) {
        for (ListItemResult person : from) {
            if (person.articleTitle.isPresent()) {  // They have an article! TODO: handle general articles targeted by multiple people's names
                into.add(new CrewMember(Article.urlFromTitle(person.articleTitle.get()), null, Publisher.WIKIPEDIA).withRole(role).withName(person.name));
            } else {
                into.add(new CrewMember().withRole(role).withName(person.name).withPublisher(Publisher.WIKIPEDIA));
            }
        }
    }
}
