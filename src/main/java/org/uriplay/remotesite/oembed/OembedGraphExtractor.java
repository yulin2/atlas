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

package org.uriplay.remotesite.oembed;

import org.jherd.beans.BeanGraphExtractor;
import org.jherd.beans.DescriptionMode;
import org.jherd.beans.Representation;
import org.jherd.beans.id.IdGenerator;
import org.jherd.beans.id.IdGeneratorFactory;
import org.springframework.beans.MutablePropertyValues;
import org.uriplay.feeds.OembedItem;
import org.uriplay.media.TransportType;
import org.uriplay.media.entity.Encoding;
import org.uriplay.media.entity.Episode;
import org.uriplay.media.entity.Location;
import org.uriplay.media.entity.Version;

import com.google.common.collect.Sets;

/**
 * {@link BeanGraphExtractor} pull details from oEmbed data.
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class OembedGraphExtractor implements BeanGraphExtractor<OembedSource> {

	private final IdGeneratorFactory idGeneratorFactory;

	public OembedGraphExtractor(IdGeneratorFactory idGeneratorFactory) {
		this.idGeneratorFactory = idGeneratorFactory;
	}

	public Representation extractFrom(OembedSource source) {
		
		IdGenerator idGenerator = idGeneratorFactory.create();
		
		Representation representation = new Representation();
		
		String episodeUri = source.getUri();
		OembedItem oembed = source.getOembed();
		
		String versionId = idGenerator.getNextId();
		String encodingId = idGenerator.getNextId();
		String locationId = idGenerator.getNextId();
		
		representation.addType(episodeUri, Episode.class);
		representation.addUri(episodeUri);
		
		representation.addType(versionId, Version.class);
		representation.addAnonymous(versionId);
		
		addItemPropertiesTo(representation, episodeUri, versionId, oembed);
		
		representation.addType(encodingId, Encoding.class);
		representation.addAnonymous(encodingId);
		
		addVersionPropertiesTo(representation, versionId, encodingId);
		
		representation.addType(locationId, Location.class);
		representation.addAnonymous(locationId);

		addEncodingPropertiesTo(representation, encodingId, locationId, oembed);
		
		addLocationPropertiesTo(representation, locationId, oembed);
		
		return representation;
	}

	private void addItemPropertiesTo(Representation representation, String itemUri, String versionId, OembedItem oembed) {
		MutablePropertyValues mpvs = new MutablePropertyValues();
		mpvs.addPropertyValue("title", oembed.title());
		mpvs.addPropertyValue("publisher", oembed.providerUrl());
		mpvs.addPropertyValue("thumbnail", oembed.thumbnailUrl());
		mpvs.addPropertyValue("versions", Sets.newHashSet(versionId));
		addCurieFor(itemUri, mpvs);
		representation.addValues(itemUri, mpvs);
	}

	private void addCurieFor(String itemUri, MutablePropertyValues mpvs) {
		String curie = curieFor(itemUri);
		if (curie != null) {
			mpvs.addPropertyValue("curie", curie);
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

	private void addVersionPropertiesTo(Representation representation, String versionId, String encodingId) {
		MutablePropertyValues mpvs = new MutablePropertyValues();
		mpvs.addPropertyValue("manifestedAs", Sets.newHashSet(encodingId));
		representation.addValues(versionId, mpvs);
	}

	private void addEncodingPropertiesTo(Representation representation, String encodingId, String locationId, OembedItem oembed) {
		MutablePropertyValues mpvs = new MutablePropertyValues();
		mpvs.addPropertyValue("availableAt", Sets.newHashSet(locationId));
		mpvs.addPropertyValue("videoHorizontalSize", oembed.width());
		mpvs.addPropertyValue("videoVerticalSize", oembed.height());
		String dataContainerFormat = getDataContainerFormat();
		if (dataContainerFormat != null) {
			mpvs.addPropertyValue("dataContainerFormat", dataContainerFormat);
		}
		representation.addValues(encodingId, mpvs);
	}
	
	private void addLocationPropertiesTo(Representation representation, String locationId, OembedItem oembed) {
		MutablePropertyValues mpvs = new MutablePropertyValues();
		mpvs.addPropertyValue("transportType", TransportType.EMBEDOBJECT.toString().toLowerCase());
		mpvs.addPropertyValue("transportSubType", "html");
		mpvs.addPropertyValue("embedCode", oembed.embedCode());
		String extractedLocationUri = extractLocationUriFrom(oembed);
		if (extractedLocationUri != null) {
			mpvs.addPropertyValue("uri", extractedLocationUri);
		}
		representation.addValues(locationId, mpvs);
	}

	// override in subclasses for specific sites/oembed output formats
	protected String extractLocationUriFrom(OembedItem oembed) {
		return null;
	}

	protected String getDataContainerFormat() {
		return null;
	}


	public Representation extractFrom(OembedSource source, DescriptionMode mode) {
		// TODO Auto-generated method stub
		return null;
	}

}
