package org.atlasapi.remotesite.wikipedia;

import com.google.common.collect.Multimap;
import com.google.common.io.Resources;
import java.io.IOException;
import java.util.List;
import net.sourceforge.jwbf.core.contentRep.Article;
import org.apache.commons.io.IOUtils;
import org.atlasapi.media.entity.CrewMember;
import org.atlasapi.media.entity.Film;
import org.junit.Test;
import static org.junit.Assert.*;
import xtc.parser.ParseException;

public class FilmExtractionTests {
    String articleText;
    Article article;
    
    public FilmExtractionTests() throws IOException {
        articleText = IOUtils.toString(Resources.getResource(getClass(), "example.mediawiki").openStream(), "utf8");
        article = new Article(null, "Hackers (1995 film)");
        article.setText(articleText);
    }

    @Test
    public void testInfoboxScraping() throws IOException, ParseException {
        Multimap<String, String> attrs = FilmInfoboxScraper.getInfoboxAttrs(articleText);
        assertTrue(attrs.get("producer").contains("Michael Peyser"));
        assertTrue(attrs.get("name").contains("Hackers"));
        assertEquals(1, attrs.get("producer").size());
        assertEquals(1, attrs.get("name").size());
    }
    
    @Test
    public void testFilmExtraction() {
        FilmExtractor fex = new FilmExtractor();
        Film flim = fex.extract(article);
        assertEquals("Hackers", flim.getTitle());
        
        List<CrewMember> people = flim.getPeople();
        boolean hasDirector = false;
        for (CrewMember p : people){
            if (p.role()==CrewMember.Role.DIRECTOR) {
                assertEquals("Iain Softley", p.name());
                hasDirector = true;
            }
        }
        assertTrue(hasDirector);
    }
}
