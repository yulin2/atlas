package org.atlasapi.remotesite.talktalk;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.atlasapi.media.entity.Content;
import org.atlasapi.remotesite.talktalk.vod.bindings.ChannelType;
import org.atlasapi.remotesite.talktalk.vod.bindings.ItemTypeType;
import org.atlasapi.remotesite.talktalk.vod.bindings.VODEntityType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.metabroadcast.common.http.HttpStatusCode;

/**
 * Controller for updating specific channels, brands, series and episodes from
 * TalkTalk.
 * 
 */
@Controller
public class TalkTalkContentUpdateController {
    
    private TalkTalkChannelProcessor<?> channelProcessor;
    private TalkTalkVodEntityProcessor<List<Content>> entityProcessor;

    public TalkTalkContentUpdateController(TalkTalkChannelProcessor<?> channelProcessor, TalkTalkVodEntityProcessor<List<Content>> entityProcessor) {
        this.channelProcessor = checkNotNull(channelProcessor);
        this.entityProcessor = checkNotNull(entityProcessor);
    }
    
    @RequestMapping(value="/system/update/talktalk/channels/{cid}", method=RequestMethod.POST)
    public void updateChannel(HttpServletResponse response, @PathVariable("cid") String cid) throws IOException {
        try {
            ChannelType channel = new ChannelType();
            channel.setId(cid);
            Object result = channelProcessor.process(channel);
            sendResult(response, result);
        } catch (TalkTalkException e) {
            sendError(response, e);
        }
    }
    
    @RequestMapping(value="/system/update/talktalk/brands/{id}", method=RequestMethod.POST)
    public void updateBrand(HttpServletResponse response, @PathVariable("id") String id) throws IOException {
        try {
            VODEntityType entity = entity(id, ItemTypeType.BRAND);
            sendResult(response, entityProcessor.processEntity(entity));
        } catch (Exception e) {
            sendError(response, e);
        }
    }

    @RequestMapping(value="/system/update/talktalk/series/{id}", method=RequestMethod.POST)
    public void updateSeries(HttpServletResponse response, @PathVariable("id") String id) throws IOException {
        try {
            VODEntityType entity = entity(id, ItemTypeType.SERIES);
            sendResult(response, entityProcessor.processEntity(entity));
        } catch (Exception e) {
            sendError(response, e);
        }
    }
    
    @RequestMapping(value="/system/update/talktalk/episodes/{id}", method=RequestMethod.POST)
    public void updateEpisode(HttpServletResponse response, @PathVariable("id") String id) throws IOException {
        try {
            VODEntityType entity = entity(id, ItemTypeType.EPISODE);
            sendResult(response, entityProcessor.processEntity(entity));
        } catch (Exception e) {
            sendError(response, e);
        }
    }
    
    private void sendError(HttpServletResponse response, Exception e) throws IOException {
        response.setStatus(HttpStatusCode.SERVER_ERROR.code());
        e.printStackTrace(response.getWriter());
    }

    private VODEntityType entity(String id, ItemTypeType type) {
        VODEntityType entity = new VODEntityType();
        entity.setItemType(type);
        entity.setId(id);
        return entity;
    }

    private void sendResult(HttpServletResponse response, Object result) throws IOException {
        String msg = result.toString();
        response.setStatus(HttpStatusCode.OK.code());
        response.setContentLength(msg.length());
        response.getWriter().println(msg);
    }
    
}
