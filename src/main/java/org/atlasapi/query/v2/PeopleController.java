package org.atlasapi.query.v2;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.beans.AtlasErrorSummary;
import org.atlasapi.beans.AtlasModelWriter;
import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Person;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
import org.atlasapi.persistence.logging.AdapterLog;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.query.Selection;

@Controller
public class PeopleController extends BaseController {

    public PeopleController(KnownTypeQueryExecutor executor, ApplicationConfigurationFetcher configFetcher,
                    AdapterLog log, AtlasModelWriter outputter) {
        super(executor, configFetcher, log, outputter);
    }

    @RequestMapping("/3.0/people.*")
    public void content(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            ContentQuery filter = builder.build(request);

            if (!Selection.ALL.equals(filter.getSelection())) {
                throw new IllegalArgumentException("Cannot specifiy a limit or offset here");
            }
            String commaSeperatedUris = request.getParameter("uri");
            if (commaSeperatedUris == null) {
                throw new IllegalArgumentException("No uris specified");
            }
            List<String> uris = ImmutableList.copyOf(URI_SPLITTER.split(commaSeperatedUris));
            if (Iterables.isEmpty(uris)) {
                throw new IllegalArgumentException("No uris specified");
            }
            
            List<Person> people = ImmutableList.of();
            List<Identified> content = executor.executeUriQuery(uris, filter);
            if (! content.isEmpty()) {
                people = ImmutableList.copyOf(Iterables.filter(content, Person.class));
                if (people.isEmpty()) {
                    throw new IllegalArgumentException("The uris requested were not for people");
                }
            }
            
            modelAndViewFor(request, response, people);
        } catch (Exception e) {
            errorViewFor(request, response, AtlasErrorSummary.forException(e));
        }
    }
}
