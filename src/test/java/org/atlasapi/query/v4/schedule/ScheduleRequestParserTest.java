package org.atlasapi.query.v4.schedule;

import static org.atlasapi.media.entity.MediaType.VIDEO;
import static org.atlasapi.media.entity.Publisher.BBC;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.Set;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.output.Annotation;
import org.atlasapi.media.channel.ChannelResolver;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;
import com.metabroadcast.common.servlet.StubHttpServletRequest;
import com.metabroadcast.common.time.DateTimeZones;

@RunWith(MockitoJUnitRunner.class)
public class ScheduleRequestParserTest {

    private final ApplicationConfigurationFetcher applicationFetcher = mock(ApplicationConfigurationFetcher.class);
    private final ChannelResolver channelResolver = mock(ChannelResolver.class);
    private final ScheduleRequestParser builder = new ScheduleRequestParser(channelResolver, applicationFetcher, Duration.standardDays(1));

    private final NumberToShortStringCodec codec = new SubstitutionTableNumberCodec();
    
    @Test
    public void testCreatesQueryFromValidQueryString() {
        
        Channel channel = new Channel(BBC, "Channel", "cbbc", VIDEO, "uri");
        channel.setId(1234L);
        
        DateTime from = new DateTime(DateTimeZones.UTC);
        DateTime to = from.plusHours(1);
        Publisher publisher = BBC;
        Set<Annotation> annotations = Annotation.defaultAnnotations();
        ApplicationConfiguration appConfig = ApplicationConfiguration.DEFAULT_CONFIGURATION;
        String appKey = "key";
        
        StubHttpServletRequest request = scheduleRequest(channel, from, to, publisher, appKey, annotations, ".json");
        
        when(channelResolver.fromId(channel.getId())).thenReturn(Maybe.just(channel));
        when(applicationFetcher.configurationFor(request)).thenReturn(Maybe.just(appConfig));
        
        ScheduleQuery query = builder.queryFrom(request);
        
        assertThat(query.getChannel(), is(channel));
        assertThat(query.getInterval(), is(new Interval(from, to)));
        assertThat(query.getPublisher(), is(publisher));
        assertThat(query.getAnnotations(), is(annotations));
        assertThat(query.getApplicationConfiguration(), is(appConfig));
    }
    
    @Test
    public void testCreatesQueryFromValidQueryStringWithNoExtension() {
        
        Channel channel = new Channel(BBC, "Channel", "cbbc", VIDEO, "uri");
        channel.setId(1234L);
        
        DateTime from = new DateTime(DateTimeZones.UTC);
        DateTime to = from.plusHours(1);
        Publisher publisher = BBC;
        Set<Annotation> annotations = Annotation.defaultAnnotations();
        ApplicationConfiguration appConfig = ApplicationConfiguration.DEFAULT_CONFIGURATION;
        String appKey = "key";
        
        StubHttpServletRequest request = scheduleRequest(channel, from, to, publisher, appKey, annotations, "");
        
        when(channelResolver.fromId(channel.getId())).thenReturn(Maybe.just(channel));
        when(applicationFetcher.configurationFor(request)).thenReturn(Maybe.just(appConfig));
        
        ScheduleQuery query = builder.queryFrom(request);
        
        assertThat(query.getChannel(), is(channel));
        assertThat(query.getInterval(), is(new Interval(from, to)));
        assertThat(query.getPublisher(), is(publisher));
        assertThat(query.getAnnotations(), is(annotations));
        assertThat(query.getApplicationConfiguration(), is(appConfig));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testDoesntAcceptQueryDurationGreaterThanMax() {
        
        Channel channel = new Channel(BBC, "Channel", "cbbc", VIDEO, "uri");
        channel.setId(1234L);
        Publisher publisher = BBC;
        Set<Annotation> annotations = Annotation.defaultAnnotations();
        ApplicationConfiguration appConfig = ApplicationConfiguration.DEFAULT_CONFIGURATION;
        String appKey = "key";
        
        DateTime from = new DateTime(DateTimeZones.UTC);
        DateTime to = from.plusHours(25);

        StubHttpServletRequest request = scheduleRequest(channel, from, to, publisher, appKey, annotations, ".json");
        
        when(channelResolver.fromId(channel.getId())).thenReturn(Maybe.just(channel));
        when(applicationFetcher.configurationFor(request)).thenReturn(Maybe.just(appConfig));

        builder.queryFrom(request);

    }

    private StubHttpServletRequest scheduleRequest(Channel channel, DateTime from, DateTime to, Publisher publisher, String appKey, Set<Annotation> annotations, String extension) {
        String uri = String.format(
            "http://localhost/4.0/schedules/%s%s",
            codec.encode(BigInteger.valueOf(channel.getId())),
            extension
        );
        return new StubHttpServletRequest().withRequestUri(uri)
                .withParam("from", from.toString())
                .withParam("to", to.toString())
                .withParam("source", publisher.key())
                .withParam("annotations", Joiner.on(',').join(Iterables.transform(annotations, Annotation.toKeyFunction())))
                .withParam("apiKey", appKey);
    }

}
