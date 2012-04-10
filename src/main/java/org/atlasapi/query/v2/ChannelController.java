package org.atlasapi.query.v2;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.channel.ChannelGroup;
import org.atlasapi.media.channel.ChannelGroupStore;
import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.media.entity.simple.ChannelQueryResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Iterables;
import com.google.common.io.Flushables;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.http.HttpStatusCode;
import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.query.Selection;
import com.metabroadcast.common.query.Selection.SelectionBuilder;

@Controller
// TODO transplant on to BaseController when annotations are available.
public class ChannelController {

    private static final SelectionBuilder SELECTION_BUILDER = Selection.builder().withMaxLimit(50).withDefaultLimit(10);
    private static final Splitter CSV_SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();

    private final ChannelResolver channelResolver;
    private final ChannelGroupStore channelGroupResolver;
    private final NumberToShortStringCodec idCodec;
    private final ChannelSimplifier channelSimplifier;

    private Gson gson;

    public ChannelController(ChannelResolver channelResolver, ChannelGroupStore channelGroupResolver, NumberToShortStringCodec idCodec, ChannelSimplifier channelSimplifier) {
        this.channelResolver = channelResolver;
        this.channelGroupResolver = channelGroupResolver;
        this.idCodec = idCodec;
        this.channelSimplifier = channelSimplifier;
        this.gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    }

    @RequestMapping("/3.0/channels.json")
    public void listChannels(HttpServletRequest request, HttpServletResponse response, @RequestParam(required = false) String key,
            @RequestParam(value = "package", required = false) String packageName, @RequestParam(required = false) String regions) throws IOException {

        Selection selection = SELECTION_BUILDER.build(request);

        List<Channel> channels;
        if (!Strings.isNullOrEmpty(key)) {
            Maybe<Channel> channelFromKey = channelResolver.fromKey(key);
            if (channelFromKey.isNothing()) {
                response.setStatus(HttpStatusCode.NOT_FOUND.code());
                response.setContentLength(0);
                return;
            }
            channels = ImmutableList.of(channelFromKey.requireValue());
        } else {
            Set<Long> channelGroupIds = getChannelGroups(packageName, regions);
            if (!channelGroupIds.isEmpty()) {
                Iterable<ChannelGroup> channelGroups = channelGroupResolver.channelGroupsFor(channelGroupIds);
                channels = selection.applyTo(channelResolver.forIds(ImmutableSet.copyOf(concat(transform(channelGroups, TO_CHANNEL_IDS)))));
            } else {
                channels = selection.applyTo(channelResolver.all());
            }
        }

        writeOut(response, request, new ChannelQueryResult(channelSimplifier.simplify(channels, showChannelGroups(request))));
    }

    private Set<Long> getChannelGroups(String packageName, String regions) {
        Builder<Long> channelGroups = ImmutableSet.builder();
        if (packageName != null) {
            channelGroups.addAll(Iterables.transform(CSV_SPLITTER.split(packageName), TO_LONG));
        }
        if (regions != null) {
            channelGroups.addAll(Iterables.transform(CSV_SPLITTER.split(regions), TO_LONG));
        }
        return channelGroups.build();
    }

    @RequestMapping("/3.0/channels/{id}.json")
    public void listChannel(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") String id) throws IOException {

        Maybe<Channel> possibleChannel = channelResolver.fromId(idCodec.decode(id).longValue());

        if (possibleChannel.isNothing()) {
            response.sendError(HttpStatusCode.NOT_FOUND.code());
        }

        writeOut(response, request, new ChannelQueryResult(channelSimplifier.simplify(ImmutableList.of(possibleChannel.requireValue()), showChannelGroups(request))));
    }

    public boolean showChannelGroups(HttpServletRequest request) {
        return ImmutableSet.copyOf(CSV_SPLITTER.split(Strings.nullToEmpty(request.getParameter("annotations")))).contains("channel_groups");
    }

    private void writeOut(HttpServletResponse response, HttpServletRequest request, ChannelQueryResult channelQueryResult) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream(), Charsets.UTF_8);
        boolean ignoreEx = true;
        try {
            gson.toJson(channelQueryResult, writer);
            ignoreEx = false;
        } finally {
            Flushables.flush(writer, ignoreEx);
        }
    }

    private static Function<String, Long> TO_LONG = new Function<String, Long>() {
        @Override
        public Long apply(String input) {
            return Long.parseLong(input);
        }
    };

    private static Function<ChannelGroup, Set<Long>> TO_CHANNEL_IDS = new Function<ChannelGroup, Set<Long>>() {
        @Override
        public Set<Long> apply(ChannelGroup input) {
            return input.getChannels();
        }
    };
}
