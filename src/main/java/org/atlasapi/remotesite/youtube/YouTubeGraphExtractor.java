/* Copyright 2009 British Broadcasting Corporation
   Copyright 2009 Meta Broadcast Ltd

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License. */

package org.atlasapi.remotesite.youtube;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.atlasapi.media.TransportSubType;
import org.atlasapi.media.TransportType;
import org.atlasapi.media.entity.Alias;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.Policy;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Version;
import org.atlasapi.media.reference.entity.ContainerFormat;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.youtube.entity.YouTubeSource;
import org.atlasapi.remotesite.youtube.entity.YouTubeSource.Video;
import org.joda.time.DateTimeFieldType;
import org.joda.time.Duration;

import com.google.common.collect.Sets;
import com.metabroadcast.common.media.MimeType;

/**
 * {@link ContentExtractor} that processes the result of a query to the YouTube
 * GData API.<br>
 * Each YouTube video is formed of an Item, which has Versions, Versions have
 * Broadcast and Encodings, while Encodings have Locations and Policies.
 * 
 * @author Robert Chatley (robert@metabroadcast.com)
 * @author Augusto Uehara (augusto@metabroadcast.com)
 */
public class YouTubeGraphExtractor implements ContentExtractor<YouTubeSource, Item> {
    static enum EMBED_TYPE {
        IFRAME, IFRAME_WITH_API, AS3_OBJECT
    };

    @Override
    public Item extract(YouTubeSource source) {
        if (source == null) {
            return null;
        }

        Set<Encoding> encodings = Sets.newHashSet();
        ArrayList<Alias> aliases = new ArrayList<Alias>();
        ArrayList<String> aliasesUrl = new ArrayList<String>();

        for (Video video : source.getVideos()) {
            Encoding encoding = extractEncodingPropertyValuesFrom(video);
            if (encoding == null) {
                continue;
            }
            Set<Location> locations = extractLocationPropertyValuesFrom(source);
            encoding.setAvailableAt(locations);

            encodings.add(encoding);

            Alias alias = new Alias("zz:youtube:id",
                    YoutubeUriCanonicaliser.videoIdFrom(video.getUrl()));
            aliases.add(alias);
            aliasesUrl.add("zz:youtube:id=" + alias.getValue());
            
        }
        encodings.add(encodingForWebPage(source));

        Version version = new Version();

        version.setManifestedAs(encodings);

        if (source.getVideos().size() > 0) {
            version.setDuration(source.getVideos().get(0).getDuration());
        }

        Item item = item(source);
        item.addVersion(version);

        if (source.getDefaultPlayerUrl() != null) {
            String defaultPlayerNamespace = "";
            Alias defaultPlayer = new Alias(defaultPlayerNamespace,
                    source.getDefaultPlayerUrl());
            aliases.add(defaultPlayer);
            aliasesUrl.add(defaultPlayer.getValue());
        }

        item.addAliases(aliases);
        item.addAliasUrls(aliasesUrl);
        if (source.getRecorded() != null)
            item.setYear(new Integer(source.getRecorded().get(
                    DateTimeFieldType.year())));

        item.setGenres(source.getGenres());

        return item;
    }

    private Encoding encodingForWebPage(YouTubeSource source) {
        Location location = new Location();
        location.setTransportType(TransportType.LINK);
        location.setUri(source.getUri());

        Encoding encoding = new Encoding();
        encoding.addAvailableAt(location);

        return encoding;
    }

    private Item item(YouTubeSource source) {
        Item item = new Item(source.getUri(),
                YoutubeUriCanonicaliser.curieFor(source.getUri()),
                Publisher.YOUTUBE);

        item.setTitle(source.getVideoTitle());
        item.setDescription(source.getDescription());

        item.setThumbnail(source.getThumbnailImageUri());
        item.setImage(source.getImageUri());
        if (source.getVideos().size() > 0) {
            item.setIsLongForm((source.getVideos().get(0).getDuration())
                    .isLongerThan(Duration.standardMinutes(15)));
        }
        item.setMediaType(MediaType.VIDEO);

        item.setGenres(source.getCategories());
        return item;
    }

