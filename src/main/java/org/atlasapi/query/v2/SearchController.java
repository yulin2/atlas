package org.atlasapi.query.v2;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.beans.AtlasErrorSummary;
import org.atlasapi.beans.AtlasModelWriter;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.SearchResolver;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.search.model.Search;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Strings;
import com.metabroadcast.common.query.Selection;

@Controller
public class SearchController extends BaseController {

    private final SearchResolver searcher;

    public SearchController(SearchResolver searcher, ApplicationConfigurationFetcher configFetcher, AdapterLog log, AtlasModelWriter outputter) {
        super(configFetcher, log, outputter);
        this.searcher = searcher;
    }

    @RequestMapping("/3.0/search.*")
    public void search(@RequestParam String q, @RequestParam(required=false) String publisher, HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            if (Strings.isNullOrEmpty(q)) {
                throw new IllegalArgumentException("You must specify a query parameter");
            }
        
            Selection selection = Selection.builder().build(request);
            if (! selection.hasLimit()) {
                throw new IllegalArgumentException("You must specify a limit parameter");
            }
            
            Set<Publisher> publishers = publishers(publisher, appConfig(request));
            List<Identified> content = searcher.search(new Search(q), publishers, selection);
        
            modelAndViewFor(request, response, content);
        } catch (Exception e) {
            errorViewFor(request, response, AtlasErrorSummary.forException(e));
        }
    }
}
