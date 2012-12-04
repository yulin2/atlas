package org.atlasapi.query.v4.schedule;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.entity.ChannelSchedule;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.output.Annotation;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.servlet.StubHttpServletRequest;
import com.metabroadcast.common.servlet.StubHttpServletResponse;
import com.metabroadcast.common.time.DateTimeZones;

public class ScheduleQueryResultWriterTest {

    ScheduleQueryResultWriter writer = new ScheduleQueryResultWriter();
    
    @Test
    public void testWrite() throws IOException {
        Channel channel = new Channel(Publisher.BBC, "aTitle", "aKey", MediaType.VIDEO, "aUri");
        channel.setId(1234l);
        
        DateTime from = new DateTime(0, DateTimeZones.UTC);
        DateTime to = new DateTime(1, DateTimeZones.UTC);
        Interval interval = new Interval(from, to);
        
        Item item = new Item("aUri","aCurie",Publisher.BBC);
        item.setId(4321l);
        item.setTitle("aTitle");
        
        Iterable<Item> entries = ImmutableList.of(item);
        ChannelSchedule cs = new ChannelSchedule(channel, interval, entries);
        
        HttpServletRequest request = new StubHttpServletRequest();
        StubHttpServletResponse response = new StubHttpServletResponse();
        writer.write(new ScheduleQueryResult(request, response, cs, Annotation.defaultAnnotations(), ApplicationConfiguration.DEFAULT_CONFIGURATION));
        response.getWriter().flush();
        System.out.println(response.getResponseAsString());
    }

}