    public Encoding extractEncodingPropertyValuesFrom(Video video) {
        MimeType containerFormat = ContainerFormat.fromAltName(video.getType());
        if (containerFormat == null) {
            return null;
        }
        Encoding encoding = new Encoding();
        encoding.setDataContainerFormat(containerFormat);

        switch (video.getYoutubeFormat()) {
        case 1:
        case 6: {
            encoding.setVideoHorizontalSize(176);
            encoding.setVideoVerticalSize(144);
            if (video.getYoutubeFormat() == 1) {
                encoding.setAudioCoding(MimeType.AUDIO_AMR);
            } else {
                encoding.setAudioCoding(MimeType.AUDIO_MP4);
            }
            encoding.setAudioChannels(1);
            encoding.setVideoCoding(MimeType.VIDEO_H263);
            encoding.setHasDOG(false);
            break;
        }
        case 5: {
            encoding.setHasDOG(true);
            break;
        }
        }
        return encoding;
    }

    private Set<Location> extractLocationPropertyValuesFrom(YouTubeSource source) {
        Set<Location> locations = new HashSet<Location>();

        Policy policy = new Policy();
        policy.setAvailabilityStart(source.getUploaded());

        Location location1 = new Location();

        location1.setPolicy(policy);

        if (source.getFive() != null) {
            Location location5 = location1.copy();
            location5.setTransportType(TransportType.EMBED);
            location5.setEmbedCode(embedCodeFor(source.getURL()));
            location5.setUri(source.getFive());
            location5.setEmbedCode(embedCodeFor(source.getFive()));
            locations.add(location5);
        }
        if (source.getSix() != null) {
            Location location6 = location1.copy();
            location6.setTransportType(TransportType.STREAM);
            location6.setTransportSubType(TransportSubType.RTSP);
            location6.setUri(source.getSix());
            locations.add(location6);
        }

        if (source.getDefaultPlayerUrl() != null && !source.getDefaultPlayerUrl().equals("")) {
            Location locationDefault = location1.copy();
            locationDefault.setTransportType(TransportType.LINK);
            locationDefault.setUri(source.getDefaultPlayerUrl());
            locations.add(locationDefault);
        }

        if (source.getMobilePlayerUrl() != null && !source.getMobilePlayerUrl().equals("")) {
            Location locationMobile = location1.copy();
            locationMobile.setTransportType(TransportType.LINK);
            locationMobile.setUri(source.getMobilePlayerUrl());
            locations.add(locationMobile);
        }

        return locations;
    }

    /**
     * Returns embed code for default type (iframe);
     * 
     * @param url
     * @return
     */
    public String embedCodeFor(String url) {
        String videoId = YoutubeUriCanonicaliser.videoIdFrom(url);
        return embedCodeFor(videoId, EMBED_TYPE.IFRAME);
    }

    /**
     * Returns embed code for the specified YouTube embed type
     * 
     * @param videoId
     *            YouTube video id.
     * @param embedType
     * @return
     */
    public String embedCodeFor(String videoId, EMBED_TYPE embedType) {
        String embedCode = null;
        switch (embedType) {
        case IFRAME_WITH_API:
            embedCode = "        <div id=\"ytplayer\"></div>\n\n"
                    + "<script>\n"
                    + " var tag = document.createElement('script');\n"
                    + " tag.src = \"https://www.youtube.com/player_api\";\n"
                    + " var firstScriptTag = document.getElementsByTagName('script')[0];\n"
                    + " firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);\n\n"
                    + " var player;\n\n"
                    + " function onYouTubePlayerAPIReady() {\n"
                    + " player = new YT.Player('ytplayer', {\n"
                    + " height: '390',\n width: '640',\n videoId: '" + videoId
                    + "'\n });\n}\n </script>";
            break;
        case AS3_OBJECT:
            embedCode = "<object width=\"640\" height=\"390\">\n"
                    + " <param name=\"movie\" \n"
                    + "value=\"https://www.youtube.com/v/"
                    + videoId
                    + "?version=3&autoplay=1\"></param>\n"
                    + "<param name=\"allowScriptAccess\" value=\"always\"></param>\n"
                    + "<embed src=\"https://www.youtube.com/v/" + videoId
                    + "?version=3&autoplay=1\"\n"
                    + "type=\"application/x-shockwave-flash\"\n"
                    + "allowscriptaccess=\"always\"\n"
                    + "width=\"640\" height=\"390\"></embed>\n" + "</object>\n";
            break;
        case IFRAME:
            embedCode = " <iframe id\"ytplayer\" type=\"text/html\" width=\"640\" height=\"390\" \n"
                    + "src=\"http://www.youtube.com/embed/"
                    + videoId
                    + "?autoplay=1&origin=http://example.com\" \n"
                    + "frameborder=\"0\"/>";
            break;

        }

        return embedCode;
    }
};
