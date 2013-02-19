package org.atlasapi.query.v2;

import static com.google.common.collect.Iterables.transform;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.channel.ChannelGroup;
import org.atlasapi.media.channel.ChannelGroupResolver;
import org.atlasapi.media.channel.ChannelNumbering;
import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.simple.ChannelQueryResult;
import org.atlasapi.media.entity.simple.HistoricalChannelEntry;
import org.atlasapi.query.v2.ChannelFilterer.ChannelFilter;
import org.atlasapi.query.v2.ChannelFilterer.ChannelFilter.ChannelFilterBuilder;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.common.io.Flushables;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.base.MoreOrderings;
import com.metabroadcast.common.caching.BackgroundComputingValue;
import com.metabroadcast.common.http.HttpStatusCode;
import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.media.MimeType;
import com.metabroadcast.common.query.Selection;
import com.metabroadcast.common.query.Selection.SelectionBuilder;

@Controller
// TODO transplant on to BaseController when annotations are available.
public class ChannelController {

    private static final String PARENT_ANNOTATION = "parent";
    private static final String VARIATIONS_ANNOTATION = "variations";
    private static final String CHANNEL_GROUPS_ANNOTATION = "channel_groups";
    public static final String HISTORY_ANNOTATION = "history";
    public static final String ANNOTATION_KEY = "annotations";
    private static final Set<String> VALID_ANNOTATIONS = ImmutableSet.<String>builder()
            .add(CHANNEL_GROUPS_ANNOTATION)
            .add(HISTORY_ANNOTATION)
            .add(PARENT_ANNOTATION)
            .add(VARIATIONS_ANNOTATION)
            .build();
    private static final SelectionBuilder SELECTION_BUILDER = Selection.builder().withMaxLimit(100).withDefaultLimit(10);
    private static final Splitter CSV_SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();
    private static final String TITLE = "title";
    private static final Object TITLE_REVERSE = "title.reverse";

    private final ChannelSimplifier channelSimplifier;
    private final ChannelFilterer filterer = new ChannelFilterer();
    private final Gson gson;
    private final BackgroundComputingValue<ChannelAndGroupsData> data;
    private final NumberToShortStringCodec codec;
    private final ApplicationConfigurationFetcher configFetcher;
    
    public ChannelController(final ChannelResolver channelResolver, ChannelGroupResolver channelGroupResolver, ChannelSimplifier channelSimplifier, NumberToShortStringCodec codec, ApplicationConfigurationFetcher configFetcher) {
        this.channelSimplifier = channelSimplifier;
        this.codec = codec;
        this.gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        
        this.data = new BackgroundComputingValue<ChannelController.ChannelAndGroupsData>(Duration.standardMinutes(10), new ChannelAndGroupsDataUpdater(channelResolver, channelGroupResolver));
        this.configFetcher = configFetcher;
    }
    
    @PostConstruct
    public void start() {
        data.start();
    }
    
    @PreDestroy
    public void shutdown() {
        data.shutdown();
    }

