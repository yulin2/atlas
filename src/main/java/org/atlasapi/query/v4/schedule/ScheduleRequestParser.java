package org.atlasapi.query.v4.schedule;

import static com.metabroadcast.common.webapp.query.DateTimeInQueryParser.queryDateTimeParser;
import static org.atlasapi.output.Annotation.defaultAnnotations;
import static org.elasticsearch.common.Preconditions.checkArgument;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.output.Annotation;
import org.atlasapi.persistence.media.channel.ChannelResolver;
import org.atlasapi.query.v2.QueryParameterAnnotationsExtractor;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import com.google.common.base.Strings;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;
import com.metabroadcast.common.webapp.query.DateTimeInQueryParser;

class ScheduleRequestParser {
    
    private static final Pattern CHANNEL_ID_PATTERN = Pattern.compile(
        ".*/([^.]+)(.[\\w\\d.]+)?$"
    );
    
    private final ChannelResolver channelResolver;
    private final ApplicationConfigurationFetcher applicationStore;
    
    private final NumberToShortStringCodec idCodec;
    private final DateTimeInQueryParser dateTimeParser;
    private final QueryParameterAnnotationsExtractor annotationExtractor;
    private Duration maxQueryDuration;

    public ScheduleRequestParser(ChannelResolver channelResolver, ApplicationConfigurationFetcher appFetcher, Duration maxQueryDuration) {
        this.channelResolver = channelResolver;
        this.applicationStore = appFetcher;
        this.maxQueryDuration = maxQueryDuration;
        this.idCodec = new SubstitutionTableNumberCodec();
        this.dateTimeParser = queryDateTimeParser()
                .parsesIsoDateTimes()
                .parsesIsoTimes()
                .parsesIsoDates()
                .parsesOffsets()
                .build();
        this.annotationExtractor = new QueryParameterAnnotationsExtractor();
    }

    public ScheduleQuery queryFrom(HttpServletRequest request) {

        Publisher publisher = extractPublisher(request);
        Channel channel = extractChannel(request);
        Interval queryInterval = extractInterval(request);
        
        ApplicationConfiguration appConfig = getConfiguration(request);
        checkArgument(appConfig.getEnabledSources().contains(publisher), "Source %s not enabled", publisher);
        
        Set<Annotation> annotations = annotationExtractor.extract(request).or(defaultAnnotations());

        return new ScheduleQuery(publisher, channel, queryInterval, appConfig, annotations);
    }

    private Channel extractChannel(HttpServletRequest request) {
        String channelId = getChannelId(request.getRequestURI());
        long cid = idCodec.decode(channelId).longValue();
        Maybe<Channel> channel = channelResolver.fromId(cid);
        
        checkArgument(channel.hasValue(), "Unknown channel %s", channelId);
        return channel.requireValue();
    }

    private Interval extractInterval(HttpServletRequest request) {
        DateTime from = dateTimeParser.parse(getParameter(request, "from"));
        DateTime to = dateTimeParser.parse(getParameter(request, "to"));
        
        Interval queryInterval = new Interval(from, to);
        checkArgument(!queryInterval.toDuration().isLongerThan(maxQueryDuration), "Query interval cannot be longer than %s", maxQueryDuration);
        return queryInterval;
    }

    private Publisher extractPublisher(HttpServletRequest request) {
        String pubKey = getParameter(request, "source");
        Maybe<Publisher> publisher = Publisher.fromKey(pubKey);
        checkArgument(publisher.hasValue(), "Unknown source %s", pubKey);
        return publisher.requireValue();
    }

    private ApplicationConfiguration getConfiguration(HttpServletRequest request) {
        Maybe<ApplicationConfiguration> config = applicationStore.configurationFor(request);
        if (config.hasValue()) {
            return config.requireValue();
        }
        // request doesn't specify apiKey so use default configuration.
        if (request.getParameter("apiKey") == null) {
            return ApplicationConfiguration.DEFAULT_CONFIGURATION;
        }
        // the request has an apiKey param but no config is found.
        throw new IllegalArgumentException("Uknown application " + request);
    }

    private String getParameter(HttpServletRequest request, String param) {
        String paramValue = request.getParameter(param);
        checkArgument(!Strings.isNullOrEmpty(paramValue), "Missing required parameter %s", param);
        return paramValue;
    }

    private String getChannelId(String requestUri) {
        Matcher matcher = CHANNEL_ID_PATTERN.matcher(requestUri);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return "";
    }

}
