package org.atlasapi.query.v4.schedule;

import static com.google.common.base.Preconditions.checkArgument;
import static com.metabroadcast.common.webapp.query.DateTimeInQueryParser.queryDateTimeParser;
import static org.atlasapi.output.Annotation.defaultAnnotations;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.application.SourceStatus;
import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.output.Annotation;
import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.query.v2.QueryParameterAnnotationsExtractor;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import com.google.common.base.Strings;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;
import com.metabroadcast.common.time.Clock;
import com.metabroadcast.common.webapp.query.DateTimeInQueryParser;

class ScheduleRequestParser {
    
    private static final Pattern CHANNEL_ID_PATTERN = Pattern.compile(
        ".*schedules/([^.]+)(.[\\w\\d.]+)?$"
    );
    
    private final ChannelResolver channelResolver;
    private final ApplicationConfigurationFetcher applicationStore;

    private final RequestParameterValidator validator = RequestParameterValidator.builder()
        .withRequiredParameters("from","to","source")
        .withOptionalParameters("annotations","apiKey", "callback")
        .build();
    
    private final NumberToShortStringCodec idCodec;
    private final DateTimeInQueryParser dateTimeParser;
    private final QueryParameterAnnotationsExtractor annotationExtractor;
    private final Duration maxQueryDuration;
    private final Clock clock;


    public ScheduleRequestParser(ChannelResolver channelResolver, ApplicationConfigurationFetcher appFetcher, Duration maxQueryDuration, Clock clock) {
        this.channelResolver = channelResolver;
        this.applicationStore = appFetcher;
        this.maxQueryDuration = maxQueryDuration;
        this.idCodec = SubstitutionTableNumberCodec.lowerCaseOnly();
        this.dateTimeParser = queryDateTimeParser()
                .parsesIsoDateTimes()
                .parsesIsoTimes()
                .parsesIsoDates()
                .parsesOffsets()
                .build();
        this.clock = clock;
        this.annotationExtractor = new QueryParameterAnnotationsExtractor();
    }

    public ScheduleQuery queryFrom(HttpServletRequest request) throws NotFoundException {
        // Attempt to extract channel first so we can 404 if missing before
        // 400ing from bad params.
        Channel channel = extractChannel(request);

        validator.validateParameters(request);
        
        Publisher publisher = extractPublisher(request);
        Interval queryInterval = extractInterval(request);
        
        ApplicationConfiguration appConfig = getConfiguration(request);
        appConfig = appConfigForValidPublisher(publisher, appConfig, queryInterval);
        checkArgument(appConfig != null, "Source %s not enabled", publisher);
        
        Set<Annotation> annotations = annotationExtractor.extractFromKeys(request).or(defaultAnnotations());

        return new ScheduleQuery(publisher, channel, queryInterval, appConfig, annotations);
    }

    private ApplicationConfiguration appConfigForValidPublisher(Publisher publisher,
                                                                ApplicationConfiguration appConfig,
                                                                Interval interval) {
        if (appConfig.isEnabled(publisher)) {
            return appConfig;
        }
        if (Publisher.PA.equals(publisher) && overlapsOpenInterval(interval)) {
            appConfig = appConfig.withSource(Publisher.PA, SourceStatus.AVAILABLE_ENABLED);
            return appConfig;
        }
        return null;
    }

    private boolean overlapsOpenInterval(Interval interval) {
        DateMidnight now = clock.now().toDateMidnight();
        Interval openInterval = new Interval(now.minusDays(7), now.plusDays(8));
        return openInterval.contains(interval);
    }

    private Channel extractChannel(HttpServletRequest request) throws NotFoundException {
        String channelId = getChannelId(request.getRequestURI());
        Maybe<Channel> channel = resolveChannel(channelId);
        
        if (channel.hasValue()) {
            return channel.requireValue();
        }
        throw new NotFoundException(String.format("Unknown channel '%s'", channelId));
    }

    private Maybe<Channel> resolveChannel(String channelId) {
        long cid;
        try {
            cid = idCodec.decode(channelId).longValue();
        } catch (IllegalArgumentException e) {
            return Maybe.nothing();
        }
        return channelResolver.fromId(cid);
    }

    private String getChannelId(String requestUri) {
        Matcher matcher = CHANNEL_ID_PATTERN.matcher(requestUri);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("Channel identifier missing");
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
        String apiKeyParam = request.getParameter("apiKey");
        // request doesn't specify apiKey so use default configuration.
        if (apiKeyParam == null) {
            return ApplicationConfiguration.DEFAULT_CONFIGURATION;
        }
        // the request has an apiKey param but no config is found.
        throw new IllegalArgumentException("Unknown API key " + apiKeyParam);
    }

    private String getParameter(HttpServletRequest request, String param) {
        String paramValue = request.getParameter(param);
        checkArgument(!Strings.isNullOrEmpty(paramValue), "Missing required parameter %s", param);
        return paramValue;
    }

}
