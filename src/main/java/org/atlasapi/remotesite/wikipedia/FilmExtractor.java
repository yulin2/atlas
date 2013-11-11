package org.atlasapi.remotesite.wikipedia;

import com.google.common.collect.ListMultimap;
import java.io.IOException;
import java.util.List;
import net.sourceforge.jwbf.core.contentRep.Article;
import org.atlasapi.media.entity.CrewMember;
import org.atlasapi.media.entity.Film;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.remotesite.ContentExtractor;
import xtc.parser.ParseException;

/**
 * This attempts to extract a {@link Film} from its Wikipedia article.
 */
public class FilmExtractor implements ContentExtractor<Article, Film> {

    @Override
    public Film extract(Article article) {
        String source = article.getText();
        try {
            ListMultimap<String, String> infoboxAttrs = FilmInfoboxScraper.getInfoboxAttrs(source);
            
            String url = "http://en.wikipedia.org/wiki/" + article.getTitle();  // TODO probably not...
            Film flim = new Film(url, url, Publisher.WIKIPEDIA);
            
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
