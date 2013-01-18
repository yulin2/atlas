package org.atlasapi.query.v2;

import java.io.IOException;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.entity.ChannelSchedule;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.output.AtlasModelWriter;
import org.atlasapi.output.ErrorSummary;
import org.atlasapi.persistence.content.ScheduleResolver;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.media.channel.ChannelResolver;
import org.joda.time.DateTime;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;
import com.metabroadcast.common.webapp.query.DateTimeInQueryParser;

@Controller
public class ScheduleController extends BaseController<Iterable<ChannelSchedule>> {
    
    private final DateTimeInQueryParser dateTimeInQueryParser = new DateTimeInQueryParser();
    private final ScheduleResolver scheduleResolver;
	private ChannelResolver channelResolver;
	
    private final NumberToShortStringCodec idCodec = new SubstitutionTableNumberCodec();
    
    public ScheduleController(ScheduleResolver scheduleResolver, ChannelResolver channelResolver, ApplicationConfigurationFetcher configFetcher, AdapterLog log, AtlasModelWriter<Iterable<ChannelSchedule>> outputter) {
        super(configFetcher, log, outputter);
        this.scheduleResolver = scheduleResolver;
        this.channelResolver = channelResolver;
    }

    @RequestMapping("/3.0/schedule.*")
    public void schedule(
            @RequestParam(required=false) String to, @RequestParam(required=false) String from, 
            @RequestParam(required=false) String on, 
            @RequestParam(required=false) String channel, @RequestParam(value="channel_id",required=false) String channelId, 
            @RequestParam String publisher, 
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            DateTime fromWhen = null;
            DateTime toWhen = null;

            if (! Strings.isNullOrEmpty(on)) {
                fromWhen = dateTimeInQueryParser.parse(on);
                toWhen = dateTimeInQueryParser.parse(on);
            } else if (! Strings.isNullOrEmpty(to) && ! Strings.isNullOrEmpty(from)) {
                fromWhen = dateTimeInQueryParser.parse(from);
                toWhen = dateTimeInQueryParser.parse(to);
            } else {
                throw new IllegalArgumentException("You must pass either 'on' or 'from' and 'to'");
            }
            
            ApplicationConfiguration appConfig = appConfig(request);
            Set<Publisher> publishers = publishers(publisher, appConfig);
            if (publishers.isEmpty()) {
                throw new IllegalArgumentException("You must specify at least one publisher that you have permission to view");
            }
            
            if (Strings.isNullOrEmpty(channelId) && Strings.isNullOrEmpty(channel) || !Strings.isNullOrEmpty(channelId) && !Strings.isNullOrEmpty(channel)) {
                throw new IllegalArgumentException("You must specify exactly one of channel and channel_id");
            }
            Set<Channel> channels = Strings.isNullOrEmpty(channel) ? channelsFromIds(channelId) : channelsFromKeys(channel);
            if (channels.isEmpty()) {
                throw new IllegalArgumentException("You must specify at least one channel that exists using the channel or channel_id parameter");
            }
            
            modelAndViewFor(request, response, scheduleResolver.schedule(fromWhen, toWhen, channels, publishers).channelSchedules(), appConfig);
        } catch (Exception e) {
            errorViewFor(request, response, ErrorSummary.forException(e));
        }
    }
    
    private Set<Channel> channelsFromIds(String channelId) {
        return ImmutableSet.copyOf(Iterables.transform(Iterables.filter(Iterables.transform(URI_SPLITTER.split(channelId), new Function<String, Maybe<Channel>>() {
            @Override
            public Maybe<Channel> apply(String input) {
                return channelResolver.fromId(Id.valueOf(idCodec.decode(input)));
            }
        }),Maybe.HAS_VALUE),Maybe.<Channel>requireValueFunction()));
    }

    private Set<Channel> channelsFromKeys(String channelString) {
        ImmutableSet.Builder<Channel> channels = ImmutableSet.builder();
        for (String channelKey: URI_SPLITTER.split(channelString)) {
            Maybe<Channel> channel = channelResolver.fromKey(channelKey);
            if (channel.hasValue()) {
                channels.add(channel.requireValue());
            }
        }
        return channels.build();
    }
}
