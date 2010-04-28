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


package org.uriplay.remotesite.dailymotion;

import org.jherd.beans.BeanGraphExtractor;
import org.jherd.beans.DescriptionMode;
import org.jherd.beans.Representation;
import org.jherd.beans.id.IdGenerator;
import org.jherd.beans.id.IdGeneratorFactory;
import org.springframework.beans.MutablePropertyValues;
import org.uriplay.media.TransportType;
import org.uriplay.media.entity.Encoding;
import org.uriplay.media.entity.Item;
import org.uriplay.media.entity.Location;
import org.uriplay.media.entity.Version;
import org.uriplay.query.content.PerPublisherCurieExpander;
import org.uriplay.remotesite.html.HtmlDescriptionOfItem;
import org.uriplay.remotesite.html.HtmlDescriptionSource;

import com.google.common.collect.Sets;

public class DailyMotionItemGraphExtractor implements BeanGraphExtractor<HtmlDescriptionSource>  {

	private final IdGeneratorFactory idGeneratorFactory;

	public DailyMotionItemGraphExtractor(IdGeneratorFactory idGen) {
		this.idGeneratorFactory = idGen;
	}

	public Representation extractFrom(HtmlDescriptionSource src) {
		String itemUri = src.getUri();
		IdGenerator idGenerator = idGeneratorFactory.create();

		Representation representation = new Representation();
		
		String versionId = idGenerator.getNextId();
		String encodingId = idGenerator.getNextId();
		String locationId = idGenerator.getNextId();
		
		addItemPropertiesTo(representation, itemUri, versionId, src.getItem());
		addVersionPropertiesTo(representation, versionId, encodingId);
		addEncodingPropertiesTo(representation, encodingId, locationId);
		addLocationPropertiesTo(representation, locationId, src.getItem());
		
		return representation;
	}
	
	private void addItemPropertiesTo(Representation representation, String itemUri, String versionId, HtmlDescriptionOfItem item) {
		representation.addType(itemUri, Item.class);
		representation.addUri(itemUri);

		MutablePropertyValues mpvs = new MutablePropertyValues();
		mpvs.addPropertyValue("title", item.getTitle());
		mpvs.addPropertyValue("description", item.getDescription());
		mpvs.addPropertyValue("publisher", "dailymotion.com");
		mpvs.addPropertyValue("versions", Sets.newHashSet(versionId));
		mpvs.addPropertyValue("thumbnail", item.getThumbnail());
		
		if (itemUri.startsWith("http://www.dailymotion.com/video/")) {
			mpvs.addPropertyValue("curie", PerPublisherCurieExpander.CurieAlgorithm.DM.compact(itemUri));
		}
		
		representation.addValues(itemUri, mpvs);
	}
	
	private void addEncodingPropertiesTo(Representation representation, String encodingId, String locationId) {
		representation.addType(encodingId, Encoding.class);
		representation.addAnonymous(encodingId);
		
		MutablePropertyValues mpvs = new MutablePropertyValues();
		mpvs.addPropertyValue("availableAt", Sets.newHashSet(locationId));
		mpvs.addPropertyValue("dataContainerFormat", "application/x-shockwave-flash");
		representation.addValues(encodingId, mpvs);
	}

	private void addVersionPropertiesTo(Representation representation, String versionId, String encodingId) {
	
		representation.addType(versionId, Version.class);
		representation.addAnonymous(versionId);
		MutablePropertyValues mpvs = new MutablePropertyValues();
		mpvs.addPropertyValue("manifestedAs", Sets.newHashSet(encodingId));
		representation.addValues(versionId, mpvs);
	}
	
	private void addLocationPropertiesTo(Representation representation, String locationId, HtmlDescriptionOfItem item) {
		representation.addType(locationId, Location.class);
		representation.addAnonymous(locationId);

		MutablePropertyValues mpvs = new MutablePropertyValues();
	//	mpvs.addPropertyValue("uri", item.getVideoSource());
		mpvs.addPropertyValue("transportType", TransportType.EMBEDOBJECT.toString().toLowerCase());
		mpvs.addPropertyValue("transportSubType", "html");
		mpvs.addPropertyValue("embedCode", embedCode(item.getVideoSource()));
		
		representation.addValues(locationId, mpvs);
	}

	private String embedCode(String videoSource) {
		return "<object width=\"480\" height=\"381\"><param name=\"movie\" value=\"" + videoSource + "&related=0\">" +
				"</param><param name=\"allowFullScreen\" value=\"true\"></param><param name=\"allowScriptAccess\" value=\"always\">" +
				"</param><embed src=\"" + videoSource + "&related=0\" type=\"application/x-shockwave-flash\" width=\"480\" height=\"381\" " +
				"allowFullScreen=\"true\" allowScriptAccess=\"always\"></embed></object>";
	}

	public Representation extractFrom(HtmlDescriptionSource src, DescriptionMode mode) {
		return null;
	}
}