    @RequestMapping("/3.0/channels.json")
    public void listChannels(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value = ANNOTATION_KEY, required = false) String annotationsStr,
            @RequestParam(value = "key", required = false) String channelKey,
            @RequestParam(value = "platforms", required = false) String platformKey, 
            @RequestParam(value = "regions", required = false) String regionKeys, 
            @RequestParam(value = "broadcaster", required = false) String broadcasterKey,
            @RequestParam(value = "media_type", required = false) String mediaTypeKey, 
            @RequestParam(value = "available_from", required = false) String availableFromKey,
            @RequestParam(value = "order_by", required = false) String orderBy) throws IOException {
        try {
            Selection selection = SELECTION_BUILDER.build(request);
            //App Config for filtering
            final ApplicationConfiguration appConfig;
            Maybe<ApplicationConfiguration> possibleAppConfig = configFetcher.configurationFor(request);
            if(possibleAppConfig.isNothing()){
                appConfig = ApplicationConfiguration.defaultConfiguration();
            }else{
                appConfig = possibleAppConfig.requireValue();
            }
            List<Channel> channels = null;
            if (!Strings.isNullOrEmpty(channelKey)) {
                Channel channelFromKey = data.get().keyToChannel.get(channelKey);
                if (channelFromKey == null) {
                    response.setStatus(HttpStatusCode.NOT_FOUND.code());
                    response.setContentLength(0);
                    return;
                }
                if(appConfig.isEnabled(channelFromKey.source())){
                    channels = ImmutableList.of(channelFromKey);
                }else{
                    response.sendError(HttpStatusCode.FORBIDDEN.code(),
                            "API key doesn't allow use of this channel.");
                }
            } else {
                Optional<Ordering<Channel>> ordering = ordering(orderBy);
                if (ordering.isPresent()) {
                    channels = ordering.get().immutableSortedCopy(data.get().allChannels);
                }
                else {
                    channels = ImmutableList.copyOf(data.get().allChannels);
                }
                channels = selection.applyTo(Iterables.filter(filterer.filter(channels, constructFilter(platformKey, regionKeys, broadcasterKey, mediaTypeKey, availableFromKey), data.get().channelToGroups), 
                        new Predicate<Channel>() {
                    @Override
                    public boolean apply(@Nullable Channel channel) {
                        return appConfig.isEnabled(channel.source());
                    }}
                ));
            }

            Set<String> annotations = checkAnnotationValidity(splitString(annotationsStr));
            writeOut(response, request, new ChannelQueryResult(channelSimplifier.simplify(channels, showChannelGroups(annotations), showHistory(annotations), showParent(annotations), showVariations(annotations))));
        } catch (IllegalArgumentException e) {
            response.sendError(HttpStatusCode.BAD_REQUEST.code(), e.getMessage());
        }
    }
    
    @RequestMapping("/3.0/channels/{id}.json")
    public void listChannel(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value = ANNOTATION_KEY, required = false) String annotationsStr, 
            @PathVariable("id") String id) throws IOException {
        try {
            Channel possibleChannel = data.get().idToChannel.get(codec.decode(id).longValue());
            //App Config for filtering
            Maybe<ApplicationConfiguration> possibleAppConfig = configFetcher.configurationFor(request);
            final ApplicationConfiguration appConfig;
            if(possibleAppConfig.isNothing()){
                appConfig = ApplicationConfiguration.defaultConfiguration();
            }else{
                appConfig = possibleAppConfig.requireValue();
            }

            if (possibleChannel == null) {
                response.sendError(HttpStatusCode.NOT_FOUND.code());
            }
            if(!appConfig.isEnabled(possibleChannel.source())){
                response.sendError(HttpStatusCode.FORBIDDEN.code(),
                        "API key doesn't allow use of this channel.");
            }

            Set<String> annotations = checkAnnotationValidity(splitString(annotationsStr));
            writeOut(response, request, new ChannelQueryResult(channelSimplifier.simplify(ImmutableList.of(possibleChannel), showChannelGroups(annotations), showHistory(annotations), showParent(annotations), showVariations(annotations))));
        } catch (IllegalArgumentException e) {
            response.sendError(HttpStatusCode.BAD_REQUEST.code(), e.getMessage());
        }
    }
    
    private Optional<Ordering<Channel>> ordering(String orderBy) {
        if (!Strings.isNullOrEmpty(orderBy)) {
            if (orderBy.equals(TITLE)) {
                return Optional.of(MoreOrderings.transformingOrdering(TO_TITLE));
            } else if (orderBy.equals(TITLE_REVERSE)) {
                return Optional.of(MoreOrderings.transformingOrdering(TO_TITLE, Ordering.<String>natural().reverse()));
            }
        }
        
        return Optional.absent();
    }
    
    private static final Function<Channel, String> TO_TITLE = new Function<Channel, String>() {
        @Override
        public String apply(Channel input) {
            return input.title();
        }
    };
    
    private ChannelFilter constructFilter(String platformId, String regionIds, String broadcasterKey, String mediaTypeKey, String availableFromKey) {
        ChannelFilterBuilder filter = ChannelFilter.builder();
        
        Set<ChannelGroup> channelGroups = getChannelGroups(platformId, regionIds);
        if (!channelGroups.isEmpty()) {
            filter.withChannelGroups(channelGroups);
        }
        
        if (!Strings.isNullOrEmpty(broadcasterKey)) {
            filter.withBroadcaster(Publisher.fromKey(broadcasterKey).requireValue());
        }
        
        if (!Strings.isNullOrEmpty(mediaTypeKey)) {
            filter.withMediaType(MediaType.valueOf(mediaTypeKey.toUpperCase()));
        }
        
        if (!Strings.isNullOrEmpty(availableFromKey)) {
            filter.withAvailableFrom(Publisher.fromKey(availableFromKey).requireValue());
        }
        
        return filter.build();
    }

    private Set<ChannelGroup> getChannelGroups(String platformId, String regionIds) {
        Set<Long> channelGroups = Sets.newHashSet();
        if (platformId != null) {
            Iterables.addAll(channelGroups, transform(CSV_SPLITTER.split(platformId), toDecodedId));
        }
        if (regionIds != null) {
            Iterables.addAll(channelGroups, transform(CSV_SPLITTER.split(regionIds), toDecodedId));
        }
        
        if (channelGroups.isEmpty()) {
            return ImmutableSet.of();
        } else {
            return ImmutableSet.copyOf(transform(channelGroups, Functions.forMap(data.get().idToChannelGroup)));
        }
    }
    
    private final Function<String, Long> toDecodedId = new Function<String, Long>() {

        @Override
        public Long apply(String input) {
            return codec.decode(input).longValue();
        }
    };

    private Set<String> checkAnnotationValidity(Set<String> annotations) {
        for (String annotation : annotations) {
            if (!VALID_ANNOTATIONS.contains(annotation)) {
                throw new IllegalArgumentException(annotation + " is not a valid annotation");
            }
        }
        return annotations;
    }
    
    private Set<String> splitString(String value) {
        if (!Strings.isNullOrEmpty(value)) {
            return ImmutableSet.copyOf(CSV_SPLITTER.split(Strings.nullToEmpty(value)));
        }
        return ImmutableSet.of();
    }

    private boolean showChannelGroups(Set<String> annotations) {
        return annotations.contains(CHANNEL_GROUPS_ANNOTATION);
    }
    
    private boolean showHistory(Set<String> annotations) {
        return annotations.contains(HISTORY_ANNOTATION);
    }
    
    private boolean showParent(Set<String> annotations) {
        return annotations.contains(PARENT_ANNOTATION);
    }
    
    private boolean showVariations(Set<String> annotations) {
        return annotations.contains(VARIATIONS_ANNOTATION);
    }
    
    private void writeOut(HttpServletResponse response, HttpServletRequest request, ChannelQueryResult channelQueryResult) throws IOException {
        response.setCharacterEncoding(Charsets.UTF_8.toString());
        response.setContentType(MimeType.APPLICATION_JSON.toString());
        
        String callback = callback(request);
        
        OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream(), Charsets.UTF_8);
        boolean ignoreEx = true;
        try {
            if (callback != null) {
                writer.write(callback + "(");
            }
            gson.toJson(channelQueryResult, writer);
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

    private static Function<String, Long> TO_LONG = new Function<String, Long>() {
        @Override
        public Long apply(String input) {
            return Long.parseLong(input);
        }
    };

    private class ChannelAndGroupsDataUpdater implements Callable<ChannelAndGroupsData> {
        private final ChannelResolver channelResolver;
        private final ChannelGroupResolver channelGroupResolver;

        public ChannelAndGroupsDataUpdater(ChannelResolver channelResolver, ChannelGroupResolver channelGroupResolver) {
            this.channelResolver = channelResolver;
            this.channelGroupResolver = channelGroupResolver;
        }

        @Override
        public ChannelAndGroupsData call() throws Exception {
            Set<Channel> allChannels = ImmutableSet.copyOf(channelResolver.all());
            Set<ChannelGroup> allChannelGroups = ImmutableSet.copyOf(channelGroupResolver.channelGroups());
            Map<Long, Channel> idToChannel = Maps.uniqueIndex(allChannels, Channel.TO_ID);
            Map<String, Channel> keyToChannel = Maps.uniqueIndex(allChannels, Channel.TO_KEY);
            Map<Long, ChannelGroup> idToChannelGroup = Maps.uniqueIndex(allChannelGroups, ChannelGroup.TO_ID);
            
            // TODO rewrite this, can link from channels to channelgroups, rather than fetching all channelgroups, and computing the links backwards
            SetMultimap<Long, ChannelGroup> channelToGroups = HashMultimap.create();
            for (ChannelGroup group : allChannelGroups) {
                for (ChannelNumbering channelNumbering : group.getChannelNumberings()) {
                    channelToGroups.put(channelNumbering.getChannel(), group);
                }
            }
            
            return new ChannelAndGroupsData(allChannels, idToChannel, keyToChannel, idToChannelGroup, channelToGroups);
        }
    }
    
    private class ChannelAndGroupsData {
        private final Set<Channel> allChannels;
        private final SetMultimap<Long, ChannelGroup> channelToGroups;
        private final Map<Long, Channel> idToChannel;
        private final Map<String, Channel> keyToChannel;
        private final Map<Long, ChannelGroup> idToChannelGroup;
        
        public ChannelAndGroupsData(Set<Channel> allChannels, Map<Long, Channel> idToChannel, Map<String, Channel> keyToChannel, Map<Long, ChannelGroup> idToChannelGroup, SetMultimap<Long, ChannelGroup> channelToGroups) {
            this.allChannels = allChannels;
            this.idToChannel = idToChannel;
            this.keyToChannel = keyToChannel;
            this.idToChannelGroup = idToChannelGroup;
            this.channelToGroups = channelToGroups;
        }
    }
}
