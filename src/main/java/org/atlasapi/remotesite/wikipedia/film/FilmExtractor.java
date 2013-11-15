package org.atlasapi.remotesite.wikipedia.film;

import com.google.common.collect.ListMultimap;
import java.io.IOException;
import java.util.List;
import org.atlasapi.media.entity.CrewMember;
import org.atlasapi.media.entity.Film;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.wikipedia.Article;
import xtc.parser.ParseException;

/**
 * This attempts to extract a {@link Film} from its Wikipedia article.
 */
public class FilmExtractor implements ContentExtractor<Article, Film> {

    @Override
    public Film extract(Article article) {
        String source = article.getMediaWikiSource();
        try {
            ListMultimap<String, String> infoboxAttrs = FilmInfoboxScraper.getInfoboxAttrs(source);
            
            String url = article.getUrl();
            Film flim = new Film(url, url, Publisher.WIKIPEDIA);
            
            flim.setLastUpdated(article.getLastModified());
            
            List<String> title = infoboxAttrs.get("name");
            if (title.size() == 1) {
                flim.setTitle(title.get(0));
            } else {
                throw new RuntimeException("Film in Wikipedia article \"" + article.getTitle() + "\" has " + (title.isEmpty() ? "no title." : "multiple titles."));
            }
            
            List<CrewMember> people = flim.getPeople();
            for(String director : infoboxAttrs.get("director")) {
                people.add(new CrewMember().withRole(CrewMember.Role.DIRECTOR).withName(director).withPublisher(Publisher.WIKIPEDIA));
            }
            
            return flim;
        } catch (IOException ex) {
            throw new RuntimeException(ex);  // TODO probably not...
        } catch (ParseException ex) {
            throw new RuntimeException(ex);  // TODO probably not...
        }
    }
    
}
