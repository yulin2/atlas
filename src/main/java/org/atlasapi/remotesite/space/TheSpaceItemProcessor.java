package org.atlasapi.remotesite.space;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.http.SimpleHttpRequest;
import com.metabroadcast.common.intl.Countries;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atlasapi.media.TransportType;
import org.atlasapi.media.entity.ChildRef;
import org.atlasapi.media.entity.Clip;
import org.atlasapi.media.content.Container;
import org.atlasapi.media.content.Content;
import org.atlasapi.media.entity.Described;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.ParentRef;
import org.atlasapi.media.entity.Policy;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Series;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.Duration;
import org.joda.time.format.ISODateTimeFormat;

/**
 */
public class TheSpaceItemProcessor {
    
    private final String BASE_CANONICAL_URI = "http://thespace.org";
    private final String BASE_ITEMS_URI = "http://thespace.org/items/";
    private final String BASE_CATEGORY_URI = "http://thespace.org/by/genre/";
    private final String EPISODE_TYPE = "episode";
    //
    private final Log logger = LogFactory.getLog(getClass());
    //
    private final String url;
    private final SimpleHttpClient client;
    private final AdapterLog log;
    private final ContentResolver contentResolver;
    private final ContentWriter contentWriter;
    
    public TheSpaceItemProcessor(String url, SimpleHttpClient client, AdapterLog log, ContentResolver contentResolver, ContentWriter contentWriter) {
        this.url = url;
        this.client = client;
        this.log = log;
        this.contentResolver = contentResolver;
        this.contentWriter = contentWriter;
    }
    
