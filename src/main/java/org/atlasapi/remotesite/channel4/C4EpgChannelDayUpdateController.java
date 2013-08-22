package org.atlasapi.remotesite.channel4;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.servlet.http.HttpServletResponse;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.remotesite.channel4.epg.C4EpgChannelDayUpdater;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.metabroadcast.common.http.HttpStatusCode;

@Controller
public class C4EpgChannelDayUpdateController {

    private final DateTimeFormatter parser = ISODateTimeFormat.date();
    private final C4EpgChannelDayUpdater updater;
    private final C4AtomApi atomApi;

    public C4EpgChannelDayUpdateController(C4AtomApi atomApi, C4EpgChannelDayUpdater updater) {
        this.atomApi = atomApi;
        this.updater = updater;
    }
    
    @RequestMapping(value="/system/update/c4/epg/{channelKey}/{day}",method=RequestMethod.POST)
    public void doUpdate(@PathVariable String channelKey, @PathVariable String day, HttpServletResponse response) {
        
        Channel channel = checkNotNull(atomApi.getChannelMap().get(channelKey));
        
        LocalDate scheduleDay = parser.parseLocalDate(day);
        
        updater.update(channelKey, channel, scheduleDay);
        
        response.setStatus(HttpStatusCode.OK.code());
        response.setContentLength(0);
        
    }
    
}
