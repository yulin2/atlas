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

package org.uriplay.remotesite.youtube;

import java.util.Set;

import org.joda.time.Duration;
import org.uriplay.beans.BeanGraphExtractor;
import org.uriplay.beans.Representation;
import org.uriplay.media.TransportSubType;
import org.uriplay.media.TransportType;
import org.uriplay.media.entity.Encoding;
import org.uriplay.media.entity.Item;
import org.uriplay.media.entity.Location;
import org.uriplay.media.entity.Version;
import org.uriplay.media.reference.entity.ContainerFormat;
import org.uriplay.media.reference.entity.MimeType;
import org.uriplay.remotesite.ContentExtractor;
import org.uriplay.remotesite.youtube.YouTubeSource.Video;

import com.google.common.collect.Sets;

/**
 * {@link BeanGraphExtractor} that processes the result of a query to the YouTube
 * GData API, and constructs a {@link Representation}, which may later be handled
 * by the {@link BeanGraphFactory}.
 *
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class YouTubeGraphExtractor implements ContentExtractor<YouTubeSource, Item> {
	
	private static final String YOUTUBE_PUBLISHER = "youtube.com";
	

	public YouTubeGraphExtractor() {
	}

	@Override
	public Item extract(YouTubeSource source) {
		
		Set<Encoding> encodings = Sets.newHashSet();
		for (Video video : source.getVideos()) {
			Encoding encoding = extractEncodingPropertyValuesFrom(video);
			if (encoding == null) {
				continue;
			}
			Location location = extractLocationPropertyValuesFrom(video);
			encoding.addAvailableAt(location);
			encodings.add(encoding);
		}
		
		encodings.add(encodingForWebPage(source));
		
		
		Version version = new Version();
		
		version.setManifestedAs(encodings);
		
		if (source.getVideos().size() > 0) {
			version.setDuration(source.getVideos().get(0).getDuration());
		}
		
		Item item = item(source);
		item.addVersion(version);
		
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
		Item item = new Item(source.getUri(), YoutubeUriCanonicaliser.curieFor(source.getUri()));

		item.setTitle(source.getVideoTitle());
		item.setDescription(source.getDescription());
		
		item.setGenres(new YouTubeGenreMap().map(source.getCategories()));
		item.setTags(source.getTags());
		
		item.setPublisher(YOUTUBE_PUBLISHER);
		item.setThumbnail(source.getThumbnailImageUri());
		item.setImage(source.getImageUri());
		if (source.getVideos().size() > 0) {
			item.setIsLongForm((source.getVideos().get(0).getDuration()).isLongerThan(Duration.standardMinutes(15)));
		}
		return item;
	}

	private Encoding extractEncodingPropertyValuesFrom(Video video) {
		MimeType containerFormat = ContainerFormat.fromAltName(video.getType());
		if (containerFormat == null) {
			return null;
		}
		Encoding encoding = new Encoding();
		encoding.setDataContainerFormat(containerFormat);
		
		switch (video.getYoutubeFormat()) {
			case 1 :
			case 6 : {
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
	
	private Location extractLocationPropertyValuesFrom(Video video) {

		Location location = new Location();
		
		location.setUri(video.getUrl());

		if (video.isEmbeddable()) {
			switch (video.getYoutubeFormat()) {
				case 1:
				case 6: {
					location.setTransportType(TransportType.STREAM);
					location.setTransportSubType(TransportSubType.RTSP);
					break;
				}
				case 5: {
					location.setTransportType(TransportType.EMBED);
					location.setEmbedCode(embedCodeFor(video.getUrl()));
					break;
				}
				default: {
					location.setTransportType(TransportType.LINK);
				}
			}
		}
		
		return location;
	}

	private String embedCodeFor(String url) {
		String embedCode = "<object width=\"560\" height=\"340\">" +
				"<param name=\"movie\" value=\"%s&hl=en&fs=1\"></param>" +
				"<param name=\"allowFullScreen\" value=\"true\"></param>" +
				"<param name=\"allowscriptaccess\" value=\"always\"></param>" +
				"<embed src=\"%s&hl=en&fs=1\" type=\"application/x-shockwave-flash\" allowscriptaccess=\"always\" allowfullscreen=\"true\" width=\"560\" height=\"340\"></embed>" +
				"</object>";
		return String.format(embedCode, url, url);
	}
}
