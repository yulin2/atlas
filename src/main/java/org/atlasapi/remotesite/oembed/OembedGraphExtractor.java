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

package org.atlasapi.remotesite.oembed;

import org.atlasapi.media.TransportType;
import org.atlasapi.media.content.Encoding;
import org.atlasapi.media.content.Item;
import org.atlasapi.media.content.Location;
import org.atlasapi.media.content.Version;
import org.atlasapi.output.oembed.OembedItem;
import org.atlasapi.remotesite.ContentExtractor;

import com.metabroadcast.common.media.MimeType;

/**
 * {@link BeanGraphExtractor} pull details from oEmbed data.
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class OembedGraphExtractor implements ContentExtractor<OembedSource, Item> {

	public Item extract(OembedSource source) {

		String episodeUri = source.getUri();
		OembedItem oembed = source.getOembed();

		Location location = addLocationPropertiesTo(oembed);

		Encoding encoding = addEncodingPropertiesTo(oembed);
		encoding.addAvailableAt(location);
		
		Version version = new Version();
		version.addManifestedAs(encoding);
		
		Item item = item(episodeUri, oembed);
		item.addVersion(version);
		
		return item;
	}

	private Item item(String itemUri, OembedItem oembed) {
		Item item = new Item();
		item.setCanonicalUri(itemUri);
		item.setTitle(oembed.title());
		item.setThumbnail(oembed.thumbnailUrl());
		addCurieFor(item);
		return item;
	}

	private void addCurieFor(Item item) {
		String curie = curieFor(item.getCanonicalUri());
		if (curie != null) {
			item.setCurie(curie);
		}
	}
	
	/**
	 * Override this method in subclasses to provide publisher specific curies
	 * @param itemUri
	 * @return compact form of this URI
	 */
	protected String curieFor(String itemUri) {
		return null;
	}

	private Encoding addEncodingPropertiesTo(OembedItem oembed) {
		Encoding encoding = new Encoding();
		encoding.setVideoHorizontalSize(oembed.width());
		encoding.setVideoVerticalSize(oembed.height());
		encoding.setDataContainerFormat(getDataContainerFormat());
		return encoding;
	}
	
	private Location addLocationPropertiesTo(OembedItem oembed) {
		Location location = new Location();
		location.setTransportType(TransportType.EMBED);
		location.setEmbedCode(oembed.embedCode());
		String extractedLocationUri = extractLocationUriFrom(oembed);
		if (extractedLocationUri != null) {
			location.setUri(extractedLocationUri);
		}
		return location;
	}

	// override in subclasses for specific sites/oembed output formats
	protected String extractLocationUriFrom(OembedItem oembed) {
		return null;
	}

	protected MimeType getDataContainerFormat() {
		return null;
	}
}
