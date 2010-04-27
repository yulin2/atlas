/* Copyright 2009 Meta Broadcast Ltd

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License. */


package org.uriplay.remotesite.bliptv;

import java.util.Set;

import org.jherd.beans.BeanGraphExtractor;
import org.jherd.beans.DescriptionMode;
import org.jherd.beans.Representation;
import org.jherd.beans.id.IdGenerator;
import org.jherd.beans.id.IdGeneratorFactory;
import org.springframework.beans.MutablePropertyValues;
import org.uriplay.media.TransportType;
import org.uriplay.media.entity.Encoding;
import org.uriplay.media.entity.Episode;
import org.uriplay.media.entity.Location;
import org.uriplay.media.entity.Version;
import org.uriplay.query.content.PerPublisherCurieExpander;
import org.uriplay.remotesite.html.HtmlDescriptionOfItem;
import org.uriplay.remotesite.html.HtmlDescriptionSource;

import com.google.common.collect.Sets;

public class BlipTvGraphExtractor implements BeanGraphExtractor<HtmlDescriptionSource>  {

	private final IdGeneratorFactory idGeneratorFactory;

	public BlipTvGraphExtractor(IdGeneratorFactory idGen) {
		this.idGeneratorFactory = idGen;
	}

	public Representation extractFrom(HtmlDescriptionSource src) {
		String itemUri = src.getUri();
		IdGenerator idGenerator = idGeneratorFactory.create();

		Representation representation = new Representation();
		
		String versionId = idGenerator.getNextId();
		
		Set<String> encodingIds = Sets.newHashSet();
		
		addItemPropertiesTo(representation, itemUri, versionId, src.getItem());
		addVersionPropertiesTo(representation, versionId, encodingIds);
		
		for (String locationUri : src.locationUris()) {

			String encodingId = idGenerator.getNextId();
			String locationId = idGenerator.getNextId();

			encodingIds.add(encodingId);
			
			if (locationUri.endsWith(".flv")) {
				
				String locationId2 = idGenerator.getNextId();

				addFlvDownloadLocationTo(representation, locationId, locationUri);
				addEmbedCodeLocationTo(representation, locationId2, src.embedCode());
				addEncodingPropertiesTo(representation, encodingId, Sets.newHashSet(locationId, locationId2), locationUri);
			} else {
				addLocationPropertiesTo(representation, locationId, src.getItem(), locationUri);
				addEncodingPropertiesTo(representation, encodingId, Sets.newHashSet(locationId), locationUri);
			}
		}
		
		return representation;
	}
	
	private void addEmbedCodeLocationTo(Representation representation, String locationId, String embedCode) {
		representation.addType(locationId, Location.class);
		representation.addAnonymous(locationId);
		MutablePropertyValues mpvs = new MutablePropertyValues();
		mpvs.addPropertyValue("embedCode", embedCode);
		mpvs.addPropertyValue("transportType", TransportType.EMBEDOBJECT.toString().toLowerCase());
		mpvs.addPropertyValue("transportSubType", "html");
		representation.addValues(locationId, mpvs);
	}

	private void addFlvDownloadLocationTo(Representation representation, String locationId, String locationUri) {
		representation.addType(locationId, Location.class);
		representation.addAnonymous(locationId);
		MutablePropertyValues mpvs = new MutablePropertyValues();
		mpvs.addPropertyValue("uri", locationUri);
		mpvs.addPropertyValue("transportType", TransportType.DOWNLOAD.toString().toLowerCase());
		representation.addValues(locationId, mpvs);
	}

	private void addItemPropertiesTo(Representation representation, String itemUri, String versionId, HtmlDescriptionOfItem item) {
		representation.addType(itemUri, Episode.class);
		representation.addUri(itemUri);

		MutablePropertyValues mpvs = new MutablePropertyValues();
		mpvs.addPropertyValue("title", item.getTitle());
		mpvs.addPropertyValue("description", item.getDescription());
		mpvs.addPropertyValue("publisher", "blip.tv");
		mpvs.addPropertyValue("versions", Sets.newHashSet(versionId));
		mpvs.addPropertyValue("thumbnail", item.getThumbnail());
		if (itemUri.startsWith("http://blip.tv/file/")) {
			mpvs.addPropertyValue("curie", PerPublisherCurieExpander.CurieAlgorithm.BLIP.compact(itemUri));
		}
		representation.addValues(itemUri, mpvs);
	}
	
	private void addEncodingPropertiesTo(Representation representation, String encodingId, Set<String> locationIds, String locationUri) {
		
		representation.addType(encodingId, Encoding.class);
		representation.addAnonymous(encodingId);
		
		MutablePropertyValues mpvs = new MutablePropertyValues();
		mpvs.addPropertyValue("availableAt", locationIds);
		if (locationUri.endsWith(".flv")) {
			mpvs.addPropertyValue("dataContainerFormat", "video/x-flv");
		} else if (locationUri.endsWith(".mov")) {
			mpvs.addPropertyValue("dataContainerFormat", "video/quicktime");
		} else if (locationUri.endsWith(".m4v")) {
			mpvs.addPropertyValue("dataContainerFormat", "video/m4v");
		} else if (locationUri.endsWith(".mp4")) {
			mpvs.addPropertyValue("dataContainerFormat", "video/mp4");
		}
		
		representation.addValues(encodingId, mpvs);
	}

	private void addVersionPropertiesTo(Representation representation, String versionId, Set<String> encodingIds) {
	
		representation.addType(versionId, Version.class);
		representation.addAnonymous(versionId);
		MutablePropertyValues mpvs = new MutablePropertyValues();
		mpvs.addPropertyValue("manifestedAs", encodingIds);
		representation.addValues(versionId, mpvs);
	}
	
	private void addLocationPropertiesTo(Representation representation, String locationId, HtmlDescriptionOfItem item, String locationUri) {
		representation.addType(locationId, Location.class);
		representation.addAnonymous(locationId);

		MutablePropertyValues mpvs = new MutablePropertyValues();
		mpvs.addPropertyValue("uri", locationUri);
		mpvs.addPropertyValue("transportType", TransportType.DOWNLOAD.toString().toLowerCase());
		
		representation.addValues(locationId, mpvs);
	}

	public Representation extractFrom(HtmlDescriptionSource src, DescriptionMode mode) {
		return null;
	}
}
