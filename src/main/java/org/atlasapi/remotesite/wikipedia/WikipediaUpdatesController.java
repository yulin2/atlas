package org.atlasapi.remotesite.wikipedia;

import com.google.common.collect.ImmutableList;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WikipediaUpdatesController {
    private WikipediaModule module;
    
    public WikipediaUpdatesController(WikipediaModule module) {
        this.module = module;
    }
    
    @RequestMapping(value="/system/update/wikipedia/film", method=RequestMethod.POST)
    public void updateFilm(HttpServletResponse response, @RequestParam("name") final String articleName) {
        module.filmsUpdaterForTitles(new FilmArticleTitleSource() {
            @Override
            public Iterable<String> getAllFilmArticleTitles() {
                return ImmutableList.of(articleName);
            }
        }).runTask();
    }

}
