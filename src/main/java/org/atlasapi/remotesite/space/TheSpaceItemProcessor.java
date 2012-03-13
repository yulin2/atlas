package org.atlasapi.remotesite.space;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.http.IdentityHttpResponseTransformer;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.http.SimpleHttpRequest;
import java.io.InputStream;
import java.util.Iterator;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.ParentRef;
import org.atlasapi.media.entity.Series;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.Duration;

/**
 */
public class TheSpaceItemProcessor {

    private final String BASE_CANONICAL_URI = "http://thespace.org/items/";
    private final String SERIES_TYPE = "series";
    private final String EPISODE_TYPE = "episode";
    //
    private final SimpleHttpClient client;
    private final AdapterLog log;
    private final ContentResolver contentResolver;
    private final ContentWriter contentWriter;

    public TheSpaceItemProcessor(SimpleHttpClient client, AdapterLog log, ContentResolver contentResolver, ContentWriter contentWriter) {
        this.client = client;
        this.log = log;
        this.contentResolver = contentResolver;
        this.contentWriter = contentWriter;
    }

    public void process(InputStream content) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        JsonNode item = mapper.readTree(content);
        String type = item.get("type").asText();
        String pid = item.get("pid").asText();

        if (type.equals(SERIES_TYPE)) {
            Series series = (Series) contentResolver.findByCanonicalUris(ImmutableSet.of(getCanonicalUri(pid))).getFirstValue().valueOrNull();
            if (series == null) {
                series = new Series();
            }
            makeSeries(series, item, mapper);
        } else if (type.equals(EPISODE_TYPE)) {
            Episode episode = (Episode) contentResolver.findByCanonicalUris(ImmutableSet.of(getCanonicalUri(pid))).getFirstValue().valueOrNull();
            if (episode == null) {
                episode = new Episode();
            }
            makeEpisode(episode, item, mapper);
        } else {
            throw new IllegalArgumentException("No suitable type: " + type);
        }
    }

    private void makeSeries(Series series, JsonNode node, ObjectMapper mapper) throws Exception {
        JsonNode pid = node.get("pid");
        series.setCanonicalUri(getCanonicalUri(pid.asText()));

        JsonNode title = node.get("title");
        series.setTitle(title.asText());

        JsonNode episodes = node.get("expected_child_count");
        if (episodes != null) {
            series.setTotalEpisodes(episodes.asInt());
        }

        JsonNode long_synopsis = node.get("long_synopsis");
        JsonNode medium_synopsis = node.get("medium_synopsis");
        JsonNode short_synopsis = node.get("short_synopsis");
        String synopsis = null;
        if (long_synopsis != null) {
            synopsis = long_synopsis.asText();
        } else if (medium_synopsis != null) {
            synopsis = medium_synopsis.asText();
        } else if (short_synopsis != null) {
            synopsis = short_synopsis.asText();
        }
        series.setDescription(synopsis);

        JsonNode image = node.get("image");
        if (image != null) {
            JsonNode smallImage = image.get("image.depiction320");
            if (smallImage != null) {
                series.setThumbnail(smallImage.asText());
            }
            JsonNode bigImage = image.get("image.depiction640");
            if (bigImage != null) {
                series.setImage(bigImage.asText());
            }
        }
        contentWriter.createOrUpdate(series);
    }

    private void makeEpisode(Episode episode, JsonNode node, ObjectMapper mapper) throws Exception {
        JsonNode pid = node.get("pid");
        episode.setCanonicalUri(getCanonicalUri(pid.asText()));

        JsonNode title = node.get("title");
        episode.setTitle(title.asText());

        JsonNode position = node.get("position");
        if (position != null) {
            episode.setEpisodeNumber(position.asInt());
        }

        JsonNode long_synopsis = node.get("long_synopsis");
        JsonNode medium_synopsis = node.get("medium_synopsis");
        JsonNode short_synopsis = node.get("short_synopsis");
        String synopsis = null;
        if (long_synopsis != null) {
            synopsis = long_synopsis.asText();
        } else if (medium_synopsis != null) {
            synopsis = medium_synopsis.asText();
        } else if (short_synopsis != null) {
            synopsis = short_synopsis.asText();
        }
        episode.setDescription(synopsis);

        JsonNode image = node.get("image");
        if (image != null) {
            JsonNode smallImage = image.get("image.depiction320");
            if (smallImage != null) {
                episode.setThumbnail(smallImage.asText());
            }
            JsonNode bigImage = image.get("image.depiction640");
            if (bigImage != null) {
                episode.setImage(bigImage.asText());
            }
        }

        Iterator<JsonNode> versions = node.get("versions").getElements();
        while (versions.hasNext()) {
            String vPid = versions.next().get("pid").asText();
            InputStream vStream = client.get(new SimpleHttpRequest<InputStream>(TheSpaceUpdater.BASE_API_URL + "/items/" + vPid + ".json", new IdentityHttpResponseTransformer()));
            JsonNode version = mapper.readTree(vStream);
            episode.addVersion(getVersion(version.get("version")));
        }

        JsonNode parent = node.get("parent");
        if (parent != null) {
            String pPid = parent.get("pid").asText();
            Series pSeries = (Series) contentResolver.findByCanonicalUris(ImmutableSet.of(getCanonicalUri(pPid))).getFirstValue().valueOrNull();
            if (pSeries == null) {
                InputStream pStream = client.get(new SimpleHttpRequest<InputStream>(TheSpaceUpdater.BASE_API_URL + "/items/" + pPid + ".json", new IdentityHttpResponseTransformer()));
                pSeries = new Series();
                pSeries.setChildRefs(ImmutableList.of(episode.childRef()));
                makeSeries(pSeries, mapper.readTree(pStream), mapper);
            } else {
                pSeries.setChildRefs(Iterables.concat(pSeries.getChildRefs(), ImmutableList.of(episode.childRef())));
            }
            episode.setParentRef(ParentRef.parentRefFrom(pSeries));
            contentWriter.createOrUpdate(pSeries);
        }
        contentWriter.createOrUpdate(episode);
    }

    private Version getVersion(JsonNode node) {
        Version version = new Version();

        JsonNode pid = node.get("pid");
        version.setCanonicalUri(getCanonicalUri(pid.asText()));

        JsonNode duration = node.get("duration");
        if (duration != null) {
            version.setDuration(Duration.standardSeconds(Integer.parseInt(duration.asText())));
        }

        return version;
    }

    private String getCanonicalUri(String pid) {
        return BASE_CANONICAL_URI + pid;
    }
}
