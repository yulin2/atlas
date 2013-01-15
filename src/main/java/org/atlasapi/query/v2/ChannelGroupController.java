package org.atlasapi.query.v2;

import static org.atlasapi.query.v2.ChannelController.ANNOTATION_KEY;
import static org.atlasapi.query.v2.ChannelController.HISTORY_ANNOTATION;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.media.channel.ChannelGroup;
import org.atlasapi.media.channel.ChannelGroupResolver;
import org.atlasapi.media.channel.ChannelGroupType;
import org.atlasapi.media.channel.Platform;
import org.atlasapi.media.entity.simple.ChannelGroupQueryResult;
import org.atlasapi.query.v2.ChannelGroupFilterer.ChannelGroupFilter;
import org.atlasapi.query.v2.ChannelGroupFilterer.ChannelGroupFilter.ChannelGroupFilterBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Flushables;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.metabroadcast.common.http.HttpStatusCode;
import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.query.Selection;
import com.metabroadcast.common.query.Selection.SelectionBuilder;

@Controller
public class ChannelGroupController {

    private static final String CHANNELS_ANNOTATION = "channels";
    private static final String TYPE_KEY = "type";
    private static final String PLATFORM_ID_KEY = "platform_id";
    private static final SelectionBuilder SELECTION_BUILDER = Selection.builder().withMaxLimit(50).withDefaultLimit(10);
    private static final Splitter CSV_SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();
    private static final Set<String> VALID_ANNOTATIONS = ImmutableSet.<String>builder()
        .add(CHANNELS_ANNOTATION)
        .add(HISTORY_ANNOTATION)
        .build();
    private final ChannelGroupResolver channelGroupResolver;
    private final ChannelSimplifier simplifier;
    private final ChannelGroupFilterer filterer = new ChannelGroupFilterer();
    private final Gson gson;
    private final NumberToShortStringCodec idCodec;
   

    public ChannelGroupController(ChannelGroupResolver channelGroupResolver, NumberToShortStringCodec idCodec, ChannelSimplifier simplifier) {
        this.channelGroupResolver = channelGroupResolver;
        this.idCodec = idCodec;
        this.simplifier = simplifier;
        this.gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    }

    @RequestMapping("/3.0/channel_groups.json")
    public void listChannels(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value = ANNOTATION_KEY, required = false) String annotationsStr,
            @RequestParam(value = TYPE_KEY, required = false) String type,
            @RequestParam(value = PLATFORM_ID_KEY, required = false) String platformId) throws IOException {
        try {
            List<ChannelGroup> channelGroups = ImmutableList.copyOf(channelGroupResolver.channelGroups());

            Selection selection = SELECTION_BUILDER.build(request);        
            channelGroups = selection.applyTo(filterer.filter(channelGroups, constructFilter(platformId, type)));

            Set<String> annotations = checkAnnotationValidity(splitString(annotationsStr));
            writeOut(response, request, new ChannelGroupQueryResult(simplifier.simplify(channelGroups, showChannels(annotations), showHistory(annotations))));
        } catch (IllegalArgumentException e) {
            response.sendError(HttpStatusCode.BAD_REQUEST.code(), e.getMessage());
        }
    }

    private Set<String> checkAnnotationValidity(Set<String> annotations) {
        for (String annotation : annotations) {
            if (!VALID_ANNOTATIONS.contains(annotation)) {
                throw new IllegalArgumentException(annotation + " is not a valid annotation");
            }
        }
        return annotations;
    }

    @RequestMapping("/3.0/channel_groups/{id}.json")
    public void listChannel(HttpServletRequest request, HttpServletResponse response, 
            @PathVariable("id") String id,
            @RequestParam(value = ANNOTATION_KEY, required = false) String annotationsStr) throws IOException {
        try {
            Optional<ChannelGroup> possibleChannelGroup = channelGroupResolver.channelGroupFor(idCodec.decode(id).longValue());
            if (!possibleChannelGroup.isPresent()) {
                response.sendError(HttpStatusCode.NOT_FOUND.code());
            } else {
                Set<String> annotations = checkAnnotationValidity(splitString(annotationsStr));
                writeOut(response, request, new ChannelGroupQueryResult(simplifier.simplify(ImmutableList.of(possibleChannelGroup.get()), showChannels(annotations), showHistory(annotations))));
            }
        } catch (IllegalArgumentException e) {
            response.sendError(HttpStatusCode.BAD_REQUEST.code(), e.getMessage());
        }
    }
    
    private ChannelGroupFilter constructFilter(String platformId, String type) {
        ChannelGroupFilterBuilder filter = ChannelGroupFilter.builder();
        
        if (!Strings.isNullOrEmpty(platformId)) {
            // resolve platform if present
            Optional<ChannelGroup> possiblePlatform = channelGroupResolver.channelGroupFor(idCodec.decode(platformId).longValue());
            if (!possiblePlatform.isPresent()) {
                throw new IllegalArgumentException("could not resolve channel group with id " + platformId);
            }
            if (!(possiblePlatform.get() instanceof Platform)) {
                throw new IllegalArgumentException("channel group with id " + platformId + " not a platform");
            }
            filter.withPlatform((Platform)possiblePlatform.get());
        }

        if (!Strings.isNullOrEmpty(type)) {
            // resolve channelGroup type
            if (type.equals("platform")) {
                filter.withType(ChannelGroupType.PLATFORM);
            } else if (type.equals("region")) {
                filter.withType(ChannelGroupType.REGION);
            } else {
                throw new IllegalArgumentException("type provided was not valid, should be either platform or region");
            }
        }
        
        return filter.build();
    }
    
    private void writeOut(HttpServletResponse response, HttpServletRequest request, ChannelGroupQueryResult channelGroupQueryResult) throws IOException {

        String callback = callback(request);
        
        OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream(), Charsets.UTF_8);
        boolean ignoreEx = true;
        try {
            if (callback != null) {
                writer.write(callback + "(");
            }
            gson.toJson(channelGroupQueryResult, writer);
            if (callback != null) {
                writer.write(");");
            }
            ignoreEx = false;
        } finally {
            Flushables.flush(writer, ignoreEx);
        }
    }
    
    private String callback(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String callback = request.getParameter("callback");
        if (Strings.isNullOrEmpty(callback)) {
            return null;
        }

        try {
            return URLEncoder.encode(callback, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
    
    private Set<String> splitString(String value) {
        if (!Strings.isNullOrEmpty(value)) {
            return ImmutableSet.copyOf(CSV_SPLITTER.split(Strings.nullToEmpty(value)));
        }
        return ImmutableSet.of();
    }

    private boolean showChannels(Set<String> annotations) {
        return annotations.contains(CHANNELS_ANNOTATION);
    }

    private boolean showHistory(Set<String> annotations) {
        return annotations.contains(HISTORY_ANNOTATION);
    }
}
