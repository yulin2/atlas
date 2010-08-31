package org.atlasapi.remotesite.tvblob;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.DefinitiveContentWriter;
import org.atlasapi.remotesite.FetchException;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.joda.time.DateTime;

import com.metabroadcast.common.time.DateTimeZones;

public class TVBlobDayPopulator {

    private static final String BASE_URL = "http://tvblob.com/";
    private static final String BASE_CHANNEL_URL = BASE_URL + "channel/";
    private static final String BASE_CURIE = "tvblob:";

    private final String channelSlug;
    private final String channel;
    private final JsonFactory jsonF = new JsonFactory();

    static final Log LOG = LogFactory.getLog(TVBlobDayPopulator.class);
    private final DefinitiveContentWriter contentStore;
    private final ContentResolver contentResolver;

    public TVBlobDayPopulator(DefinitiveContentWriter contentStore, ContentResolver contentResolver, String channelSlug) {
        this.contentStore = contentStore;
        this.contentResolver = contentResolver;
        this.channelSlug = channelSlug;
        this.channel = BASE_CHANNEL_URL + channelSlug;
    }

    public void populate(InputStream source) {
        JsonParser jp = null;

        try {
            jp = jsonF.createJsonParser(source);

            if (jp.nextToken() != JsonToken.START_OBJECT) {
                throw new FetchException("Expected tvblob data to start with an Object");
            }

            while (jp.nextToken() != JsonToken.END_OBJECT) {

                if ("broadcasts".equals(jp.getCurrentName()) && jp.nextToken() == JsonToken.START_ARRAY) {
                    while (jp.nextToken() != JsonToken.END_ARRAY) {

                        Episode episode = null;
                        DateTime start = null;
                        DateTime end = null;
                        Brand brand = null;

                        while (jp.nextToken() != JsonToken.END_OBJECT) {
                            String fieldName = jp.getCurrentName();
                            jp.nextToken();

                            if ("end".equals(fieldName)) {
                                end = new DateTime(jp.getText(), DateTimeZones.ROME);
                            } else if ("start".equals(fieldName)) {
                                start = new DateTime(jp.getText(), DateTimeZones.ROME);
                            } else if ("serie".equals(fieldName)) {
                                brand = getBrand(jp, channelSlug);
                            } else if ("episode".equals(fieldName)) {
                                episode = getEpisode(jp, channelSlug);
                            }
                        }

                        if (start != null && end != null && episode != null) {
                            Broadcast broadcast = new Broadcast(channel, start.toDateTime(DateTimeZones.UTC), end
                                            .toDateTime(DateTimeZones.UTC));
                            broadcast.setLastUpdated(new DateTime(DateTimeZones.UTC));
                            
                            Content currentContent = contentResolver.findByUri(episode.getCanonicalUri());
                            if (currentContent != null && currentContent instanceof Episode) {
                                episode = (Episode) currentContent;
                            }

                            if (brand != null) {
                                episode.setBrand(brand);
                            }

                            Version version = episode.getVersions().iterator().next();
                            version.addBroadcast(broadcast);
                            
                            contentStore.createOrUpdateDefinitiveItem(episode);
                        }
                    }

                }
            }
        } catch (Exception e) {
            LOG.warn("Problem while decoding tvblob json", e);
        } finally {
            if (jp != null) {
                try {
                    jp.close();
                } catch (IOException e) {
                    LOG.warn("Unable to close JsonParser", e);
                }
            }
        }
    }

    private Brand getBrand(JsonParser jp, String channelSlug) {
        try {

            if (jp.getCurrentToken() == JsonToken.START_OBJECT) {
                String title = null;
                Integer pid = null;

                while (jp.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = jp.getCurrentName();
                    jp.nextToken();

                    if ("pid".equals(fieldName)) {
                        pid = jp.getIntValue();
                    } else if ("title".equals(fieldName)) {
                        title = jp.getText();
                    }
                }

                if (pid != null) {
                    Brand brand = new Brand(BASE_URL + "brand/" + channelSlug + "/" + pid, BASE_CURIE + "brand_"
                                    + channelSlug + "_" + pid, Publisher.TVBLOB);
                    brand.setTitle(title);
                    brand.setLastUpdated(new DateTime(DateTimeZones.UTC));

                    return brand;
                }
            }
        } catch (Exception e) {
            LOG.warn("Problem creating brand for tvblob broadcast", e);
        }
        return null;
    }

    private Episode getEpisode(JsonParser jp, String channelSlug) {
        try {
            if (jp.getCurrentToken() == JsonToken.START_OBJECT) {
                String title = null;
                String subTitle = null;
                String shortSynopsis = null;
                Integer pid = null;

                while (jp.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = jp.getCurrentName();
                    jp.nextToken();

                    if ("pid".equals(fieldName)) {
                        pid = jp.getIntValue();
                    } else if ("title".equals(fieldName)) {
                        title = jp.getText();
                    } else if ("subtitle".equals(fieldName)) {
                        subTitle = jp.getText();
                    } else if ("short_synopsis".equals(fieldName)) {
                        shortSynopsis = jp.getText();
                    }
                }

                if (pid != null) {
                    Episode episode = new Episode(BASE_URL + "episode/" + channelSlug + "/" + pid, BASE_CURIE
                                    + "episode_" + channelSlug + "_" + pid, Publisher.TVBLOB);
                    episode.setDescription(shortSynopsis);
                    episode.setTitle(title(title, subTitle));
                    episode.setLastUpdated(new DateTime(DateTimeZones.UTC));
                    Version version = new Version();
                    episode.addVersion(version);
                    return episode;
                }
            }
        } catch (Exception e) {
            LOG.warn("Problem creating episode for tvblob broadcast", e);
        }
        return null;
    }

    private String title(String title, String subTitle) {
        if (title != null) {
            if (subTitle != null) {
                title = title + " - " + subTitle;
            }
            return title;
        }
        return subTitle;
    }
}
