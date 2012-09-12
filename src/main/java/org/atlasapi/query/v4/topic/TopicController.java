package org.atlasapi.query.v4.topic;

import com.google.common.base.Strings;
import com.metabroadcast.common.query.Selection;
import com.metabroadcast.common.webapp.query.DateTimeInQueryParser;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.media.entity.Topic;
import org.atlasapi.output.AtlasErrorSummary;
import org.atlasapi.output.AtlasModelWriter;
import org.atlasapi.persistence.topic.TopicQueryResolver;
import org.atlasapi.persistence.topic.TopicSearcher;
import org.joda.time.Interval;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TopicController {

    private final DateTimeInQueryParser dateTimeInQueryParser = new DateTimeInQueryParser();
    private final TopicQueryResolver topicResolver;
    private final TopicSearcher topicSearcher;
    private final AtlasModelWriter<Iterable<Topic>> responseWriter;
    private final ApplicationConfigurationFetcher configurationFetcher;

    public TopicController(TopicQueryResolver topicResolver, TopicSearcher topicSearcher, AtlasModelWriter<Iterable<Topic>> responseWriter, ApplicationConfigurationFetcher configurationFetcher) {
        this.topicResolver = topicResolver;
        this.topicSearcher = topicSearcher;
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
            List<Topic> topics = topicSearcher.popularTopics(interval, topicResolver, selection);
            responseWriter.writeTo(request, response, topics, Collections.EMPTY_SET, configuration);
        } catch (Exception ex) {
            responseWriter.writeError(request, response, AtlasErrorSummary.forException(ex));
        }
    }
}
