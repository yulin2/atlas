package org.atlasapi.query.v2;

import static com.google.api.client.util.Preconditions.checkNotNull;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.application.v3.ApplicationConfiguration;
import org.atlasapi.media.entity.Event;
import org.atlasapi.media.entity.Topic;
import org.atlasapi.output.AtlasErrorSummary;
import org.atlasapi.output.AtlasModelWriter;
import org.atlasapi.persistence.event.EventResolver;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.topic.TopicQueryResolver;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.http.HttpStatusCode;
import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.query.Selection;
import com.metabroadcast.common.query.Selection.SelectionBuilder;

@Controller
public class EventsController extends BaseController<Iterable<Event>> {

    private static final SelectionBuilder SELECTION_BUILDER = Selection.builder()
            .withMaxLimit(100)
            .withDefaultLimit(10);
    private static final AtlasErrorSummary NOT_FOUND = new AtlasErrorSummary(new NullPointerException())
            .withMessage("No such Event exists")
            .withErrorCode("Event not found")
            .withStatusCode(HttpStatusCode.NOT_FOUND);
    private static final AtlasErrorSummary EVENT_GROUP_NOT_FOUND = new AtlasErrorSummary(new NullPointerException())
            .withMessage("No EventGroup with this ID exists")
            .withErrorCode("Event Group not found")
            .withStatusCode(HttpStatusCode.BAD_REQUEST);
    private static final AtlasErrorSummary FORBIDDEN = new AtlasErrorSummary(new NullPointerException())
            .withMessage("You require an API key to view this data")
            .withErrorCode("Api Key required")
            .withStatusCode(HttpStatusCode.FORBIDDEN);
    
    private final EventResolver eventResolver;
    private final TopicQueryResolver topicResolver;

    public EventsController(ApplicationConfigurationFetcher configFetcher, AdapterLog log, 
            AtlasModelWriter<? super Iterable<Event>> outputter, NumberToShortStringCodec idCodec, 
            EventResolver eventResolver, TopicQueryResolver topicResolver) {
        super(configFetcher, log, outputter, idCodec);
        this.topicResolver = topicResolver;
        this.eventResolver = checkNotNull(eventResolver);
    }

    @SuppressWarnings("deprecation") // because of Maybe returned from TopicQueryResolver
    @RequestMapping(value={"/3.0/events.*", "/events.*"})
    public void allEvents(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value = "event_group", required = false) String eventGroupId) throws IOException {
        try {
            final ApplicationConfiguration appConfig = appConfig(request);
            
            Selection selection = SELECTION_BUILDER.build(request);
            Iterable<Event> events;
            
            if (eventGroupId != null) {
                Maybe<Topic> eventGroup = topicResolver.topicForId(Long.valueOf(eventGroupId));
                if (eventGroup.isNothing()) {
                    errorViewFor(request, response, EVENT_GROUP_NOT_FOUND);
                    return; 
                } else {
                    events = eventResolver.fetchByEventGroup(eventGroup.requireValue());
                }
            } else {
                events = eventResolver.fetchAll();
            }
            
            events = selection.apply(Iterables.filter(events, isEnabled(appConfig)));

            modelAndViewFor(request, response, events, appConfig);
        } catch (Exception e) {
            errorViewFor(request, response, AtlasErrorSummary.forException(e));
        }
    }

    @RequestMapping(value={"/3.0/events/{id}.*", "/events/{id}.*"})
    public void singleEvent(HttpServletRequest request, HttpServletResponse response,
            @PathVariable("id") String id) throws IOException {
        try {
            final ApplicationConfiguration appConfig = appConfig(request);
            
            Optional<Event> event = eventResolver.fetch(idCodec.decode(id).longValue());
            if (!event.isPresent()) {
                errorViewFor(request, response, NOT_FOUND);
                return;
            }
            if (!appConfig.isEnabled(event.get().publisher())) {
                errorViewFor(request, response, FORBIDDEN);
                return;
            }

            modelAndViewFor(request, response, event.asSet(), appConfig);
        } catch (Exception e) {
            errorViewFor(request, response, AtlasErrorSummary.forException(e));
        }
    }
    
    private static Predicate<Event> isEnabled(final ApplicationConfiguration appConfig) {
        return new Predicate<Event>() {
            @Override
            public boolean apply(Event input) {
                return appConfig.isEnabled(input.publisher());
            }
        };
    }
}
