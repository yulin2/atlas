package org.atlasapi.query.v2;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.beans.AtlasErrorSummary;
import org.atlasapi.beans.AtlasModelType;
import org.atlasapi.beans.AtlasModelWriter;
import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.media.entity.Person;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.query.content.people.ContentResolvingPeopleResolver;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.query.Selection;

@Controller
public class PeopleController extends BaseController {

    private final ContentResolvingPeopleResolver resolver;

    public PeopleController(ContentResolvingPeopleResolver resolver, ApplicationConfigurationFetcher configFetcher,
                    AdapterLog log, AtlasModelWriter outputter) {
        super(configFetcher, log, outputter);
        this.resolver = resolver;
    }

    @RequestMapping("/3.0/people.*")
    public void content(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            ContentQuery filter = builder.build(request);

            if (!Selection.ALL.equals(filter.getSelection())) {
                throw new IllegalArgumentException("Cannot specifiy a limit or offset here");
            }
            String uri = request.getParameter("uri");
            if (uri == null) {
                throw new IllegalArgumentException("No uri specified");
            }
            
            Person person = resolver.person(uri, filter);
            
            modelAndViewFor(request, response, ImmutableList.of(person), AtlasModelType.PEOPLE);
        } catch (Exception e) {
            errorViewFor(request, response, AtlasErrorSummary.forException(e));
        }
    }
}
