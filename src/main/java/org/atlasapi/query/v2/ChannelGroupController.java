package org.atlasapi.query.v2;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.media.channel.ChannelGroup;
import org.atlasapi.persistence.media.channel.ChannelGroupStore;
import org.atlasapi.media.entity.simple.ChannelGroupQueryResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.http.HttpStatusCode;
import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.query.Selection;
import com.metabroadcast.common.query.Selection.SelectionBuilder;
import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.output.AtlasModelWriter;
import org.atlasapi.persistence.logging.AdapterLog;

@Controller
public class ChannelGroupController extends BaseController<ChannelGroupQueryResult> {

    private static final SelectionBuilder SELECTION_BUILDER = Selection.builder().withMaxLimit(50).withDefaultLimit(10);
    private static final Splitter CSV_SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();
    private final ChannelGroupStore channelGroupResolver;
    private final ChannelSimplifier simplifier;
    private final NumberToShortStringCodec idCodec;

    public ChannelGroupController(ChannelGroupStore channelGroupResolver, NumberToShortStringCodec idCodec, ChannelSimplifier simplifier, ApplicationConfigurationFetcher configFetcher, AdapterLog log, AtlasModelWriter<ChannelGroupQueryResult> outputter) {
        super(configFetcher, log, outputter);
        this.channelGroupResolver = channelGroupResolver;
        this.idCodec = idCodec;
        this.simplifier = simplifier;
    }

    @RequestMapping("/3.0/channel_groups.json")
    public void listChannels(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Selection selection = SELECTION_BUILDER.build(request);
        modelAndViewFor(request, response, new ChannelGroupQueryResult(simplifier.simplify(selection.applyTo(channelGroupResolver.channelGroups()), showChannels(request))), ApplicationConfiguration.DEFAULT_CONFIGURATION);
    }

    @RequestMapping("/3.0/channel_groups/{id}.json")
    public void listChannel(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") String id) throws IOException {
        Optional<ChannelGroup> possibleChannelGroup = channelGroupResolver.channelGroupFor(idCodec.decode(id).longValue());
        if (!possibleChannelGroup.isPresent()) {
            response.sendError(HttpStatusCode.NOT_FOUND.code());
        }
        modelAndViewFor(request, response, new ChannelGroupQueryResult(simplifier.simplify(ImmutableList.of(possibleChannelGroup.get()), showChannels(request))), ApplicationConfiguration.DEFAULT_CONFIGURATION);
    }

    private boolean showChannels(HttpServletRequest request) {
        return ImmutableSet.copyOf(CSV_SPLITTER.split(Strings.nullToEmpty(request.getParameter("annotations")))).contains("channels");
    }
}
