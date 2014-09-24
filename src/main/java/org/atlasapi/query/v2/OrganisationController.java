package org.atlasapi.query.v2;

import static com.google.api.client.util.Preconditions.checkNotNull;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.application.v3.ApplicationConfiguration;
import org.atlasapi.media.entity.Event;
import org.atlasapi.media.entity.Organisation;
import org.atlasapi.media.entity.Topic;
import org.atlasapi.output.AtlasErrorSummary;
import org.atlasapi.output.AtlasModelWriter;
import org.atlasapi.persistence.content.organisation.OrganisationResolver;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.topic.TopicQueryResolver;
import org.joda.time.DateTime;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.api.client.util.Preconditions;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.http.HttpStatusCode;
import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.query.Selection;
import com.metabroadcast.common.query.Selection.SelectionBuilder;


public class OrganisationController extends BaseController<Iterable<Organisation>> {

    private static final SelectionBuilder SELECTION_BUILDER = Selection.builder()
            .withMaxLimit(100)
            .withDefaultLimit(10);
    private static final AtlasErrorSummary NOT_FOUND = new AtlasErrorSummary(new NullPointerException())
        .withMessage("No such Organisation exists")
        .withErrorCode("Organisation not found")
        .withStatusCode(HttpStatusCode.NOT_FOUND);
    private static final AtlasErrorSummary EVENT_GROUP_NOT_FOUND = new AtlasErrorSummary(new NullPointerException())
        .withMessage("No EventGroup with this ID exists")
        .withErrorCode("Event Group not found")
        .withStatusCode(HttpStatusCode.BAD_REQUEST);
    private static final AtlasErrorSummary FORBIDDEN = new AtlasErrorSummary(new NullPointerException())
        .withMessage("You require an API key to view this data")
        .withErrorCode("Api Key required")
        .withStatusCode(HttpStatusCode.FORBIDDEN);
    
    private final TopicQueryResolver topicResolver;
    private final OrganisationResolver organisationResolver;
    
    public OrganisationController(ApplicationConfigurationFetcher configFetcher, AdapterLog log, 
            AtlasModelWriter<? super Iterable<Organisation>> outputter, NumberToShortStringCodec idCodec, 
                             TopicQueryResolver topicResolver, OrganisationResolver organisationResolver) {
        super(configFetcher, log, outputter, idCodec);
        this.topicResolver = checkNotNull(topicResolver);
        this.organisationResolver = checkNotNull(organisationResolver);
    }
    @RequestMapping(value={"/3.0/organisations.*", "/organisations.*"})
    public void allOrganisations(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value = "event_group", required = false) String eventGroupId) throws IOException {
        try {
            final ApplicationConfiguration appConfig = appConfig(request);
            
            Selection selection = SELECTION_BUILDER.build(request);
            Iterable<Organisation> organisations;
            
            Optional<Topic> eventGroup = Optional.absent();
            
            if (eventGroupId != null) {
                Maybe<Topic> resolved = topicResolver.topicForId(idCodec.decode(eventGroupId).longValue());
                if (resolved.isNothing()) {
                    errorViewFor(request, response, EVENT_GROUP_NOT_FOUND);
                    return; 
                }
                eventGroup = Optional.of(resolved.requireValue());
            }
            
            organisations = selection.apply(Iterables.filter(organisationResolver.fetch(eventGroup), isEnabled(appConfig)));

            modelAndViewFor(request, response, organisations, appConfig);
        } catch (Exception e) {
            errorViewFor(request, response, AtlasErrorSummary.forException(e));
        }
    }

    @RequestMapping(value={"/3.0/organisations/{id}.*", "/organisations/{id}.*"})
    public void singleOrganisation(HttpServletRequest request, HttpServletResponse response,
            @PathVariable("id") String id) throws IOException {
        try {
            final ApplicationConfiguration appConfig = appConfig(request);
            
            Optional<Organisation> organisation = organisationResolver.organisation(idCodec.decode(id).longValue());
            if (!organisation.isPresent()) {
                errorViewFor(request, response, NOT_FOUND);
                return;
            }
            if (!appConfig.isEnabled(organisation.get().getPublisher())) {
                errorViewFor(request, response, FORBIDDEN);
                return;
            }

            modelAndViewFor(request, response, organisation.asSet(), appConfig);
        } catch (Exception e) {
            errorViewFor(request, response, AtlasErrorSummary.forException(e));
        }
    }

    private static Predicate<Organisation> isEnabled(final ApplicationConfiguration appConfig) {
        return new Predicate<Organisation>() {
            @Override
            public boolean apply(Organisation input) {
                return appConfig.isEnabled(input.getPublisher());
            }
        };
    }
}
