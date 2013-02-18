package org.atlasapi.query.v4.topic;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.topic.PopularTopicIndex;
import org.atlasapi.media.topic.Topic;
import org.atlasapi.media.topic.TopicResolver;
import org.atlasapi.output.Annotation;
import org.atlasapi.output.AtlasModelWriter;
import org.atlasapi.output.ErrorSummary;
import org.joda.time.Interval;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.metabroadcast.common.query.Selection;
import com.metabroadcast.common.webapp.query.DateTimeInQueryParser;

@Controller
public class PopularTopicController {

    private final DateTimeInQueryParser dateTimeInQueryParser = new DateTimeInQueryParser();
    private final TopicResolver resolver;
    private final PopularTopicIndex index;
    private final AtlasModelWriter<Iterable<Topic>> responseWriter;
    private final ApplicationConfigurationFetcher configurationFetcher;

    public PopularTopicController(TopicResolver resolver, PopularTopicIndex index, AtlasModelWriter<Iterable<Topic>> responseWriter, ApplicationConfigurationFetcher configurationFetcher) {
        this.resolver = resolver;
        this.index = index;
        this.responseWriter = responseWriter;
        this.configurationFetcher = configurationFetcher;
    }

    @RequestMapping({"/4.0/topics/popular.*", "/4.0/topics/popular"})
    public void popularTopics(@RequestParam(required = true) String from, @RequestParam(required = true) String to, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (Strings.isNullOrEmpty(from) || Strings.isNullOrEmpty(to)) {
            throw new IllegalArgumentException("Request parameters 'from' and 'to' are required!");
        }
        Selection selection = Selection.builder().withDefaultLimit(Integer.MAX_VALUE).withMaxLimit(Integer.MAX_VALUE).build(request);
        try {
            ApplicationConfiguration configuration = configurationFetcher.configurationFor(request).valueOrDefault(ApplicationConfiguration.DEFAULT_CONFIGURATION);
            Interval interval = new Interval(dateTimeInQueryParser.parse(from), dateTimeInQueryParser.parse(to));
            ListenableFuture<FluentIterable<Id>> topicIds = index.popularTopics(interval, selection);
            responseWriter.writeTo(request, response, resolve(topicIds), ImmutableSet.<Annotation>of(), configuration);
        } catch (Exception ex) {
            responseWriter.writeError(request, response, ErrorSummary.forException(ex));
        }
    }

    private Iterable<Topic> resolve(ListenableFuture<FluentIterable<Id>> topicIds) throws Exception {
        return Futures.get(Futures.transform(topicIds,
            new Function<FluentIterable<Id>, Iterable<Topic>>() {
                @Override
                public FluentIterable<Topic> apply(FluentIterable<Id> input) {
                    return resolver.resolveIds(input).getResources();
                }
            }), 60, TimeUnit.SECONDS, Exception.class);
    }
}
