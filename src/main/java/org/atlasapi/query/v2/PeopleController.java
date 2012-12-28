package org.atlasapi.query.v2;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.media.entity.Person;
import org.atlasapi.output.AtlasErrorSummary;
import org.atlasapi.output.AtlasModelWriter;
import org.atlasapi.persistence.content.PeopleResolver;
import org.atlasapi.persistence.logging.AdapterLog;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;

@Controller
public class PeopleController extends BaseController<Iterable<Person>> {

    private final PeopleResolver resolver;

    public PeopleController(PeopleResolver resolver, ApplicationConfigurationFetcher configFetcher,
                    AdapterLog log, AtlasModelWriter<Iterable<Person>> outputter) {
        super(configFetcher, log, outputter, SubstitutionTableNumberCodec.lowerCaseOnly());
        this.resolver = resolver;
    }

    @RequestMapping("/3.0/people.*")
    public void content(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            ApplicationConfiguration config = possibleAppConfig(request)
                    .valueOrDefault(ApplicationConfiguration.defaultConfiguration());

            String uri = request.getParameter("uri");
            String id = request.getParameter("id");
            if (!(Strings.isNullOrEmpty(uri) ^ Strings.isNullOrEmpty(id))) {
                throw new IllegalArgumentException("specify exactly one of 'uri' and 'id'");
            }
            Person person;
            if (uri != null) {
                person = resolver.person(uri).valueOrNull();
            } else {
                person = resolver.person(idCodec.decode(id).longValue());
            }
            
            if (!config.isEnabled(person.getPublisher())) {
                throw new IllegalArgumentException("source not enabled");
            }
            
            modelAndViewFor(request, response, ImmutableList.of(person), config);
        } catch (Exception e) {
            errorViewFor(request, response, AtlasErrorSummary.forException(e));
        }
    }
}