    public void process(JsonNode node) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        if (node.has("type") && node.has("pid")) {
            String type = node.get("type").asText();
            String pid = node.get("pid").asText();
            if (type.equals(EPISODE_TYPE)) {
                Item content = (Item) contentResolver.findByCanonicalUris(ImmutableSet.of(getItemsUri(pid))).getFirstValue().valueOrNull();
                boolean isTopLevel = !node.has("parent");
                if (content == null && isTopLevel) {
                    Item item = new Item();
                    fillItem(item, node, mapper);
                    contentWriter.createOrUpdate(item);
                } else if (content == null && !isTopLevel) {
                    Episode episode = new Episode();
                    fillItem(episode, node, mapper);
                    fillEpisode(episode, node, mapper);
                    contentWriter.createOrUpdate(episode);
                } else {
                    if (content instanceof Episode && isTopLevel) {
                        Item item = new Item();
                        detachEpisodeFromParent(content);
                        fillItem(item, node, mapper);
                        contentWriter.createOrUpdate(item);
                    } else if (content instanceof Episode) {
                        fillItem(content, node, mapper);
                        fillEpisode((Episode) content, node, mapper);
                        contentWriter.createOrUpdate(content);
                    } else {
                        fillItem(content, node, mapper);
                        contentWriter.createOrUpdate(content);
                    }
                }
            }
        }
    }

    private void detachEpisodeFromParent(Item content) {
        final Episode episode = (Episode) content;
        ParentRef parentRef = episode.getContainer();
        if (parentRef != null) {
            ResolvedContent parents = contentResolver.findByCanonicalUris(Arrays.asList(parentRef.getUri()));
            if (!parents.isEmpty() && parents.getFirstValue().requireValue() instanceof Series) {
                Container parent = (Container) parents.getFirstValue().requireValue();
                parent.setChildRefs(Iterables.filter(parent.getChildRefs(), new Predicate<ChildRef>() {

                    @Override
                    public boolean apply(ChildRef input) {
                        return !input.equals(episode.childRef());
                    }
                }));
                contentWriter.createOrUpdate(parent);
            } else {
                logger.warn("Cannot find parent for " + episode.getCanonicalUri());
            }
        }
    }
    
    private void fillItem(Item episode, JsonNode node, ObjectMapper mapper) throws Exception {
        try {
            JsonNode pid = node.get("pid");
            episode.setCanonicalUri(getItemsUri(pid.asText()));
            episode.setPublisher(Publisher.THESPACE);
            
            JsonNode title = node.get("title");
            if (title != null && !title.isNull()) {
                episode.setTitle(title.asText());
            }
            
            JsonNode mediaType = node.get("media_type");
            if (mediaType != null && mediaType.asText().toLowerCase().equals("audio")) {
                episode.setMediaType(MediaType.AUDIO);
            } else if (mediaType != null && mediaType.asText().toLowerCase().equals("audio_video")) {
                episode.setMediaType(MediaType.VIDEO);
            }
            
            fillSynopsis(node, episode);
            
            fillImage(node, episode);
            
            fillGenres(node, episode);
            
            fillClips(node, mapper, episode);
            
            fillVersions(node, episode);
        } catch (Exception ex) {
            log.record(new AdapterLogEntry(AdapterLogEntry.Severity.WARN).withDescription("Failed ingesting episode: " + episode.getCanonicalUri()).withSource(getClass()));
            throw ex;
        }
    }
    
    private void fillVersions(JsonNode node, Item episode) {
        JsonNode versions = node.get("versions");
        Set<Version> versionsObj = new HashSet<Version>();
        if (versions != null && !versions.isNull()) {
            Iterator<JsonNode> it = versions.getElements();
            while (it.hasNext()) {
                versionsObj.add(getVersion(it.next(), episode));
            }
        }
        episode.setVersions(versionsObj);
    }
    
    private void fillClips(JsonNode node, ObjectMapper mapper, Content content) throws Exception {
        Iterator<JsonNode> clips = node.get("available_clips").getElements();
        List<Clip> clipsObj = new LinkedList<Clip>();
        while (clips.hasNext()) {
            String cPid = clips.next().get("pid").asText();
            JsonNode clip = client.get(new SimpleHttpRequest<JsonNode>(url + "/items/" + cPid + ".json", new JSonNodeHttpResponseTransformer(mapper)));
            clipsObj.add(getClip(clip.get("programme")));
        }
        content.setClips(clipsObj);
    }
    
    private void fillGenres(JsonNode node, Described described) {
        Iterator<JsonNode> categories = node.get("categories").getElements();
        Set<String> genres = new HashSet<String>();
        while (categories.hasNext()) {
            String id = BASE_CATEGORY_URI + categories.next().get("id").asText();
            genres.add(id);
        }
        described.setGenres(new TheSpaceGenreMap().mapRecognised(genres));
    }
    
    private void fillImage(JsonNode node, Described described) {
        JsonNode image = node.get("image");
        if (image != null && !image.isNull()) {
            JsonNode smallImage = image.get("depiction_320");
            if (smallImage != null && !smallImage.isNull()) {
                described.setThumbnail(getImagesUri(smallImage.asText()));
            }
            JsonNode bigImage = image.get("depiction_640");
            if (bigImage != null && !bigImage.isNull()) {
                described.setImage(getImagesUri(bigImage.asText()));
            }
        }
    }
    
    private void fillSynopsis(JsonNode node, Described described) {
        JsonNode long_synopsis = node.get("long_synopsis");
        JsonNode medium_synopsis = node.get("medium_synopsis");
        JsonNode short_synopsis = node.get("short_synopsis");
        String synopsis = null;
        if (long_synopsis != null && !long_synopsis.isNull()) {
            synopsis = long_synopsis.asText();
        } else if (medium_synopsis != null && !medium_synopsis.isNull()) {
            synopsis = medium_synopsis.asText();
        } else if (short_synopsis != null && !short_synopsis.isNull()) {
            synopsis = short_synopsis.asText();
        }
        described.setDescription(synopsis);
    }
    
    private void fillEpisode(Episode episode, JsonNode node, ObjectMapper mapper) throws Exception {
        try {
            JsonNode position = node.get("position");
            if (position != null && !position.isNull()) {
                episode.setEpisodeNumber(position.asInt());
            }
            
            JsonNode parent = node.get("parent").get("programme");
            if (parent != null && !parent.isNull()) {
                String pPid = parent.get("pid").asText();
                Series series = (Series) contentResolver.findByCanonicalUris(ImmutableSet.of(getItemsUri(pPid))).getFirstValue().valueOrNull();
                if (series == null) {
                    series = new Series();
                }
                fillSeries(series, mapper, client.get(new SimpleHttpRequest<JsonNode>(url + "/items/" + pPid + ".json", new JSonNodeHttpResponseTransformer(mapper))).get("programme"));
                series.setChildRefs(Iterables.concat(series.getChildRefs(), ImmutableList.of(episode.childRef())));
                episode.setParentRef(ParentRef.parentRefFrom(series));
                contentWriter.createOrUpdate(series);
            }
        } catch (Exception ex) {
            log.record(new AdapterLogEntry(AdapterLogEntry.Severity.WARN).withDescription("Failed ingesting episode: " + episode.getCanonicalUri()).withSource(getClass()));
            throw ex;
        }
    }
    
    private Series fillSeries(Series series, ObjectMapper mapper, JsonNode node) throws Exception {
        JsonNode pid = node.get("pid");
        series.setCanonicalUri(getItemsUri(pid.asText()));
        series.setPublisher(Publisher.THESPACE);
        
        JsonNode title = node.get("title");
        series.setTitle(title.asText());
        
        JsonNode episodes = node.get("expected_child_count");
        if (episodes != null && !episodes.isNull()) {
            series.setTotalEpisodes(episodes.asInt());
        }
        
        fillSynopsis(node, series);
        
        fillImage(node, series);
        
        fillGenres(node, series);
        
        fillClips(node, mapper, series);
        
        return series;
    }
    
    private Clip getClip(JsonNode node) throws Exception {
        Clip clip = new Clip();
        
        JsonNode pid = node.get("pid");
        clip.setCanonicalUri(getItemsUri(pid.asText()));
        clip.setPublisher(Publisher.THESPACE);
        
        JsonNode title = node.get("title");
        clip.setTitle(title.asText());
        
        fillSynopsis(node, clip);
        
        fillImage(node, clip);
        
        fillVersions(node, clip);
        
        return clip;
    }
    
    private Version getVersion(JsonNode node, Content parent) {
        Version version = new Version();
        
        JsonNode pid = node.get("pid");
        version.setCanonicalUri(getItemsUri(pid.asText()));
        
        JsonNode duration = node.get("duration");
        if (duration != null && !duration.isNull()) {
            version.setDuration(Duration.standardSeconds(Integer.parseInt(duration.asText())));
        }
        
        Encoding encoding = new Encoding();
        Location location = new Location();
        Policy policy = new Policy();
        encoding.setCanonicalUri(parent.getCanonicalUri());
        encoding.addAvailableAt(location);
        location.setCanonicalUri(parent.getCanonicalUri());
        location.setUri(parent.getCanonicalUri());
        location.setAvailable(true);
        location.setTransportType(TransportType.LINK);
        location.setPolicy(policy);
        policy.setCanonicalUri(parent.getCanonicalUri());
        policy.setRevenueContract(Policy.RevenueContract.FREE_TO_VIEW);
        policy.setAvailableCountries(ImmutableSet.of(Countries.ALL));
        JsonNode start = node.get("start_of_media_availability");
        if (start != null && !start.isNull()) {
            policy.setAvailabilityStart(ISODateTimeFormat.dateTimeParser().parseDateTime(start.asText()));
        }
        JsonNode end = node.get("end_of_media_availability");
        if (end != null && !end.isNull()) {
            policy.setAvailabilityEnd(ISODateTimeFormat.dateTimeParser().parseDateTime(end.asText()));
        }
        version.addManifestedAs(encoding);
        
        return version;
    }
    
    private String getImagesUri(String image) {
        return BASE_CANONICAL_URI + image;
    }
    
    private String getItemsUri(String pid) {
        return BASE_ITEMS_URI + pid;
    }
}
