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

package org.atlasapi.remotesite.synd;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atlasapi.media.TransportSubType;
import org.atlasapi.media.TransportType;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.reference.entity.AudioFormat;
import org.atlasapi.media.reference.entity.ContainerFormat;
import org.atlasapi.media.reference.entity.VideoFormat;

import com.google.common.collect.Iterables;
import com.metabroadcast.common.media.MimeType;
import com.sun.syndication.feed.synd.SyndEnclosure;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

/**
 * Base class with some functionalisty shared by different podcast graph extractors.
 * 
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public abstract class PodcastGraphExtractor {
	
	private final Log log = LogFactory.getLog(getClass());
	
	protected Encoding encodingFrom(List<SyndEnclosure> enclosures) {
		Encoding encoding = new Encoding();
		
		SyndEnclosure enclosure = Iterables.getOnlyElement(enclosures);
		encoding.setDataSize(enclosure.getLength() / 1024);
		
		MimeType audioCoding = AudioFormat.fromAltName(enclosure.getType());
		if (audioCoding == MimeType.AUDIO_MPEG) {
			encoding.setAudioCoding(audioCoding);
		}
		MimeType videoCoding = VideoFormat.fromAltName(enclosure.getType());
		if (videoCoding == MimeType.VIDEO_MPEG) {
			encoding.setVideoCoding(videoCoding);
		}
		MimeType containerFormat = ContainerFormat.fromAltName(enclosure.getType());
		if (containerFormat != null) {
			encoding.setDataContainerFormat(containerFormat);
		} else {
			log.warn("unknownDataContainerFormat " + enclosure.getUrl() + " : " + enclosure.getType());
		}
		return encoding;
	}
	
	protected Item itemFrom(SyndEntry entry, String feedUri) {
		Item item = new Item();
		item.setCanonicalUri(itemUri(entry));
		item.setTitle(entry.getTitle());
		item.setDescription(entry.getDescription().getValue());
		if (publisher() != null) {
			item.setPublisher(publisher());
		}
		return item;
	}

	protected abstract String itemUri(SyndEntry entry);

	protected Location locationFrom(String locationUri) {
		Location location = new Location();
		location.setTransportType(TransportType.DOWNLOAD);
		location.setTransportSubType(TransportSubType.HTTP);
		location.setUri(locationUri);
		return location;
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
