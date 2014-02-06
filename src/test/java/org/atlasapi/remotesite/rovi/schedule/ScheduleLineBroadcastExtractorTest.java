package org.atlasapi.remotesite.rovi.schedule;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.media.entity.Broadcast;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.metabroadcast.common.base.Maybe;

@RunWith(MockitoJUnitRunner.class)
public class ScheduleLineBroadcastExtractorTest {

    private static final String SCHEDULE_ID = "456";
    private final ChannelResolver channelResolver = mock(ChannelResolver.class);
    private final ScheduleLineBroadcastExtractor extractor = new ScheduleLineBroadcastExtractor(
            channelResolver);
    
    private final Channel channel = Channel.builder()
                                           .withUri("http://rovicorp.com/channels/123")
                                           .build();   
    
    @Test
    public void testExtractChannelExists() {
        when(channelResolver.forAlias("http://rovicorp.com/channels/123")).thenReturn(Maybe.just(channel));
        
        Broadcast broadcast = extractor.extract(scheduleLine("123")).requireValue();
        assertThat(broadcast.getCanonicalUri(), is("http://rovicorp.com/broadcasts/" + SCHEDULE_ID));
        assertThat(broadcast.getBroadcastOn(), is(channel.getCanonicalUri()));
        assertThat(broadcast.getTransmissionTime().toDateTime(DateTimeZone.UTC), 
                is(new DateTime(2014, DateTimeConstants.JANUARY, 1, 15, 0).withZone(DateTimeZone.UTC)));
        assertThat(broadcast.getTransmissionEndTime().toDateTime(DateTimeZone.UTC), 
                is(new DateTime(2014, DateTimeConstants.JANUARY, 1, 15, 10).withZone(DateTimeZone.UTC)));
    }
    
    @Test
    public void testExtractChannelDoesntExist() {
        when(channelResolver.forAlias("http://rovicorp.com/channels/123")).thenReturn(Maybe.<Channel>nothing());
        assertThat(extractor.extract(scheduleLine("123")), is(Maybe.<Broadcast>nothing()));
    }
    
    private ScheduleLine scheduleLine(String sourceId) {
        return new ScheduleLine(sourceId, new LocalDate(2014, DateTimeConstants.JANUARY, 1), 
                new LocalTime(15, 0), false, 600, "12345", null, null, null, null, null, null, 
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, 
                SCHEDULE_ID);
    }
}
