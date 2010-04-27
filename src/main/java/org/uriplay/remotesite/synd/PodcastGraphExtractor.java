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

package org.uriplay.remotesite.synd;

import java.util.List;

import org.jherd.beans.DescriptionMode;
import org.jherd.beans.Representation;
import org.jherd.core.MimeType;
import org.springframework.beans.MutablePropertyValues;
import org.uriplay.logging.Log4JLogger;
import org.uriplay.logging.UriplayLogger;
import org.uriplay.media.TransportType;
import org.uriplay.media.reference.entity.AudioFormat;
import org.uriplay.media.reference.entity.ContainerFormat;
import org.uriplay.media.reference.entity.VideoFormat;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.sun.syndication.feed.synd.SyndEnclosure;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

/**
 * Base class with some functionalisty shared by different podcast graph extractors.
 * 
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public abstract class PodcastGraphExtractor {
	
	private UriplayLogger logger;
	
	public PodcastGraphExtractor() {
		logger = new Log4JLogger();
	}

	public Representation extractFrom(SyndicationSource source, DescriptionMode mode) {
		throw new UnsupportedOperationException("Not implemented");
	}
	
	protected MutablePropertyValues extractEncodingPropertyValuesFrom(String locationUri, List<SyndEnclosure> enclosures) {
		
		MutablePropertyValues mpvs = new MutablePropertyValues();
		SyndEnclosure enclosure = Iterables.getOnlyElement(enclosures);
		mpvs.addPropertyValue("availableAt", Sets.newHashSet(locationUri));
		mpvs.addPropertyValue("dataSize", enclosure.getLength() / 1024);
		MimeType audioCoding = AudioFormat.fromAltName(enclosure.getType());
		if (audioCoding == MimeType.AUDIO_MPEG) {
			mpvs.addPropertyValue("audioCoding", audioCoding.toString());
		}
		MimeType videoCoding = VideoFormat.fromAltName(enclosure.getType());
		if (videoCoding == MimeType.VIDEO_MPEG) {
			mpvs.addPropertyValue("videoCoding", videoCoding.toString());
		}
		MimeType containerFormat = ContainerFormat.fromAltName(enclosure.getType());
		if (containerFormat != null) {
			mpvs.addPropertyValue("dataContainerFormat", containerFormat.toString());
		} else {
			logger.unknownDataContainerFormat(locationUri, enclosure.getType());
		}
		
		return mpvs;
	}
	
	protected MutablePropertyValues extractEpisodePropertyValuesFrom(SyndEntry entry, String versionUri, String feedUri) {
		MutablePropertyValues mpvs = new MutablePropertyValues();
		mpvs.addPropertyValue("title", entry.getTitle());
		mpvs.addPropertyValue("description", entry.getDescription().getValue());
		mpvs.addPropertyValue("versions", Sets.newHashSet(versionUri));
		mpvs.addPropertyValue("containedIn", Sets.newHashSet(feedUri));
		if (publisher() != null) {
			mpvs.addPropertyValue("publisher", publisher());
		}
		return mpvs;
	}
	
	protected MutablePropertyValues extractVersionPropertyValuesFrom(String encodingUri) {
		MutablePropertyValues mpvs = new MutablePropertyValues();
		mpvs.addPropertyValue("manifestedAs", Sets.newHashSet(encodingUri));
		return mpvs;
	}
	
	protected MutablePropertyValues extractLocationPropertyValuesFrom(String locationUri) {
		MutablePropertyValues mpvs = new MutablePropertyValues();
		mpvs.addPropertyValue("transportType", TransportType.DOWNLOAD.toString());
		mpvs.addPropertyValue("transportSubType", "HTTP");
		mpvs.addPropertyValue("uri", locationUri);
		return mpvs;
	}

	@SuppressWarnings("unchecked")
	protected List<SyndEnclosure> enclosuresFrom(SyndEntry entry) {
		return entry.getEnclosures();
	}
	
	@SuppressWarnings("unchecked")
	protected List<SyndEntry> entriesFrom(SyndFeed feed) {
		return feed.getEntries();
	}
	
	protected String publisher() {
		return null;
	}
}
