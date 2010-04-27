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

import org.jherd.beans.BeanGraphExtractor;
import org.jherd.beans.BeanGraphFactory;
import org.jherd.beans.DescriptionMode;
import org.jherd.beans.Representation;
import org.jherd.beans.id.IdGenerator;
import org.jherd.beans.id.IdGeneratorFactory;
import org.joda.time.DateTimeConstants;
import org.springframework.beans.MutablePropertyValues;
import org.uriplay.media.TransportType;
import org.uriplay.media.entity.Encoding;
import org.uriplay.media.entity.Item;
import org.uriplay.media.entity.Location;
import org.uriplay.media.entity.Version;
import org.uriplay.remotesite.youtube.YouTubeSource.Video;

import com.google.common.collect.Sets;

/**
 * {@link BeanGraphExtractor} that processes the result of a query to the YouTube
 * GData API, and constructs a {@link Representation}, which may later be handled
 * by the {@link BeanGraphFactory}.
 *
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class YouTubeGraphExtractor implements BeanGraphExtractor<YouTubeSource> {
	
	private static final String YOUTUBE_PUBLISHER = "youtube.com";
	
	private final IdGeneratorFactory anonIdFactory;

	public YouTubeGraphExtractor(IdGeneratorFactory anonIdGenerator) {
		this.anonIdFactory = anonIdGenerator;
	}

	public Representation extractFrom(YouTubeSource source) {
	
		IdGenerator idGenerator = anonIdFactory.create();
		
		Representation representation = new Representation();
	
		Set<String> encodingIds = Sets.newHashSet();
		
		for (Video video : source.getVideos()) {
			
			String encodingId = idGenerator.getNextId();
			String locationId = idGenerator.getNextId();

			encodingIds.add(encodingId);
			
			representation.addType(encodingId, Encoding.class);
			representation.addAnonymous(encodingId);
			representation.addValues(encodingId, extractEncodingPropertyValuesFrom(video, locationId));
			
			representation.addType(locationId, Location.class);
			representation.addAnonymous(locationId);
			representation.addValues(locationId, extractLocationPropertyValuesFrom(video));
		}
		
		addEncodingForWebPage(idGenerator, source, representation, encodingIds);
		
		String versionId = idGenerator.getNextId();
		
		representation.addUri(source.getUri());
		representation.addType(source.getUri(), Item.class);
		
        representation.addType(versionId, Version.class);
        representation.addAnonymous(versionId);
        representation.addValues(source.getUri(), extractEpisodePropertyValuesFrom(source, versionId));
        
    	if (source.getVideos().size() > 0) {
    		representation.addValues(versionId, extractVersionPropertyValuesFrom(encodingIds, source.getVideos().get(0)));
		} else {
			representation.addValues(versionId, extractVersionPropertyValuesFrom(encodingIds, null));
		}
		return representation;
	}

	private void addEncodingForWebPage(IdGenerator idGenerator, YouTubeSource source, Representation representation, Set<String> encodingIds) {
	
		String encodingId = idGenerator.getNextId();
		String locationId = idGenerator.getNextId();
		encodingIds.add(encodingId);
		
		representation.addType(encodingId, Encoding.class);
		representation.addAnonymous(encodingId);
		representation.addType(locationId, Location.class);
		representation.addAnonymous(locationId);
		
		MutablePropertyValues mpvs = new MutablePropertyValues();
		mpvs.addPropertyValue("transportType", TransportType.HTMLEMBED.toString());
		mpvs.addPropertyValue("uri", source.getUri());

		representation.addValues(locationId, mpvs);
		
		mpvs = new MutablePropertyValues();
		mpvs.addPropertyValue("availableAt", Sets.newHashSet(locationId));
		representation.addValues(encodingId, mpvs);
	}
	
	private MutablePropertyValues extractEpisodePropertyValuesFrom(YouTubeSource source, String versionId) {
		MutablePropertyValues mpvs = new MutablePropertyValues();
		mpvs.addPropertyValue("title", source.getVideoTitle());
		mpvs.addPropertyValue("description", source.getDescription());
		mpvs.addPropertyValue("versions", Sets.newHashSet(versionId));
		mpvs.addPropertyValue("genres", new YouTubeGenreMap().map(source.getCategories()));
		mpvs.addPropertyValue("tags", source.getTags());
		mpvs.addPropertyValue("publisher", YOUTUBE_PUBLISHER);
		mpvs.addPropertyValue("thumbnail", source.getThumbnailImageUri());
		mpvs.addPropertyValue("image", source.getImageUri());
		mpvs.addPropertyValue("curie", YoutubeUriCanonicaliser.curieFor(source.getUri()));
		if (source.getVideos().size() > 0) {
			mpvs.addPropertyValue("isLongForm", (source.getVideos().get(0).getDuration()) > (15 * DateTimeConstants.SECONDS_PER_MINUTE));
		}
		return mpvs;
	}
	
	private MutablePropertyValues extractVersionPropertyValuesFrom(Set<String> encodingIds, Video video) {
		MutablePropertyValues mpvs = new MutablePropertyValues();
		mpvs.addPropertyValue("manifestedAs", encodingIds);
		if (video != null) {
			mpvs.addPropertyValue("duration", video.getDuration());
		}
		return mpvs;
	}

	private MutablePropertyValues extractEncodingPropertyValuesFrom(Video video, String locationId) {
	
		MutablePropertyValues mpvs = new MutablePropertyValues();
		mpvs.addPropertyValue("availableAt", Sets.newHashSet(locationId));
		mpvs.addPropertyValue("dataContainerFormat", video.getType());
		
		switch (video.getYoutubeFormat()) {
			case 1 :
			case 6 : {
				mpvs.addPropertyValue("videoHorizontalSize", 176);
				mpvs.addPropertyValue("videoVerticalSize", 144);
				if (video.getYoutubeFormat() == 1) {
					mpvs.addPropertyValue("audioCoding", "audio/AMR");
				} else {
					mpvs.addPropertyValue("audioCoding", "audio/mp4");
				}
				mpvs.addPropertyValue("audioChannels", 1);
				mpvs.addPropertyValue("videoCoding", "video/H263");
				mpvs.addPropertyValue("hasDOG", false);
				break;
			}
			case 5: {
				mpvs.addPropertyValue("hasDOG", true);
				break;
			}
		}
		
		return mpvs;
	}
	
	private MutablePropertyValues extractLocationPropertyValuesFrom(Video video) {

		MutablePropertyValues mpvs = new MutablePropertyValues();

		mpvs.addPropertyValue("uri", video.getUrl());

		if (video.isEmbeddable()) {
			switch (video.getYoutubeFormat()) {
				case 1:
				case 6: {
					mpvs.addPropertyValue("transportType", "stream");
					mpvs.addPropertyValue("transportSubType", "rtsp");
					break;
				}
				case 5: {
					mpvs.addPropertyValue("transportType", TransportType.EMBEDOBJECT.toString());
					mpvs.addPropertyValue("transportSubType", "html");
					mpvs.addPropertyValue("embedCode", embedCodeFor(video.getUrl()));
					break;
				}
				default: {
					mpvs.addPropertyValue("transportType", TransportType.HTMLEMBED);
				}
			}
		}
		
		return mpvs;
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

	public Representation extractFrom(YouTubeSource source, DescriptionMode mode) {
		// TODO Auto-generated method stub
		return null;
	}

}
