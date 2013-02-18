package org.atlasapi.query.v4.schedule;

import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.media.content.schedule.ScheduleIndex;
import org.atlasapi.media.content.schedule.ScheduleRef;
import org.atlasapi.persistence.media.channel.ChannelResolver;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.metabroadcast.common.time.SystemClock;

@Controller
public class ScheduleIndexDebugController {

    private static final Duration MAX_REQUEST_DURATION = Duration.standardDays(1);
    
    private final Gson gson = new GsonBuilder().registerTypeAdapter(DateTime.class, new JsonSerializer<DateTime>() {
        @Override
        public JsonElement serialize(DateTime src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }
    }).create();
    private final ScheduleIndex index;
    private final ScheduleRequestParser requestParser;

    public ScheduleIndexDebugController(ScheduleIndex index, ChannelResolver channelResolver, ApplicationConfigurationFetcher appFetcher) {
        this.index = index;
        this.requestParser = new ScheduleRequestParser(
            channelResolver,
            appFetcher,
            MAX_REQUEST_DURATION,
            new SystemClock()
        );
    }

    @RequestMapping({"/system/debug/schedules/{cid}.*","/system/debug/schedules/{cid}"})
    public void debugSchedule(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ScheduleQuery query = requestParser.queryFrom(request);
        ListenableFuture<ScheduleRef> resolveSchedule = index.resolveSchedule(query.getSource(), query.getChannel(), query.getInterval());
        gson.toJson(resolveSchedule.get(5, TimeUnit.SECONDS), response.getWriter());
    }
    
}
